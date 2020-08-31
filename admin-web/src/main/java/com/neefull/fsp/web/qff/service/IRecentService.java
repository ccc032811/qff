package com.neefull.fsp.web.qff.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neefull.fsp.web.qff.entity.Recent;
import com.neefull.fsp.web.qff.entity.RecentExcelImport;
import com.neefull.fsp.web.system.entity.User;


/**近效期QFF
 * @Author: chengchengchu
 * @Date: 2019/11/29  11:31
 */
public interface IRecentService extends IService<Recent> {


    /**近效期QFF
     * @param recent
     * @return
     */
    void addRecent(Recent recent);

    /**更新近效期QFF
     * @param recent
     * @return
     */
    void editRecent(Recent recent);

    /**查询近效期QFF
     * @param recent
     * @return
     */
    IPage<Recent> getRecentPage(Recent recent, User user);

    /**删除近效期QFF
     * @param id
     * @return
     */
    void updateRecentStatus(Integer id,Integer status);

    /**查询近效期QFF
     * @param id
     * @return
     */
    Recent queryRecentById(Integer id);

    /**导出excel
     * @param recent
     * @return
     */
    IPage<RecentExcelImport> getRecentExcelImportPage(Recent recent);
}
