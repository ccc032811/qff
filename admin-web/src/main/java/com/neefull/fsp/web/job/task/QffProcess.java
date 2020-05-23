package com.neefull.fsp.web.job.task;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.config.SftpProperties;
import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.service.IAttachmentService;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.utils.MailUtils;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.qff.utils.SftpUtils;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tomcat.jni.Proc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

/**
 * @Author: chengchengchu
 * @Date: 2020/5/12  18:32
 */
@Slf4j
@Component
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class QffProcess extends BaseController {

    @Autowired
    private ICommodityService commodityService;
    @Autowired
    private IAttachmentService attachmentService;
    @Autowired
    private IProcessService processService;
    @Autowired
    private SendMailProperties mailProperties;
    @Autowired
    private IUserService userService;
    @Autowired
    private SftpProperties properties;


    @Transactional
    public void getAttmentAndStart(){

        //下载文件获取所有的文件名
        SftpUtils sftp = null;
        List<String> list = new ArrayList<>();
        try {
            sftp = new SftpUtils(properties.getHost(),properties.getUsername(),properties.getPassword());
            sftp.connect();
            list = sftp.batchDownLoadFile(properties.getSftpPath(), properties.getLocalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            sftp.disconnect();
        }

        Map<String,Commodity> commoditys = new HashMap<>();
        Map<String,String> files = new HashMap<>();

        if(CollectionUtils.isNotEmpty(list)){
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()){
                String fileName = iterator.next();
                String number = fileName.split("_")[0];
                //根据number 去查询
                Commodity commodity = commodityService.queryCommodityByNumber(number);
                //判断是否为新建状态
                if(commodity!=null&&commodity.getStatus()==1) {
                    //添加到队列中去
                    commoditys.put(number, commodity);
                    //插入新的关联附件数据
                    Attachment attachment = new Attachment();
                    attachment.setQffId(number);
                    attachment.setQffType(commodity.getStage());
                    attachment.setAttachType(fileName.substring(fileName.lastIndexOf(".") + 1));
                    attachment.setAttachSize(new File(properties.getLocalPath() + fileName).length() / 1024);
                    attachment.setRemark(fileName.substring(0, fileName.lastIndexOf(".")));
                    attachment.setSource(1);
                    attachment.setEnable(1);
                    attachmentService.addAttachment(attachment);
                    //加入要发送的附件集合
                    files.put(fileName,properties.getLocalPath() + fileName);

                }else {
                    iterator.remove();
                }

            }
        }

        try {
                sftp = new SftpUtils(properties.getHost(),properties.getUsername(),properties.getPassword());
                sftp.connect();
                if(!files.isEmpty()){
                    Set<String> strings = files.keySet();
                    for (String string : strings) {
                        sftp.deleteSFTP(properties.getSftpPath(),string);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                sftp.disconnect();
            }


        if(!commoditys.isEmpty()){
            Set<String> strings = commoditys.keySet();
            for (String string : strings) {
                Commodity commodity = commoditys.get(string);
                processService.commitProcess(commodity,new User());
                commodityService.updateCommodityStatus(commodity.getId(), ProcessConstant.UNDER_REVIEW);
            }

            // 发送邮件
            //拼接文件内容
            StringBuilder content=new StringBuilder("<html><head></head><body><h3>您好。当前从系统获取数据如下：</h3>");
            content.append("<tr><h3>具体详情如下表所示:</h3></tr>");
            content.append("<table border='5' style='border:solid 1px #000;font-size=10px;'>");
            content.append("<tr style='background-color: #00A1DD'><td>运输单号</td>" +
                    "<td>QFF编号</td><td>Plant工厂</td><td>KDL Material物料</td>" +
                    "<td>康德乐SAP 批次</td><td>罗氏物料号</td><td>药厂物料号</td><td>罗氏批号</td><td>生产日期</td>" +
                    "<td>有效期</td><td>异常总数</td><td>Remark箱号/备注</td></tr>");

            for (String string : strings) {
                Commodity commodity = commoditys.get(string);
                content.append("<tr><td>"+commodity.getTransport()+"</td><td>"+commodity.getNumber()+"</td>" +
                        "<td>"+commodity.getPlant()+"</td><td>"+commodity.getkMater()+"</td>" +
                        "<td>"+commodity.getkBatch()+"</td><td>"+commodity.getrMater()+"</td>" +
                        "<td>"+commodity.getpMater()+"</td><td>"+commodity.getrBatch()+"</td><td>"+commodity.getManuDate()+"</td>" +
                        "<td>"+commodity.getExpiryDate()+"</td><td>"+commodity.getQuarantine()+"</td><td>"+commodity.getRemark()+"</td></tr>");
            }
            content.append("</table>");
            content.append("</body></html>");

            String text = content.toString();
            //查询收件人
            List<User> userList = userService.findUserByRoleId(86);
            List<String> userMails = new ArrayList<>();
            for (User user : userList) {
                userMails.add(user.getEmail());
            }
            String[] mails = userMails.toArray(new String[0]);

            //发送邮件
            MailUtils.sendMail(text,mailProperties,mails,files);

        }





//        Map<String,String> files = new HashMap<>();
//        //去附件表查询所有的附件数
//        if(CollectionUtils.isNotEmpty(commodityList)){
//            for (Commodity commodity : commodityList) {
//                if(commodity.getAtt()==0){
//                    commoditys.add(commodity);
//                }else if(commodity.getAtt()==1){
//                    boolean isAready = true;
//                    List<Attachment> attachments = attachmentService.selectAttsByQffId(commodity.getNumber(),commodity.getStage());
//                    if(CollectionUtils.isNotEmpty(attachments)){
//                        SftpUtils sftp = null;
//                        // 本地存放地址   要设置
//                        String localPath = "C:/QFF/";
//                        // Sftp下载路径
//                        String sftpPath = "/UploadOutbound/";
//                        try {
//                            sftp = new SftpUtils("seegw-stg.shaphar.com","QFFTest","1qaz2wsx");
//                            sftp.connect();
//
//                            for (Attachment attachment : attachments) {
//                                boolean isDown = sftp.downloadFile(sftpPath, attachment.getRemark() + StringPool.DOT + attachment.getAttachType(), localPath, attachment.getQffId() + "__" + attachment.getRemark() + StringPool.DOT + attachment.getAttachType());
//                                if(isDown){
//                                    files.put(attachment.getQffId() + "__" + attachment.getRemark(), localPath + attachment.getQffId() + "__" + attachment.getRemark() + StringPool.DOT + attachment.getAttachType());
//                                    File file = new File( localPath + attachment.getQffId() + "__" + attachment.getRemark() + StringPool.DOT + attachment.getAttachType());
//                                    if(file.exists()&&file.isFile()){
//                                        attachment.setEnable(1);
//                                        attachment.setAttachSize(file.length());
//                                        attachmentService.updateAttachment(attachment);
//                                    }
//                                    //attachmentService.updateStatusById(attachment.getId(),1);
//                                }
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }finally {
//                            sftp.disconnect();
//                        }
//                    }
//                    List<Attachment> newAttachment = attachmentService.selectAttsByQffId(commodity.getNumber(),commodity.getStage());
//                    for (Attachment attachment : newAttachment) {
//                        if(attachment.getEnable()==0){
//                            isAready = false;
//                            break;
//                        }
//                    }
//                    if(isAready){
//                        commoditys.add(commodity);
//                    }
//                }
//            }
//        }


    }

}
