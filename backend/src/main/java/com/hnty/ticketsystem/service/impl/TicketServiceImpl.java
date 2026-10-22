package com.hnty.ticketsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hnty.ticketsystem.common.BizException;
import com.hnty.ticketsystem.common.Role;
import com.hnty.ticketsystem.common.TicketPriority;
import com.hnty.ticketsystem.common.TicketStatus;
import com.hnty.ticketsystem.dto.TicketCreateRequest;
import com.hnty.ticketsystem.dto.TicketQueryRequest;
import com.hnty.ticketsystem.entity.Ticket;
import com.hnty.ticketsystem.entity.User;
import com.hnty.ticketsystem.mapper.TicketMapper;
import com.hnty.ticketsystem.mapper.UserMapper;
import com.hnty.ticketsystem.security.CurrentUser;
import com.hnty.ticketsystem.security.UserContext;
import com.hnty.ticketsystem.service.TicketService;
import com.hnty.ticketsystem.vo.PageVo;
import com.hnty.ticketsystem.vo.TicketCreateVo;
import com.hnty.ticketsystem.vo.TicketVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    public PageVo<TicketVo> list(TicketQueryRequest request) {
        CurrentUser currentUser = UserContext.get();
        if (currentUser == null) {
            throw new BizException(401, "用户未登录");
        }
        LambdaQueryWrapper<Ticket> queryWrapper = new LambdaQueryWrapper<>();
        if (Role.PRESALES.equals(currentUser.getRole())) {
            queryWrapper.eq(Ticket::getCreatedBy, currentUser.getUserId());
        } else if (Role.AFTERSALES.equals(currentUser.getRole())) {
            queryWrapper.eq(Ticket::getAssigneeId, currentUser.getUserId());
        }else {
            throw new BizException(403, "未知角色");
        }
        if (StringUtils.hasText(request.getStatus())){
            queryWrapper.eq(Ticket::getStatus,request.getStatus());
        }
        if (StringUtils.hasText(request.getPriority())){
            queryWrapper.eq(Ticket::getPriority,request.getPriority());
        }
        if (StringUtils.hasText(request.getKeyword())){
            String keyword = request.getKeyword().trim();
            queryWrapper.and(w -> w.like(Ticket::getTitle,keyword)
                    .or().like(Ticket::getTicketNo,keyword));
        }
        queryWrapper.orderByDesc(Ticket::getCreatedAt).orderByDesc(Ticket::getId);
        int current = request.getPage() == null ||request.getPage()<1 ? 1 : request.getPage();
        int size  = request.getSize() == null ||request.getSize() < 1 ? 10 : Math.min(request.getSize(),100);
        Page<Ticket> page = baseMapper.selectPage(new Page<>(current, size), queryWrapper);
        List<TicketVo> list = page.getRecords().stream().map(TicketVo::from).toList();
        List<Long> ids = page.getRecords().stream()
                .flatMap(t -> Stream.of(t.getCreatedBy(), t.getAssigneeId()))
                .filter(Objects::nonNull)
                .distinct().toList();
        if (!ids.isEmpty()){
            List<User> users = userMapper.selectBatchIds(ids);
            Map<Long, String> collect = users.stream().collect(Collectors.toMap(User::getId, User::getNickname));

            for (TicketVo vo : list){
                vo.setCreatedByName(collect.get(vo.getCreatedBy()));
                vo.setAssigneeName(collect.get(vo.getAssigneeId()));
            }

        }
        return PageVo.of(page.getTotal(),list);
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
