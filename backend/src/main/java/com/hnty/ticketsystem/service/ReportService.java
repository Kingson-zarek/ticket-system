package com.hnty.ticketsystem.service;

import com.hnty.ticketsystem.vo.ReportSummaryVo;

/**
 * 统计服务
 *
 * @author Crown
 */
public interface ReportService {

    /**
     * 首页概览
     *
     * @return 各状态数量、今日新增和平均处理时长
     */
    ReportSummaryVo summary();
}
