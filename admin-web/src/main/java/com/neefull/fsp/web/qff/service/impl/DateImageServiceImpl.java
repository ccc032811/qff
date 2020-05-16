//package com.neefull.fsp.web.qff.service.impl;
//
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import com.baomidou.mybatisplus.core.toolkit.StringPool;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
//import com.neefull.fsp.web.qff.entity.DateImage;
//import com.neefull.fsp.web.qff.mapper.DateImageMapper;
//import com.neefull.fsp.web.qff.service.IDateImageService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @Author: chengchengchu
// * @Date: 2019/12/26  18:34
// */
//
//@Service
//@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
//public class DateImageServiceImpl extends ServiceImpl<DateImageMapper, DateImage> implements IDateImageService {
//
//
//    @Autowired
//    private DateImageMapper dateImageMapper;
//    @Autowired
//    private ProcessInstanceProperties properties;
//
//    @Override
//    public DateImage queryImage(Integer dataId, String vest,String relevance) {
//        return dateImageMapper.queryImage(dataId, vest, relevance);
//    }
//
//    @Override
//    public List<String> findImageByIdAndForm(Integer dataId, String relevance) {
//        List<String> imageList = new ArrayList<>();
//        List<DateImage> list = dateImageMapper.findImageByIdAndForm(dataId,relevance);
//        for (DateImage dateImage : list) {
//            if(dateImage.getImage().length()!=0){
//                String[] images = dateImage.getImage().split(StringPool.COMMA);
//                if(images.length!=0){
//                    for (String image : images) {
//                        imageList.add(properties.getImageUrl()+image);
//                    }
//                }
//            }
//        }
//        return imageList;
//    }
//
//    @Override
//    @Transactional
//    public void insertDateImage(Integer dataId, String vest, String relevance, String url) {
//        DateImage dateImage = new DateImage();
//        dateImage.setImage(url);
//        dateImage.setVest(vest);
//        dateImage.setDataId(dataId);
//        dateImage.setRelevance(relevance);
//        //设置来源
//        dateImage.setSource(2);
//        dateImageMapper.insert(dateImage);
//    }
//
//    @Override
//    @Transactional
//    public void updateDateImage(Integer dataId, String vest, String relevance, String image) {
//        DateImage dateImage = new DateImage();
//        UpdateWrapper<DateImage> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.eq("vest",vest)
//                .eq("data_id",dataId)
//                .eq("relevance",relevance)
//                .eq("source",2);
//        dateImage.setImage(image);
//        dateImageMapper.update(dateImage,updateWrapper);
//    }
//
//    @Override
//    @Transactional
//    public void deleteImage(String url) {
//
//        File file = new File(properties.getImagePath()+url);
//        if(file.exists()){
//            file.delete();
//        }
//    }
//
//}
