package com.hnty.ticketsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hnty.ticketsystem.common.BizException;
import com.hnty.ticketsystem.entity.Comment;
import com.hnty.ticketsystem.entity.Ticket;
import com.hnty.ticketsystem.entity.User;
import com.hnty.ticketsystem.mapper.CommentMapper;
import com.hnty.ticketsystem.mapper.TicketMapper;
import com.hnty.ticketsystem.mapper.UserMapper;
import com.hnty.ticketsystem.security.CurrentUser;
import com.hnty.ticketsystem.security.UserContext;
import com.hnty.ticketsystem.service.CommentService;
import com.hnty.ticketsystem.vo.CommentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工单评论业务实现
 * 评论要补用户昵称，所以查询结果不直接返回实体
 *
 * @author Crown
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<CommentVo> getComments(Long ticketId) {
        getParticipantTicket(ticketId);

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getTicketId, ticketId);
        // 按对话顺序返回，id 兜底同一秒的评论
        queryWrapper.orderByAsc(Comment::getCreatedAt).orderByAsc(Comment::getId);
        List<Comment> comments = baseMapper.selectList(queryWrapper);

        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .toList();
        // 一次查完评论作者，避免每条评论都查一次用户表
        Map<Long, User> userMap = userIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        return comments.stream()
                .map(comment -> toCommentVo(comment, userMap))
                .toList();
    }

    @Override
    public void addComment(Long ticketId, String content) {
        Ticket ticket = getParticipantTicket(ticketId);

        Comment comment = new Comment();
        comment.setTicketId(ticket.getId());
        // userId 从登录上下文取，不能由前端指定，否则可以冒充别人
        comment.setUserId(UserContext.get().getUserId());
        comment.setContent(content.trim());
        // createdAt 和 deleted 交给数据库默认值
        baseMapper.insert(comment);
    }

    private CommentVo toCommentVo(Comment comment, Map<Long, User> userMap) {
        CommentVo vo = new CommentVo();
        vo.setId(comment.getId());
        vo.setContent(comment.getContent());
        vo.setUserId(comment.getUserId());
        vo.setCreatedAt(comment.getCreatedAt());

        User user = userMap.get(comment.getUserId());
        // 用户被删除时查不到，昵称留空，userId 仍然返回
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserRole(user.getRole());
        }
        return vo;
    }

    /**
     * 校验当前用户是工单参与人，不是就抛 403
     *
     * @param ticketId 工单 id
     * @return 工单实体
     */
    private Ticket getParticipantTicket(Long ticketId) {
        CurrentUser currentUser = UserContext.get();
        if (currentUser == null) {
            throw new BizException(401, "用户未登录");
        }

        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BizException(4001, "工单不存在");
        }

        Long userId = currentUser.getUserId();
        // 提单人和处理人都能看，其他人一律拒绝
        if (!userId.equals(ticket.getCreatedBy()) && !userId.equals(ticket.getAssigneeId())) {
            throw new BizException(403, "用户没有权限");
        }
        return ticket;
    }

}
