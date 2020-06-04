package com.neefull.fsp.web.qff.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.qff.mapper.RecentExcelImportMapper;
import com.neefull.fsp.web.qff.mapper.RecentMapper;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.service.IRecentService;
import com.neefull.fsp.web.system.entity.User;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**近效期QFF
 * @Author: chengchengchu
 * @Date: 2019/11/29  11:32
 */

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class RecentServiceImpl extends ServiceImpl<RecentMapper, Recent> implements IRecentService {


    @Autowired
    private RecentMapper recentMapper;
    @Autowired
    private IProcessService processService;
    @Autowired
    private RecentExcelImportMapper recentExcelImportMapper;

    @Override
    @Transactional
    public Integer addRecent(Recent recent) {
        int count = recentMapper.insert(recent);
        return count;
    }

    @Override
    @Transactional
    public Integer editRecent(Recent recent) {
        int count = recentMapper.updateById(recent);
        return count;
    }

    @Override
    public IPage<Recent> getRecentPage(Recent recent, User user) {
        Page<Recent> page = new Page<>(recent.getPageNum(),recent.getPageSize());
        IPage<Recent> pageInfo = recentMapper.getRecentPage(page,recent);
        List<Recent> records = pageInfo.getRecords();
        List<Recent> newRecent = processService.queryRecentTaskByName(records,user);
        if(recent.getAtt()!=null&&recent.getAtt()==1){
            List<Recent> recentList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(newRecent)){
                for (Recent rec : newRecent) {
                    if(rec.getIsAllow()!=null&&rec.getIsAllow()==1){
                        recentList.add(rec);
                    }
                }
            }
            pageInfo.setRecords(recentList);
        }else {
            pageInfo.setRecords(newRecent);
        }

        return pageInfo;
    }

    @Override
    @Transactional
    public Integer updateRecentStatus(Integer id,Integer status) {
        Integer count = recentMapper.updateRecentStatus(id,status);
        return count;
    }

    @Override
    public Recent queryRecentById(Integer id) {
        Recent recent = recentMapper.selectById(id);
        return recent;
    }

    @Override
    public IPage<RecentExcelImport> getRecentExcelImportPage(Recent recent) {
        Page<RecentExcelImport> page = new Page<>(recent.getPageNum(),recent.getPageSize());
        return recentExcelImportMapper.getRecentExcelImportPage(page,recent);
    }

}
