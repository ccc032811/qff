package com.neefull.fsp.web.qff.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neefull.fsp.web.common.entity.QueryRequest;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.system.entity.User;

import java.util.List;


/**
 * @Author: chengchengchu
 * @Date: 2019/12/6  18:52
 */

public interface ICommodityService extends IService<Commodity> {


    /**新增养护QFF
     * @param commodity
     * @return
     */
    void addCommodity(Commodity commodity);

    /**更新养护QFF
     * @param commodity
     * @return
     */
    void editCommodity(Commodity commodity);

    /**获取养护操作的信息
     * @param commodity
     * @return
     */
    IPage<Commodity> getCommodityPage(Commodity commodity, User user, QueryRequest request);

    /**删除养护QFF
     * @param id
     * @return
     */
    void updateCommodityStatus(Integer id,Integer status);

    /**根据编号查询
     * @param id
     * @return
     */
    Commodity queryCommodityById(Integer id);


    /**根据QFF编号查询
     * @param number
     * @return
     */
    Commodity queryCommodityByNumber(String number);

    /**根据id删除
     * @param id
     */
    void deleteCommodityById(Integer id);

    /**获取所有新建的
     * @return
     */
    List<Commodity> selectAllCommodity();


    /**查询最后的时间
     * @return
     */
    String selectLastTime();


    /**excel导出
     * @param commodity
     * @param user
     * @return
     */
    List<Commodity> getPageConserve(Commodity commodity,User user);

    List<Commodity> queryProcessList(Integer status);

}
