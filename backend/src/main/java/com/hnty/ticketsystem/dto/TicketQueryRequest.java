package com.hnty.ticketsystem.dto;

import lombok.Data;

/**
 * 工单列表查询入参
 * GET 请求从 query string 绑定，不用 @RequestBody
 * 分页参数不在这里校验，由 service 兜底纠正
 *
 * @author Crown
 */
@Data
public class TicketQueryRequest {

    // 页码，从 1 开始
    private Integer page = 1;

    // 每页条数
    private Integer size = 10;

    // 状态精确匹配，可不传
    private String status;

    // 优先级精确匹配，可不传
    private String priority;

    // 关键词，对标题和工单号模糊匹配，两者之间是 or
    private String keyword;
}
