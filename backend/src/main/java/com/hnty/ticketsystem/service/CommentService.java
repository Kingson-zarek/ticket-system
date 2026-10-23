package com.hnty.ticketsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hnty.ticketsystem.entity.Comment;
import com.hnty.ticketsystem.vo.CommentVo;

import java.util.List;

/**
 * 工单评论服务
 * 只有工单参与人能查看和发表评论
 *
 * @author Crown
 */
public interface CommentService extends IService<Comment> {

    /**
     * 查询工单评论
     *
     * @param ticketId 工单 id
     * @return 按时间升序的评论
     */
    List<CommentVo> getComments(Long ticketId);

    /**
     * 发表评论
     *
     * @param ticketId 工单 id
     * @param content  评论内容
     */
    void addComment(Long ticketId, String content);

}
