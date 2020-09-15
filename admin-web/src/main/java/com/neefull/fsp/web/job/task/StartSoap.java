package com.neefull.fsp.web.job.task;

import com.neefull.fsp.web.common.utils.DateUtil;
import com.neefull.fsp.web.qff.config.SoapUrlProperties;
import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.service.IAttachmentService;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.qff.utils.SapWsUtils;
import com.neefull.fsp.web.qff.utils.XmlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: chengchengchu
 * @Date: 2020/5/22  14:52
 */
@Slf4j
@Component
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class StartSoap {

    @Autowired
    private SoapUrlProperties properties;
    @Autowired
    private ICommodityService commodityService;
    @Autowired
    private IProcessService processService;
    @Autowired
    private IAttachmentService attachmentService;


    @Transactional
    public void getMessage(String seacheDate,String fromTime,String toTime,String number){

        log.info("*****************Execute query from SAP server.******************");
        long startTime = System.currentTimeMillis();


        String soapMessage = SapWsUtils.getSoapMessage(seacheDate,fromTime,toTime,number);

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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = simpleDateFormat.parse(seacheDate+" "+toTime);
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

                    Commodity commodity = new Commodity();

                    commodity.setNumber(XmlUtils.getTagContent(s,"<QMNUM>","</QMNUM>"));
                    commodity.setPlant(XmlUtils.getTagContent(s,"<MAWERK>","</MAWERK>"));
                    commodity.setkMater(XmlUtils.getTagContent(s,"<MATNR>","</MATNR>"));
                    commodity.setkBatch(XmlUtils.getTagContent(s,"<CHARG>","</CHARG>"));
                    commodity.setrMater(XmlUtils.getTagContent(s,"<IDNLF>","</IDNLF>"));
                    commodity.setIsDanger(XmlUtils.getTagContent(s,"<MSTAE>","</MSTAE>"));
                    commodity.setpMater(XmlUtils.getTagContent(s,"<MAKTX>","</MAKTX>"));
                    commodity.setrBatch(XmlUtils.getTagContent(s,"<LICHN>","</LICHN>"));
                    commodity.setManuDate(XmlUtils.getTagContent(s,"<HSDAT>","</HSDAT>"));
                    commodity.setExpiryDate(XmlUtils.getTagContent(s,"<VFDAT>","</VFDAT>"));
                    commodity.setQuarantine(XmlUtils.getTagContent(s,"<MGEIG>","</MGEIG>"));
                    commodity.setGetRemark(XmlUtils.getTagContent(s,"<QMTXT>","</QMTXT>"));
                    commodity.setInitDate(XmlUtils.getTagContent(s,"<ERDAT>","</ERDAT>"));
                    commodity.setClassify(XmlUtils.getTagContent(s,"<ZPROCLAS>","</ZPROCLAS>"));
                    commodity.setRegister(XmlUtils.getTagContent(s,"<REGNO>","</REGNO>"));
                    commodity.setTransport(XmlUtils.getTagContent(s,"<AWBNO>","</AWBNO>"));
                    commodity.setSource(XmlUtils.getTagContent(s,"<ZIMP_LOC>","</ZIMP_LOC>"));
                    commodity.setkUnit(XmlUtils.getTagContent(s,"<MEINS>","</MEINS>"));
                    commodity.setrUnit(XmlUtils.getTagContent(s,"<PRIN_UNIT>","</PRIN_UNIT>"));
                    commodity.setSource(XmlUtils.getTagContent(s,"<ZIMP_LOC>","</ZIMP_LOC>"));

                    String stage = XmlUtils.getTagContent(s, "<HERKUNFT>", "</HERKUNFT>");
                    commodity.setType(stage);
//                    commodity.setStage(XmlUtils.getTagContent(s,"<HERKUNFT>","</HERKUNFT>"));
                    if(stage.equals("01")){
                        commodity.setStage(ProcessConstant.DELIVERY_NAME);
                    }else if(stage.equals("05")){
                        commodity.setStage(ProcessConstant.REFUND_NAME);
                    } else {
                        commodity.setStage(ProcessConstant.CONSERVE_NAME);
                    }
//                    else if(stage.equals("09")){
//                        commodity.setStage(ProcessConstant.CONSERVE_NAME);
//                    }else if(stage.equals("10")||stage.equals("11")){
//                        commodity.setStage(ProcessConstant.EXPORT_NAME);
//                    }
                    commodity.setStatus(ProcessConstant.NEW_BUILD);
                    commodity.setAccessory(0);
                    commodity.setCreateTime(parse);


                    Commodity isCommodity = commodityService.queryCommodityByNumber(commodity.getNumber());
                    if(isCommodity ==null){

                        commodityList.add(commodity);
                    }else {
                        StringBuilder alteration = new StringBuilder();

                        if(!commodity.getPlant().equals(isCommodity.getPlant())){
                            alteration.append("Plant工厂:" +date+ " 由 "+isCommodity.getPlant()+" 变更为 "+commodity.getPlant()+" 。");
                        }
                        if(!commodity.getkMater().equals(isCommodity.getkMater())){
                            alteration.append("KDLMaterial物料:" +date+ " 由 "+isCommodity.getkMater()+" 变更为 "+commodity.getkMater()+" 。");
                        }
                        if(!commodity.getkBatch().equals(isCommodity.getkBatch())){
                            alteration.append("康德乐SAP批次:" +date+ " 由 "+isCommodity.getkBatch()+" 变更为 "+commodity.getkBatch()+" 。");
                        }
                        if(!commodity.getrMater().equals(isCommodity.getrMater())){
                            alteration.append("罗氏物料号:" +date+ " 由 "+isCommodity.getrMater()+" 变更为 "+commodity.getrMater()+" 。");
                        }
                        if(!commodity.getpMater().equals(isCommodity.getpMater())){
                            alteration.append("物料描述:" +date+ " 由 "+isCommodity.getpMater()+" 变更为 "+commodity.getpMater()+" 。");
                        }
                        if(!commodity.getrBatch().equals(isCommodity.getrBatch())){
                            alteration.append("罗氏批号:" +date+ " 由 "+isCommodity.getrBatch()+" 变更为 "+commodity.getrBatch()+" 。");
                        }
                        if(!commodity.getManuDate().equals(isCommodity.getManuDate())){
                            alteration.append("生产日期:" +date+ " 由 "+isCommodity.getManuDate()+" 变更为 "+commodity.getManuDate()+" 。");
                        }
                        if(!commodity.getExpiryDate().equals(isCommodity.getExpiryDate())){
                            alteration.append("有效期:" +date+ " 由 "+isCommodity.getExpiryDate()+" 变更为 "+commodity.getExpiryDate()+" 。");
                        }
                        if(!commodity.getQuarantine().equals(isCommodity.getQuarantine())){
                            alteration.append("异常总数:" +date+ " 由 "+isCommodity.getQuarantine()+" 变更为 "+commodity.getQuarantine()+" 。");
                        }
                        if(!commodity.getGetRemark().equals(isCommodity.getGetRemark())){
                            alteration.append("Remark箱号:" +date+ " 由 "+isCommodity.getGetRemark()+" 变更为 "+commodity.getGetRemark()+" 。");
                        }
                        if(!commodity.getClassify().equals(isCommodity.getClassify())){
                            alteration.append("产品分类:" +date+ " 由 "+isCommodity.getClassify()+" 变更为 "+commodity.getClassify()+" 。");
                        }
                        if(!commodity.getRegister().equals(isCommodity.getRegister())){
                            alteration.append("注册证号:" +date+ " 由 "+isCommodity.getRegister()+" 变更为 "+commodity.getRegister()+" 。");
                        }
                        if(!commodity.getSource().equals(isCommodity.getSource())){
                            alteration.append("采购来源:" +date+ " 由 "+isCommodity.getSource()+" 变更为 "+commodity.getSource()+" 。");
                        }



                        Boolean isExist = processService.queryProcessByKey(isCommodity);
                        if(isExist){
                            processService.deleteInstance(isCommodity);
                        }
                        //有变更，删除原来的数据
                        commodityService.deleteCommodityById(isCommodity.getId());
                        if(!isExist) {
                            attachmentService.deleteByNumber(isCommodity.getNumber(), isCommodity.getStage());
                        }
                        if(StringUtils.isNotEmpty(isCommodity.getAlteration())){
                            commodity.setAlteration(isCommodity.getAlteration()+"  "+alteration.toString());
                        }else {
                            if(StringUtils.isNotEmpty(alteration.toString())) {
                                commodity.setAlteration(alteration.toString());
                            }
                        }
                        commodityList.add(commodity);
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

            Map<String, Attachment> attachmentMap = new HashMap<>();

            if(CollectionUtils.isNotEmpty(attList)){
                for (String s : attList) {
                    String num = XmlUtils.getTagContent(s, "<QMNUM>", "</QMNUM>");
                    String attName = XmlUtils.getTagContent(s, "<ATTACHNAME>", "</ATTACHNAME>");
                    String[] split = attName.split("\\.");
                    if(StringUtils.isNotBlank(num)&&StringUtils.isNotBlank(attName)){
                        if(CollectionUtils.isNotEmpty(commodityList)){
                            for (Commodity commodity : commodityList) {
                                if(commodity.getNumber().equals(num)){
                                    Attachment attachment = new Attachment();
                                    attachment.setQffId(commodity.getNumber());
                                    attachment.setQffType(commodity.getStage());
                                    attachment.setAttachType(split[1]);
                                    attachment.setRemark(split[0]);
                                    attachment.setSource(1);
                                    attachment.setEnable(0);
                                    attachment.setStatus(0);
                                    attachmentMap.put(attachment.getRemark(),attachment);
                                    commodity.setAccessory(commodity.getAccessory()+1);
                                }
                            }
                        }
                    }
                }
            }

            for (Commodity commodity : commodityList) {
                commodityService.addCommodity(commodity);
            }

            if(attachmentMap.size()!=0){
                Set<String> strings = attachmentMap.keySet();
                for (String atr : strings) {
                    Attachment attachment = attachmentMap.get(atr);
                    attachmentService.addAttachment(attachment);
                }
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }
}
