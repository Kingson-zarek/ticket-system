package com.hnty.ticketsystem.vo;

import lombok.Data;

/**
 * 创建工单返回体
 * 只回最少必要信息，不回整个工单，前端拿到 id 就能跳详情
 *
 * @author Crown
 */
@Data
public class TicketCreateVo {

    private Long id;

    private String ticketNo;
}
