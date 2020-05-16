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


    /**查询需要完成任务
     * @return
     */
    @GetMapping("/findTask")
    public FebsResponse findTask() throws FebsException {
        try {
            User user = getCurrentUser();
            Integer count = processService.findTask(user.getUsername());
            return new FebsResponse().success().data(count);
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
                    String fileName = attachment.getRemark() + StringPool.DOT + attachment.getAttachType();
                    list.add(fileName);
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

    /**上传文件
     * @param file
     * @return
     * @throws FebsException
     */
    @PostMapping("/uploadImage/{number}")
    public FebsResponse uploadImage(@RequestParam("file") MultipartFile file,@PathVariable(required = false) String number) throws FebsException {
        try {
            Map<String,String> map = fileService.uploadImage(file,number);
            return new FebsResponse().success().data(map);
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
    @GetMapping("/uploadFile/{url}")
    public FebsResponse uploadFile(HttpServletRequest request, HttpServletResponse response,@PathVariable("url") String url){

        OutputStream os=null;
        try {
            File file=new File(properties.getImagePath()+url);
            String fileName=file.getName();
            String ext=fileName.substring(fileName.lastIndexOf(".")+1);
            String agent=(String)request.getHeader("USER-AGENT");
            if(agent!=null && agent.indexOf("Fireforx")!=-1) {
                fileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
            }
            else {
                fileName= URLEncoder.encode(fileName,"UTF-8");
            }
            BufferedInputStream bis=null;
            response.reset();
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/json");
//            if(ext.equals("docx")) {
//                response.setContentType("application/msword");
//            }else if(ext.equals("pdf")) {
//                response.setContentType("application/pdf");
//            }
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            bis=new BufferedInputStream(new FileInputStream(file));
            byte[] b=new byte[bis.available()+1000];
            int i=0;
            os = response.getOutputStream();
            while((i=bis.read(b))!=-1) {
                os.write(b, 0, i);
            }
            os.flush();
            os.close();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(os!=null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new FebsResponse().success();
    }



    /**解析exexl
     * @param file
     * @return
     * @throws FebsException
     */
    @Qff("解析近效期QFF")
    @PostMapping("/resolver")
    @RequiresPermissions("recent:import")
    public FebsResponse resolverExcel(@RequestParam("file") MultipartFile file) throws FebsException {

        try {
            fileService.resolverExcel(file,getCurrentUser());
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "解析失败";
            log.error(message,e);
            throw new FebsException(message);
        }

    }


    /**
     * 下载 Excel导入模板
     */
    @GetMapping("template")
    @RequiresPermissions("recent:template")
    public void generateImportTemplate(HttpServletResponse response) {
        try {
            ClassPathResource pathResource = new ClassPathResource("/templates/excel/近效期产品清单模板.xlsx");
            InputStream inputStream = pathResource.getInputStream();
            Workbook wb = WorkbookFactory.create(inputStream);
            response.reset();
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition", "attachment; filename=" + new String("近效期产品清单模板".getBytes("UTF-8"), "iso8859-1") + ".xlsx");
            wb.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }

    }





}
