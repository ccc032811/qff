package com.neefull.fsp.web.qff.service.impl;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.qff.mapper.RocheMapper;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.service.IRocheService;
import com.neefull.fsp.web.qff.utils.PageUtils;
import com.neefull.fsp.web.system.entity.User;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**罗氏内部发起QFF
 * @Author: chengchengchu
 * @Date: 2019/11/29  13:13
 */

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class RocheServiceImpl extends ServiceImpl<RocheMapper, Roche> implements IRocheService {


    @Autowired
    private RocheMapper rocheMapper;
    @Autowired
    private IProcessService processService;

    @Override
    @Transactional
    public void addRoche(Roche roche) {
        rocheMapper.insert(roche);
    }

    @Override
    @Transactional
    public void editRoche(Roche roche) {
        rocheMapper.update(roche, new UpdateWrapper<Roche>().eq("number",roche.getNumber()));

    }

    @Override
    public IPage<Roche> getRochePage(Roche roche, User user) {

        IPage<Roche> pageInfo = new Page<>();

        if(roche.getAtt()!=null&&roche.getAtt()==1){

            List<Roche> roches = rocheMapper.getPageConserve(roche);
            List<Roche> newRoche = processService.queryRocheTaskByName(roches, user);

            List<Roche> rocheList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(newRoche)){
                for (Roche roc : newRoche) {
                    if(roc.getIsAllow()!=null&&roc.getIsAllow()==1){
                        rocheList.add(roc);
                    }
                }
            }
            List<Roche> page = PageUtils.page(rocheList, roche.getPageSize(), roche.getPageNum());

            pageInfo.setRecords(page);
            pageInfo.setCurrent(roche.getPageNum());
            pageInfo.setTotal(rocheList.size());

        }else {
            pageInfo = rocheMapper.getRochePage(new Page<>(roche.getPageNum(),roche.getPageSize()),roche);
            List<Roche> newRoche = processService.queryRocheTaskByName(pageInfo.getRecords(),user);
            pageInfo.setRecords(newRoche);

        }
        return pageInfo;
    }

    @Override
    @Transactional
    public void updateRocheStatus(Integer id,Integer status) {
        rocheMapper.updateRocheStatus(id,status);
    }

    @Override
    public Roche queryRocheById(Integer id) {
        return rocheMapper.selectById(id);
    }

}
