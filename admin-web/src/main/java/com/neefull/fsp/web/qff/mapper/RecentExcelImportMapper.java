package com.neefull.fsp.web.qff.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neefull.fsp.web.qff.entity.Recent;
import com.neefull.fsp.web.qff.entity.RecentExcelImport;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: chengchengchu
 * @Date: 2020/1/9  9:52
 */
@Component
public interface RecentExcelImportMapper extends BaseMapper<RecentExcelImport> {
    /**导出excel
     * @param page
     * @param recent
     * @return
     */
    IPage<RecentExcelImport> getRecentExcelImportPage(Page<RecentExcelImport> page, Recent recent);

}
