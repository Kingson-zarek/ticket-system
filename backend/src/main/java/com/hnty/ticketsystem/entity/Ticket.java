package com.hnty.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Crown
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ticket")
public class Ticket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ticketNo;

    private String title;

    private String content;

    private String status;

    private String priority;

    private String customerName;

    private Long createdBy;

    private Long assigneeId;

    private LocalDateTime resolvedAt;

    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
