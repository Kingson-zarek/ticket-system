package com.hnty.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工单评论
 *
 * @author Crown
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ticketId;

    private Long userId;

    private String content;

    // 逻辑删除标记，查询时 MyBatis-Plus 会自动过滤
    private Integer deleted;

    // 创建时间由数据库默认值填充
    private LocalDateTime createdAt;

}
