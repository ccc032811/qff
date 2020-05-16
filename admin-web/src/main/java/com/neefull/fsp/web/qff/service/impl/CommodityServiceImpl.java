package com.neefull.fsp.web.qff.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.mapper.CommodityMapper;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    public Integer addCommodity(Commodity commodity) {
        commodity.setStatus(ProcessConstant.NEW_BUILD);
        int count = commodityMapper.insert(commodity);
        return count;
    }

    @Override
    @Transactional
    public Integer editCommodity(Commodity commodity) {
        int count = commodityMapper.updateById(commodity);
        return count;
    }

    @Override
    public IPage<Commodity> getCommodityPage(Commodity commodity, User user) {
        Page<Commodity> page = new Page<>(commodity.getPageNum(),commodity.getPageSize());
        IPage<Commodity> pageInfo = commodityMapper.getConservePage(page,commodity);
        //获取查询的数据
        List<Commodity> records = pageInfo.getRecords();
        List<Commodity> newCommodity = processService.queryCommodityTaskByName(records,user);
        pageInfo.setRecords(newCommodity);
        return pageInfo;
   }

    @Override
    @Transactional
    public Integer updateCommodityStatus(Integer id,Integer status) {
        Integer count = commodityMapper.updateConserveStatus(id,status);
        return count;
    }

    @Override
    public Commodity queryCommodityById(Integer id) {
        Commodity commodity = commodityMapper.selectById(id);
        return commodity;
    }

    @Override
    public Commodity queryCommodityByNumber(String number) {
        QueryWrapper<Commodity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("number", number);
//        return commodityMapper.selectCount(queryWrapper);
        return  commodityMapper.selectOne(queryWrapper);

    }

    @Override
    public void deleteCommoddityById(Integer id) {
        this.baseMapper.deleteById(id);
    }

    @Override
    public List<Commodity> selectAllCommodity() {
        QueryWrapper<Commodity> wrapper = new QueryWrapper<>();
        wrapper.eq("status",1);
        return commodityMapper.selectList(wrapper);

    }

    @Override
    public String selectLastTime() {
        Date date = commodityMapper.selectLastTime();
        return DateFormatUtils.format(date,"yyyy-MM-dd");
    }


}
