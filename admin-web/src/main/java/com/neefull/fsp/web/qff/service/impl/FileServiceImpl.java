package com.neefull.fsp.web.qff.service.impl;


import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.service.IFileService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.system.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;


import java.io.File;
import java.io.IOException;
import java.util.Date;


/**
 * @Author: chengchengchu
 * @Date: 2020/3/13  15:04
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class FileServiceImpl implements IFileService {


    @Autowired
    private IProcessService processService;
    @Autowired
    private ProcessInstanceProperties properties;
    @Autowired
    private SendMailProperties mailProperties;
    @Autowired
    private IUserService userService;
    @Autowired
    private TemplateEngine templateEngine;


    @Override
    @Transactional
    public String uploadImage(MultipartFile file,String number) {

        String originalFilename = file.getOriginalFilename().replace(String.valueOf((char)160),"_").replaceAll("\\s+","_");

        int unixp = originalFilename.lastIndexOf("/");
        int winp = originalFilename.lastIndexOf("\\");
        int posp = (winp > unixp ? winp : unixp);
        if (posp != -1) {
            originalFilename = originalFilename.substring(posp + 1);
        }
        String fileName = DateFormatUtils.format(new Date(),"yyyyMMddHHmmss")+"_"+originalFilename;

        File filePath = new File(properties.getImagePath(), fileName);
        String[] paths = properties.getImagePath().split(StringPool.SLASH);
        String dir = paths[0];
        for (int i = 0; i < paths.length - 1; i++) {
            try {
                dir = dir + "/" + paths[i + 1];
                File dirFile = new File(dir);
                if (!dirFile.exists()) {
                    dirFile.mkdir();
                    System.out.println("创建目录为：" + dir);
                }
            } catch (Exception err) {
                System.err.println("文件夹创建发生异常");
            }
        }

        try {
            file.transferTo(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getImageUrl()+fileName;

    }

//    @Override
//    @Transactional
//    public void resolverExcel(MultipartFile file, User user) {
//
//        List<Recent> list = new ArrayList<>();
//        List<Integer> errorList = new ArrayList();
//
//        if (file.isEmpty()) {
//            log.error("当前文件为空");
//        }
//        try {
//            ExcelKit.$Import(RecentResolver.class).readXlsx(file.getInputStream(), new ExcelReadHandler<RecentResolver>() {
//                @Override
//                public void onSuccess(int sheetIndex, int rowIndex, RecentResolver entity) {
//                    if(rowIndex > SELECT_NUMBER){
//                        Recent recent = new Recent();
//                        if(entity!=null){
//                            if(entity.getkMater().equals("康德乐物料号")||entity.getrMater().equals("罗氏物料号")) {
//                                return;
//                            }else {
//                                if(entity.getkMater().equals("$EMPTY_CELL$")&&entity.getrMater().equals("$EMPTY_CELL$")&&entity.getName().equals("$EMPTY_CELL$")) {
//                                    return;
//                                }else {
//                                    if(!entity.getkMater().equals("$EMPTY_CELL$")){
//                                        recent.setkMater(entity.getkMater());
//                                    }else {
//                                        recent.setkMater("");
//                                    }
//                                    if(!entity.getrMater().equals("$EMPTY_CELL$")){
//                                        recent.setrMater(entity.getrMater());
//                                    }else {
//                                        recent.setrMater("");
//                                    }
//                                    if(!entity.getName().equals("$EMPTY_CELL$")){
//                                        recent.setName(entity.getName());
//                                    }else {
//                                        recent.setName("");
//                                    }
//                                    if(!entity.getUseLife().equals("$EMPTY_CELL$")){
//                                        recent.setUseLife(entity.getUseLife());
//                                    }else {
//                                        recent.setUseLife("");
//                                    }
//                                    if(!entity.getBatch().equals("$EMPTY_CELL$")){
//                                        recent.setBatch(entity.getBatch());
//                                    }else {
//                                        recent.setBatch("");
//                                    }
//                                    if(!entity.getSapBatch().equals("$EMPTY_CELL$")){
//                                        recent.setSapBatch(entity.getSapBatch());
//                                    }else {
//                                        recent.setSapBatch("");
//                                    }
//                                    if(!entity.getFactory().equals("$EMPTY_CELL$")){
//                                        recent.setFactory(entity.getFactory());
//                                    }else {
//                                        recent.setFactory("");
//                                    }
//                                    if(!entity.getWareHouse().equals("$EMPTY_CELL$")){
//                                        recent.setWareHouse(entity.getWareHouse());
//                                    }else {
//                                        recent.setWareHouse("");
//                                    }
//                                    if(!entity.getNumber().equals("$EMPTY_CELL$")){
//                                        recent.setNumber(entity.getNumber());
//                                    }else {
//                                        recent.setNumber("");
//                                    }
//                                }
//                            }
//
//                        }
//                        list.add(recent);
//                    }
//                }
//                @Override
//                public void onError(int sheetIndex, int rowIndex, List<ExcelErrorField> errorFields) {
//                    errorList.add(rowIndex+1);
//                }
//            });
//        } catch (IOException e) {
//            log.error("导入文件失败,原因为：{}",e.getMessage());
//        }
//
//        if(CollectionUtils.isNotEmpty(list)){
//            for (Recent recent : list) {
//                processService.commitProcess(recent,user);
//            }
//        }
//
//        if(CollectionUtils.isNotEmpty(errorList)){
//            String count = "";
//            for (int i=0;i<=errorList.size()-1;i++) {
//                if(i==errorList.size()-1){
//                    count+=errorList.get(i)+"行";
//                }
//                count+=errorList.get(i) +"行 ,";
//            }
//            log.info("导入失败数据,失败行数"+count);
//        }
//        //发送邮件
//        Context context = new Context();
//        context.setVariable("list",list);
//        String text = templateEngine.process("rocheRecent", context);
//
//        String[] emails = getEmails(86);
//        sendMail(text,emails);
//    }
//



}
