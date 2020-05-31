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
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.tomcat.jni.Proc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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
    @Autowired
    private TemplateEngine templateEngine;


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

        List<Commodity> commodityList = new ArrayList<>();

        if(!commoditys.isEmpty()){
            Set<String> strings = commoditys.keySet();
            for (String string : strings) {
                Commodity commodity = commoditys.get(string);
                processService.commitProcess(commodity,new User());
                commodityService.updateCommodityStatus(commodity.getId(), ProcessConstant.UNDER_REVIEW);
                commodityList.add(commodity);
            }

            // 发送邮件
            Context context = new Context();
            context.setVariable("list",commodityList);
            String text = templateEngine.process("rocheCommodity", context);

            //查询收件人
            List<User> userList = userService.findUserByRoleId(86);
            List<String> userMails = new ArrayList<>();
            for (User user : userList) {
                userMails.add(user.getEmail());
            }
            String[] mails = userMails.toArray(new String[0]);

            //发送邮件
            MailUtils.sendMail(text,mailProperties,mails,files);

            try {
                sftp = new SftpUtils(properties.getHost(),properties.getUsername(),properties.getPassword());
                sftp.connect();
                if(!files.isEmpty()){
                    Set<String> fileList = files.keySet();
                    for (String string : fileList) {
                        String name = string.substring(0, string.lastIndexOf("."));
                        String type = string.substring(string.lastIndexOf(".") + 1);

                        String newName = name+ DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                        String newFile = newName+"."+type;
                        sftp.remove(properties.getSftpPath()+string,properties.getMovepath()+newFile);
//                        sftp.deleteSFTP(properties.getSftpPath(),string);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                sftp.disconnect();
            }


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
