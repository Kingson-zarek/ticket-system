package com.hnty.ticketsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hnty.ticketsystem.common.BizException;
import com.hnty.ticketsystem.common.Role;
import com.hnty.ticketsystem.common.TicketPriority;
import com.hnty.ticketsystem.common.TicketStatus;
import com.hnty.ticketsystem.dto.TicketCreateRequest;
import com.hnty.ticketsystem.entity.Ticket;
import com.hnty.ticketsystem.entity.User;
import com.hnty.ticketsystem.mapper.TicketMapper;
import com.hnty.ticketsystem.mapper.UserMapper;
import com.hnty.ticketsystem.security.CurrentUser;
import com.hnty.ticketsystem.security.UserContext;
import com.hnty.ticketsystem.service.TicketService;
import com.hnty.ticketsystem.vo.TicketCreateVo;
import com.hnty.ticketsystem.vo.TicketVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * 工单业务实现
 * 继承 ServiceImpl 以后 baseMapper 就是 TicketMapper，不用再单独注入
 *
 * @author Crown
 */
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements TicketService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public TicketCreateVo create(TicketCreateRequest ticketCreateRequest) {
        CurrentUser user = UserContext.get();
        if (user == null) {
            throw new BizException(401, "用户未登录");
        }
        if (!Role.PRESALES.equals(user.getRole())) {
            throw new BizException(403, "只有售前可以创建工单");
        }

        checkAssignee(ticketCreateRequest.getAssigneeId());

        Ticket ticket = new Ticket();
        ticket.setTitle(ticketCreateRequest.getTitle());
        ticket.setContent(ticketCreateRequest.getContent());
        ticket.setCustomerName(ticketCreateRequest.getCustomerName());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedBy(user.getUserId());
        ticket.setAssigneeId(ticketCreateRequest.getAssigneeId());

        String prefix = "TK" + YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String maxNo = baseMapper.selectMaxNo(prefix+"%");
        int seq = 1;
        if (maxNo != null) {
            seq = Integer.parseInt(maxNo.substring(maxNo.length()-4))+1;
        }
        ticket.setTicketNo(prefix+String.format("%04d", seq));
        ticket.setPriority(ticketCreateRequest.getPriority()==null ? TicketPriority.NORMAL:ticketCreateRequest.getPriority());
        baseMapper.insert(ticket);
        TicketCreateVo vo = new TicketCreateVo();
        vo.setId(ticket.getId());
        vo.setTicketNo(ticket.getTicketNo());
        return vo;
    }

    @Override
    public TicketVo getTicketById(Long id) {
        CurrentUser user = UserContext.get();
        if (user == null) {
            throw new BizException(401, "用户未登录");
        }
        Ticket ticket = baseMapper.selectById(id);
        if (ticket==null){
            throw new BizException(4001,"工单不存在");
        }
        Long uid = user.getUserId();
        boolean isAssignee = uid.equals(ticket.getAssigneeId());
        boolean isCreator  = uid.equals(ticket.getCreatedBy());

        if (!isAssignee && !isCreator) {
            throw new BizException(403, "无权查看该工单");
        }
        TicketVo ticketVo = TicketVo.from(ticket);
        ticketVo.setCreatedByName(nicknameOf(ticket.getCreatedBy()));
        ticketVo.setAssigneeName(nicknameOf(ticket.getAssigneeId()));
        return ticketVo;
    }

    /**
     * 处理人必须是存在的、启用的售后
     * 建单和以后改派都要用这段，抽出来
     */
    private void checkAssignee(Long assigneeId) {
        User assignee = userMapper.selectById(assigneeId);
        if (assignee == null || !Role.AFTERSALES.equals(assignee.getRole())
                || !Integer.valueOf(1).equals(assignee.getEnabled())) {
            throw new BizException(4001, "处理人不存在或不可用");
        }
    }

    private String nicknameOf(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        return user == null ? null : user.getNickname();
    }
}
