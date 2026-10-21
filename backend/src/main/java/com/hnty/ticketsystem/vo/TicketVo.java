package com.hnty.ticketsystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hnty.ticketsystem.entity.Ticket;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单返回体
 * 比实体多两个昵称字段，前端拿到 id 没法显示，得给名字
 * 两个昵称要从 t_user 补，from 方法只拷基础字段，名字由调用方填
 *
 * @author Crown
 */
@Data
public class TicketVo {

    private Long id;

    private String ticketNo;

    private String title;

    private String content;

    private String status;

    private String priority;

    private String customerName;

    // 提单人
    private Long createdBy;

    private String createdByName;

    // 处理人，未指派时两个字段都是 null
    private Long assigneeId;

    private String assigneeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;

    // application.yaml 里的 jackson.date-format 只管 java.util.Date，不管 LocalDateTime
    // 不标这个注解，返回的是 2025-10-21T14:30:00 这种带 T 的格式
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 把实体里直接对应的字段拷过来，昵称需要调用方另外补
     *
     * @param ticket 工单实体
     * @return 未填昵称的 vo
     */
    public static TicketVo from(Ticket ticket) {
        TicketVo vo = new TicketVo();
        vo.setId(ticket.getId());
        vo.setTicketNo(ticket.getTicketNo());
        vo.setTitle(ticket.getTitle());
        vo.setContent(ticket.getContent());
        vo.setStatus(ticket.getStatus());
        vo.setPriority(ticket.getPriority());
        vo.setCustomerName(ticket.getCustomerName());
        vo.setCreatedBy(ticket.getCreatedBy());
        vo.setAssigneeId(ticket.getAssigneeId());
        vo.setResolvedAt(ticket.getResolvedAt());
        vo.setCreatedAt(ticket.getCreatedAt());
        vo.setUpdatedAt(ticket.getUpdatedAt());
        return vo;
    }
}
