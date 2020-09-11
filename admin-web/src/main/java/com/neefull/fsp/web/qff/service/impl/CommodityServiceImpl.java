package com.neefull.fsp.web.qff.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.mapper.CommodityMapper;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.utils.PageUtils;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @Author: chengchengchu
 * @Date: 2019/12/6  18:53
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class CommodityServiceImpl extends ServiceImpl<CommodityMapper, Commodity> implements ICommodityService {


    @Autowired
    private CommodityMapper commodityMapper;
    @Autowired
    private IProcessService processService;


    @Override
    @Transactional
    public void addCommodity(Commodity commodity) {
        commodity.setStatus(ProcessConstant.NEW_BUILD);
        commodityMapper.insert(commodity);
    }

    @Override
    @Transactional
    public void editCommodity(Commodity commodity) {
        commodityMapper.updateById(commodity);

    }

    @Override
    public IPage<Commodity> getCommodityPage(Commodity commodity, User user) {

        IPage<Commodity> pageInfo = new Page<>();
        if(commodity.getAtt()!=null&&commodity.getAtt()==1){

            List<Commodity> commodities = getAttCommodity(commodity, user);
            List<Commodity> page = PageUtils.page(commodities, commodity.getPageSize(), commodity.getPageNum());

            pageInfo.setRecords(page);
            pageInfo.setCurrent(commodity.getPageNum());
            pageInfo.setTotal(commodities.size());

        }else {

            pageInfo = commodityMapper.getConservePage(new Page<>(commodity.getPageNum(),commodity.getPageSize()),commodity);
            List<Commodity> newCommodity = processService.queryCommodityTaskByName(pageInfo.getRecords(),user);
            pageInfo.setRecords(newCommodity);
        }

        return pageInfo;
    }


    private List<Commodity> getAttCommodity(Commodity commodity,User user){
        commodity.setStatus(2);
        List<Commodity> commodityList = commodityMapper.getPageConserve(commodity);
        List<Commodity> newCommodity = processService.queryCommodityTaskByName(commodityList,user);
        List<Commodity> commodities = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(newCommodity)){
            for (Commodity commo : newCommodity) {
                if(commo.getIsAllow()!=null&&commo.getIsAllow()==1){
                    commodities.add(commo);
                }
            }
        }
        return commodities;
    }

    @Override
    @Transactional
    public void updateCommodityStatus(Integer id,Integer status) {
        commodityMapper.updateConserveStatus(id,status);
    }

    @Override
    public Commodity queryCommodityById(Integer id) {
        return commodityMapper.selectById(id);

    }

    @Override
    public Commodity queryCommodityByNumber(String number) {
        QueryWrapper<Commodity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("number", number);
        return commodityMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public void deleteCommodityById(Integer id) {
        commodityMapper.deleteById(id);
    }

    @Override
    public List<Commodity> selectAllCommodity() {
        QueryWrapper<Commodity> wrapper = new QueryWrapper<>();
        wrapper.eq("status",ProcessConstant.NEW_BUILD);
        return commodityMapper.selectList(wrapper);

    }

    @Override
    public String selectLastTime() {
        Date date = commodityMapper.selectLastTime();
        return DateFormatUtils.format(date,"yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public List<Commodity> getPageConserve(Commodity commodity,User user) {
        List<Commodity> commodityList = null;
        if(commodity.getAtt()!=null&&commodity.getAtt()==1){
            commodityList = getAttCommodity(commodity, user);
        }else {
            commodityList = commodityMapper.getPageConserve(commodity);
        }
        return commodityList;

    }


}
