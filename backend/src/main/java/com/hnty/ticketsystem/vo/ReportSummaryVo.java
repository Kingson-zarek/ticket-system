package com.hnty.ticketsystem.vo;

import lombok.Data;

/**
 * 首页统计看板返回体
 *
 * @author Crown
 */
@Data
public class ReportSummaryVo {

    private Long open;

    private Long processing;

    private Long resolved;

    private Long closed;

    // 今天新建的工单数
    private Long todayNew;

    // 已解决工单的平均处理小时数，没有已解决工单时为 0
    private Double avgHandleHours;

}
