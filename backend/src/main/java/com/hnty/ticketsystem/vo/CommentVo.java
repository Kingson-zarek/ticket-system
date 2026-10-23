package com.hnty.ticketsystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论返回体
 * 多带昵称和角色，前端拿到 userId 也显示不了名字
 *
 * @author Crown
 */
@Data
public class CommentVo {

    private Long id;

    private String content;

    private Long userId;

    private String userNickname;

    private String userRole;

    // LocalDateTime 不会走 application.yaml 里的 date-format
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}
