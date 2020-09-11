package com.neefull.fsp.web.qff.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.qff.mapper.RecentExcelImportMapper;
import com.neefull.fsp.web.qff.mapper.RecentMapper;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.service.IRecentService;
import com.neefull.fsp.web.qff.utils.PageUtils;
import com.neefull.fsp.web.system.entity.User;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jws.soap.SOAPBinding;
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
    public void addRecent(Recent recent) {
        recentMapper.insert(recent);
    }

    @Override
    @Transactional
    public void editRecent(Recent recent) {
        recentMapper.updateById(recent);
    }

    @Override
    public IPage<Recent> getRecentPage(Recent recent, User user) {
        IPage<Recent> pageInfo = new Page<>();

        if(recent.getAtt()!=null&&recent.getAtt()==1){

            List<Recent> recentList = getAttRecent(recent, user);
            List<Recent> page = PageUtils.page(recentList, recent.getPageSize(), recent.getPageNum());

            pageInfo.setRecords(page);
            pageInfo.setCurrent(recent.getPageNum());
            pageInfo.setTotal(recentList.size());

        }else {
            pageInfo = recentMapper.getRecentPage(new Page<>(recent.getPageNum(),recent.getPageSize()),recent);
            List<Recent> newRecent = processService.queryRecentTaskByName( pageInfo.getRecords(),user);
            pageInfo.setRecords(newRecent);
        }

        return pageInfo;
    }


    private List<Recent> getAttRecent(Recent recent, User user){
        recent.setStatus(2);
        List<Recent> recents = recentMapper.getPageConserve(recent);
        List<Recent> newRecent = processService.queryRecentTaskByName(recents, user);
        List<Recent> recentList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(newRecent)){
            for (Recent rec : newRecent) {
                if(rec.getIsAllow()!=null&&rec.getIsAllow()==1){
                    recentList.add(rec);
                }
            }
        }
        return recentList;
    }



    @Override
    @Transactional
    public void updateRecentStatus(Integer id,Integer status) {
         recentMapper.updateRecentStatus(id,status);

    }

    @Override
    public Recent queryRecentById(Integer id) {
        return recentMapper.selectById(id);

    }

    @Override
    public List<Recent> getRecentExcelImportPage(Recent recent,User user) {
        List<Recent> recentList = null;
        if(recent.getAtt()!=null&&recent.getAtt()==1){
            recentList = getAttRecent(recent, user);
        }else {
            recentList = recentMapper.getPageConserve(recent);
        }
        return recentList;
    }

}
