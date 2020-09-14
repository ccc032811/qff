package com.neefull.fsp.web.qff.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.others.entity.Eximport;
import com.neefull.fsp.web.qff.aspect.Qff;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.config.SftpProperties;
import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.entity.Recent;
import com.neefull.fsp.web.qff.entity.RecentResolver;
import com.neefull.fsp.web.qff.service.*;
import com.neefull.fsp.web.qff.utils.MailUtils;
import com.neefull.fsp.web.system.entity.User;
import com.sun.mail.util.MailSSLSocketFactory;
import com.wuwenze.poi.ExcelKit;
import com.wuwenze.poi.handler.ExcelReadHandler;
import com.wuwenze.poi.pojo.ExcelErrorField;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @Author: chengchengchu
 * @Date: 2019/12/16  16:23
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/file")
public class FileController extends BaseController {

    @Autowired
    private IAttachmentService attachmentService;
    @Autowired
    private ProcessInstanceProperties properties;
    @Autowired
    private IFileService fileService;
    @Autowired
    private IProcessService processService;
    @Autowired
    private SftpProperties sftpProperties;



    /**查询需要完成任务
     * @return
     */
    @GetMapping("/findTask")
    public FebsResponse findTask() throws FebsException {
        try {
            User user = getCurrentUser();
            List<String> list = processService.findTask(user.getUsername());
            return new FebsResponse().success().data(list);
        } catch (Exception e) {
            String message = "查询需要完成的任务数失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**查询所有文件
     * @param number
     * @param type
     * @return
     */
    @GetMapping("/findImages/{number}/{type}")
    public FebsResponse findImageByIdAndForm(@PathVariable String number ,@PathVariable String type) throws FebsException {
        List<String> list = new ArrayList<>();
        try {
            List<Attachment> attachments = attachmentService.selectAttsByQffId(number, type);
            if(CollectionUtils.isNotEmpty(attachments)){
                for (Attachment attachment : attachments) {
                    if(attachment.getEnable()==1){
                        String fileName = properties.getImageUrl()+attachment.getRemark() + StringPool.DOT + attachment.getAttachType();
                        list.add(fileName);
                    }
                }
            }
            return new FebsResponse().success().data(list);
        } catch (Exception e) {
            String message = "查询所有文件失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**删除文件
     * @param url
     * @return
     */
    @GetMapping("/deleteImage/{url}")
    public FebsResponse deleteImage(@PathVariable String url) throws FebsException {
        try {
            Boolean isDelete = attachmentService.deleteImage(url);
            if(isDelete){
                return new FebsResponse().success();
            }else {
                return new FebsResponse().fail();
            }

        } catch (Exception e) {
            String message = "删除文件失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    @GetMapping("/removeImage/{url}/{number}/{stage}")
    public FebsResponse removeImage(@PathVariable String url,@PathVariable String number ,@PathVariable String stage) throws FebsException {

        try {
            Boolean isDelete = attachmentService.removeImage(number,url,stage);
            if(isDelete){
                return new FebsResponse().success();
            }else {
                return new FebsResponse().fail();
            }
        } catch (Exception e) {
            String message = "删除文件失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }


    /**上传文件
     * @param file
     * @return
     * @throws FebsException
     */
    @Qff("上传文件")
    @PostMapping("/uploadImage")
    public FebsResponse uploadImage(MultipartFile file, String number) throws FebsException {

        try {
            String url  = fileService.uploadImage(file,number);
            return new FebsResponse().success().data(url);
        } catch (Exception e) {
            String message = "上传文件失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**下载文件
     * @param request
     * @param response
     * @param url
     * @return
     */
    @Qff("下载文件")
    @GetMapping("/uploadFile/{url}")
    public void uploadFiles(HttpServletRequest request, HttpServletResponse response,@PathVariable("url") String url)throws FebsException{

        OutputStream os=null;
        BufferedInputStream bis=null;
        try {
            File file=new File(sftpProperties.getLocalPath()+url);
            if(!file.exists()){
                String message = "当前文件不存在或已损坏";
                throw new FebsException(message);
            }
            String fileName=file.getName();
            String agent=(String)request.getHeader("USER-AGENT");
            if(agent!=null && agent.indexOf("Fireforx")!=-1) {
                fileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
            }
            else {
                fileName= URLEncoder.encode(fileName,"UTF-8");
            }

            bis=new BufferedInputStream(new FileInputStream(file));
            byte[] b=new byte[bis.available()];
            bis.read(b);
            bis.close();

            response.reset();
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/html; charset=UTF-8");
            response.addHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes()));
            response.addHeader("Content-Length",""+file.length());
            os = new BufferedOutputStream(response.getOutputStream());
            os.write(b);

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(os!=null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bis!=null){
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



//    /**
//     * 下载 Excel导入模板
//     */
//    @GetMapping("template")
//    @RequiresPermissions("recent:template")
//    public void generateImportTemplate(HttpServletResponse response) {
//        try {
//            ClassPathResource pathResource = new ClassPathResource("/templates/excel/近效期产品清单模板.xlsx");
//            InputStream inputStream = pathResource.getInputStream();
//            Workbook wb = WorkbookFactory.create(inputStream);
//            response.reset();
//            response.setContentType("multipart/form-data");
//            response.setHeader("Content-Disposition", "attachment; filename=" + new String("近效期产品清单模板".getBytes("UTF-8"), "iso8859-1") + ".xlsx");
//            wb.write(response.getOutputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InvalidFormatException e) {
//            e.printStackTrace();
//        }
//
//    }


}
