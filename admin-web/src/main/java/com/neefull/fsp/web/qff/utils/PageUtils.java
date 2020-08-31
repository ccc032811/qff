package com.neefull.fsp.web.qff.utils;



import java.util.ArrayList;
import java.util.List;

/**
 * @Author: chengchengchu
 * @Date: 2020/8/31  15:41
 */
public class PageUtils {


    public  static  List page(List applyList, int pageSize, int currentPage) {
        List currentPageList = new ArrayList<>();
        if (applyList != null && applyList.size() > 0) {
            int currIdx = (currentPage > 1 ? (currentPage - 1) * pageSize : 0);
            for (int i = 0; i < pageSize && i < applyList.size() - currIdx; i++) {
                currentPageList.add(applyList.get(currIdx + i));
            }
        }
        return currentPageList;
    }
}
