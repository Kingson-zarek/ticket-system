package com.hnty.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建工单入参
 * status 不在这里，初始状态固定 OPEN，不接受前端传值
 *
 * @author Crown
 */
@Data
public class TicketCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多200字")
    private String title;

    @NotBlank(message = "问题描述不能为空")
    private String content;

    // 不传时服务端兜成 NORMAL
    @Pattern(regexp = "^(LOW|NORMAL|HIGH|URGENT)$", message = "优先级只能是LOW/NORMAL/HIGH/URGENT")
    private String priority;

    @Size(max = 50, message = "客户名称最多50字")
    private String customerName;

    @NotNull(message = "请选择处理人")
    private Long assigneeId;
}
