package com.neefull.fsp.web.job.task;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.config.SoapUrlProperties;
import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.service.IAttachmentService;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.qff.utils.SapWsUtils;
import com.neefull.fsp.web.qff.utils.XmlUtils;
import com.neefull.fsp.web.system.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Author: chengchengchu
 * @Date: 2020/3/11  15:34
 */
@Slf4j
@Component
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AcquireSoapMessage extends BaseController {

    @Autowired
    private SoapUrlProperties properties;
    @Autowired
    private ICommodityService commodityService;
    @Autowired
    private IAttachmentService attachmentService;
    @Autowired
    private IProcessService processService;


    @Transactional
    public void getMessage(){

        log.info("*****************Execute query from SAP server.******************");
        long startTime = System.currentTimeMillis();
        //获取更新时间

//        String fromDate = commodityService.selectLastTime();
//        String toDate = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
//        String soapMessage = SapWsUtils.getSoapMessage(fromDate, toDate);



        StringBuffer message = new StringBuffer("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://www.shaphar.com/SoapService\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <soap:REQUEST_DATA>\n" +
                "         <soap:commonHeader>\n" +
                "            <soap:BIZTRANSACTIONID>1</soap:BIZTRANSACTIONID>\n" +
                "            <soap:COUNT>1</soap:COUNT>\n" +
                "            <soap:CONSUMER>1</soap:CONSUMER>\n" +
                "            <soap:SRVLEVEL>1</soap:SRVLEVEL>\n" +
                "            <soap:ACCOUNT>1</soap:ACCOUNT>\n" +
                "            <soap:PASSWORD>1</soap:PASSWORD>\n" +
                "            <soap:COMMENTS>1</soap:COMMENTS>\n" +
                "         </soap:commonHeader>\n" +
                "         <soap:LIST><![CDATA[<urn:ZCHN_FM_QFF xmlns:urn=\"urn:sap-com:document:sap:rfc:functions:ZCHN_FM_QFF_BS\"><ET_QFF>\n" +
                "            <item>\n" +
                "               <QMNUM></QMNUM>\n" +
                "               <HERKUNFT></HERKUNFT>\n" +
                "               <MAWERK></MAWERK>\n" +
                "               <MATNR></MATNR>\n" +
                "               <MSTAE></MSTAE>\n" +
                "               <CHARG></CHARG>\n" +
                "               <IDNLF></IDNLF>\n" +
                "               <BISMT></BISMT>\n" +
                "               <LICHN></LICHN>\n" +
                "               <HSDAT></HSDAT>\n" +
                "               <VFDAT></VFDAT>\n" +
                "               <MGEIG></MGEIG>\n" +
                "               <QMTXT></QMTXT>\n" +
                "               <ZPROCLAS></ZPROCLAS>\n" +
                "               <REGNO></REGNO>\n" +
                "               <AWBNO></AWBNO>\n" +
                "               <ERDAT></ERDAT>\n" +
                "            </item>\n" +
                "         </ET_QFF>\n" +
                "         <ET_QFF_ATT>\n" +
                "            <item>\n" +
                "               <QMNUM></QMNUM>\n" +
                "               <ATTACHNAME></ATTACHNAME>\n" +
                "               <ATTACH></ATTACH>\n" +
                "            </item>\n" +
                "         </ET_QFF_ATT>\n" +
                "         <IV_DATE_FROM>2020-01-24</IV_DATE_FROM>\n" +
                "         <IV_DATE_TO>2020-05-31</IV_DATE_TO> \n" +
                "       </urn:ZCHN_FM_QFF>]]></soap:LIST>\n" +
                "      </soap:REQUEST_DATA>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>");

        String soapMessage = message.toString();


        log.info("请求报文为："+soapMessage);
        //进行请求 获取soap返回结果
        StringBuffer messageBuffer = null;
        try {
            messageBuffer = SapWsUtils.callWebService(properties.getSoapUrl(), soapMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("返回结果为："+messageBuffer.toString());
        long endTime = System.currentTimeMillis();
        log.info("响应时间: {}ms", (endTime - startTime));
        log.info("*****************Finish query from SAP server.******************");

        //对返回数据结果进行截取

        try {
//            List<Refund> refundList = new ArrayList<>();

            String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd");

            String qffMessage = XmlUtils.getTagContent(messageBuffer.toString(), "<ET_QFF>", "</ET_QFF>");
            String[] qffString = qffMessage.split("<item>");
            List<String> qffList = new ArrayList<>();
            for (String s : qffString) {
                if(StringUtils.isNotEmpty(s)){
                    String dom = s.split("</item>")[0];
                    qffList.add(dom);
                }
            }
            //将所有QFF封装到集合中
            List<Commodity> commodityList = new ArrayList<>();

            if(CollectionUtils.isNotEmpty(qffList)){
                //将数据转换成对象存入数据库中
                for (String s : qffList) {
                    String stage = XmlUtils.getTagContent(s, "<HERKUNFT>", "</HERKUNFT>");

                    Commodity commodity = new Commodity();
                    commodity.setNumber(XmlUtils.getTagContent(s,"<QMNUM>","</QMNUM>"));
                    commodity.setPlant(XmlUtils.getTagContent(s,"<MAWERK>","</MAWERK>"));
                    commodity.setkMater(XmlUtils.getTagContent(s,"<MATNR>","</MATNR>"));
                    commodity.setkBatch(XmlUtils.getTagContent(s,"<CHARG>","</CHARG>"));
                    commodity.setrMater(XmlUtils.getTagContent(s,"<IDNLF>","</IDNLF>"));
                    commodity.setIsDanger(XmlUtils.getTagContent(s,"<MSTAE>","</MSTAE>"));
                    commodity.setpMater(XmlUtils.getTagContent(s,"<BISMT>","</BISMT>"));
                    commodity.setrBatch(XmlUtils.getTagContent(s,"<LICHN>","</LICHN>"));
                    commodity.setManuDate(XmlUtils.getTagContent(s,"<HSDAT>","</HSDAT>"));
                    commodity.setExpiryDate(XmlUtils.getTagContent(s,"<VFDAT>","</VFDAT>"));
                    commodity.setQuarantine(XmlUtils.getTagContent(s,"<MGEIG>","</MGEIG>"));
                    commodity.setGetRemark(XmlUtils.getTagContent(s,"<QMTXT>","</QMTXT>"));
                    commodity.setInitDate(XmlUtils.getTagContent(s,"<ERDAT>","</ERDAT>"));
                    commodity.setClassify(XmlUtils.getTagContent(s,"<ZPROCLAS>","</ZPROCLAS>"));
                    commodity.setRegister(XmlUtils.getTagContent(s,"<REGNO>","</REGNO>"));
                    commodity.setTransport(XmlUtils.getTagContent(s,"<AWBNO>","</AWBNO>"));
                    commodity.setType(stage);
//                    commodity.setStage(XmlUtils.getTagContent(s,"<HERKUNFT>","</HERKUNFT>"));
                    if(stage.equals("01")){
                        commodity.setStage("到货");
                    }else if(stage.equals("09")){
                        commodity.setStage("养护");
                    }else if(stage.equals("10")||stage.equals("11")){
                        commodity.setStage("出库");
                    }else if(stage.equals("05")){
                        commodity.setStage("退货");
                    } else {
                        commodity.setStage("其他");
                    }
                    commodity.setStatus(ProcessConstant.NEW_BUILD);
                    commodity.setAtt(0);



                    //判断该条数据是否记录
                    Commodity isCommodity = commodityService.queryCommodityByNumber(commodity.getNumber());
                    if(isCommodity ==null){
                        //没有改记录就直接添加
                        commodityList.add(commodity);
//                        commodityService.addCommodity(commodity);
//                        processService.commitProcess(commodity,getCurrentUser());
                    }else {
                        //对变化的字段进行记录
                        boolean rstart = false;

                        StringBuilder alteration = new StringBuilder();

                        if(!commodity.getPlant().equals(isCommodity.getPlant())){
                            alteration.append("Plant工厂: " +date+ "   由"+isCommodity.getPlant()+"变更为"+commodity.getPlant()+" 。  ");
                            rstart = true;
                        }
                        if(!commodity.getkMater().equals(isCommodity.getkMater())){
                            alteration.append("KDLMaterial物料: " +date+ "   由"+isCommodity.getkMater()+"变更为"+commodity.getkMater()+" 。  ");
                            rstart = true;
                        }
                        if(!commodity.getkBatch().equals(isCommodity.getkBatch())){
                            alteration.append("康德乐SAP批次: " +date+ "   由"+isCommodity.getkBatch()+"变更为"+commodity.getkBatch()+" 。  ");
                            rstart = true;
                        }
                        if(!commodity.getrMater().equals(isCommodity.getrMater())){
                            alteration.append("罗氏物料号: " +date+ "   由"+isCommodity.getrMater()+"变更为"+commodity.getrMater()+" 。  ");
                            rstart = true;
                        }
                        if(!commodity.getrBatch().equals(isCommodity.getrBatch())){
                            alteration.append("罗氏批号: " +date+ "   由"+isCommodity.getrBatch()+"变更为"+commodity.getrBatch()+" 。  ");
                            rstart = true;
                        }
                        if(!commodity.getManuDate().equals(isCommodity.getManuDate())){
                            alteration.append("生产日期: " +date+ "   由"+isCommodity.getManuDate()+"变更为"+commodity.getManuDate()+" 。  ");
                            rstart = true;
                        }
                        if(!commodity.getExpiryDate().equals(isCommodity.getExpiryDate())){
                            alteration.append("有效期: " +date+ "   由"+isCommodity.getExpiryDate()+"变更为"+commodity.getExpiryDate()+" 。  ");
                            rstart = true;
                        }
                        if(!commodity.getQuarantine().equals(isCommodity.getQuarantine())){
                            alteration.append("异常总数: " +date+ "   由"+isCommodity.getQuarantine()+"变更为"+commodity.getQuarantine()+" 。  ");
                            rstart = true;
                        }
                        if(!commodity.getGetRemark().equals(isCommodity.getGetRemark())){
                            alteration.append("Remark箱号: " +date+ "   由"+isCommodity.getGetRemark()+"变更为"+commodity.getGetRemark()+" 。  ");
                            rstart = true;
                        }
                        if(!commodity.getClassify().equals(isCommodity.getClassify())){
                            alteration.append("产品分类: " +date+ "   由"+isCommodity.getClassify()+"变更为"+commodity.getClassify()+" 。  ");
                            rstart = true;
                        }
                        if(!commodity.getRegister().equals(isCommodity.getRegister())){
                            alteration.append("注册证号: " +date+ "   由"+isCommodity.getRegister()+"变更为"+commodity.getRegister()+" 。  ");
                            rstart = true;
                        }
                        if(rstart){

                            commodityService.deleteCommoddityById(isCommodity.getId());
                            Boolean isExist = processService.queryProcessByKey(isCommodity);
                            if(isExist){
                                processService.deleteInstance(isCommodity);
                            }

                            commodity.setAlteration(alteration.toString());
                            commodityList.add(commodity);

//                            commodityService.editCommodity(commodity);
//                            processService.commitProcess(commodity,getCurrentUser());
                        }
                    }
                }
            }

            //对返回的数据附件进行存储
            String attMessage = XmlUtils.getTagContent(messageBuffer.toString(), "<ET_QFF_ATT>", "</ET_QFF_ATT>");
            String[] attString = attMessage.split("<item>");
            List<String> attList = new ArrayList<>();
            for (String s : attString) {
                if(StringUtils.isNotEmpty(s)){
                    String dom = s.split("</item>")[0];
                    attList.add(dom);
                }
            }

            Map<String,Attachment> attachmentMap = new HashMap<>();

            if(CollectionUtils.isNotEmpty(attList)){
                for (String s : attList) {
                    String number = XmlUtils.getTagContent(s, "<QMNUM>", "</QMNUM>");
                    String attName = XmlUtils.getTagContent(s, "<ATTACHNAME>", "</ATTACHNAME>");
                    String[] split = attName.split("\\.");
                    if(StringUtils.isNotBlank(number)&&StringUtils.isNotBlank(attName)){
                        if(CollectionUtils.isNotEmpty(commodityList)){
                            for (Commodity commodity : commodityList) {
                                if(commodity.getNumber().equals(number)){
                                    Attachment attachment = new Attachment();
                                    attachment.setQffId(commodity.getNumber());
                                    attachment.setQffType(commodity.getStage());
                                    attachment.setAttachType(split[1]);
                                    attachment.setRemark(split[0]);
                                    attachment.setSource(1);
                                    attachment.setEnable(0);
                                    attachmentMap.put(attachment.getRemark(),attachment);
                                    commodity.setAtt(1);
                                }
                            }
                        }
                    }
                }
            }

            for (Commodity commodity : commodityList) {
                commodityService.addCommodity(commodity);
            }
            Set<String> strings = attachmentMap.keySet();
            for (String string : strings) {
                Attachment attachment = attachmentMap.get(string);
                attachmentService.addAttachment(attachment);
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }


    }

}
