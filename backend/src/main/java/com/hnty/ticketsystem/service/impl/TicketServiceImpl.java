package com.hnty.ticketsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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


    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            TicketStatus.OPEN, Set.of(TicketStatus.PROCESSING),
            TicketStatus.PROCESSING, Set.of(TicketStatus.RESOLVED, TicketStatus.CLOSED),
            TicketStatus.RESOLVED, Set.of(TicketStatus.PROCESSING, TicketStatus.CLOSED),
            TicketStatus.CLOSED, Set.of()
    );

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

    @Override
    public void assign(Long id, Long assigneeId) {
        CurrentUser currentUser = UserContext.get();
        if (currentUser == null) {
            throw new BizException(401, "用户未登录");
        }
        if (!Role.PRESALES.equals(currentUser.getRole())) {
            throw new BizException(403,"没有权限修改");
        }
        Ticket ticket = baseMapper.selectById(id);
        if (ticket == null) {
            throw new BizException(4001,"工单不存在");
        }
        if (!currentUser.getUserId().equals(ticket.getCreatedBy())) {
            throw new BizException(403, "只能改派自己创建的工单");
        }
        if (TicketStatus.CLOSED.equals(ticket.getStatus())) {
            throw new BizException(4001,"工单已关闭");
        }
        checkAssignee(assigneeId);
        Ticket update = new Ticket();
        update.setId(id);
        update.setAssigneeId(assigneeId);
        baseMapper.updateById(update);
    }

    @Override
    public void transStatus(Long id, String status) {
        CurrentUser currentUser = UserContext.get();
        if (currentUser == null) {
            throw new BizException(401, "用户未登录");
        }
        if (!TicketStatus.CLOSED.equals(status) && !TicketStatus.OPEN.equals(status)
                && !TicketStatus.PROCESSING.equals(status) && !TicketStatus.RESOLVED.equals(status)) {
            throw new BizException(4001, "目标状态不合法");
        }
        Ticket ticket = baseMapper.selectById(id);
        if (ticket == null) {
            throw new BizException(4001,"工单不存在");
        }
        if (!currentUser.getUserId().equals(ticket.getCreatedBy()) && !currentUser.getUserId().equals(ticket.getAssigneeId())) {
            throw new BizException(403,"没有权限修改");
        }
        Set<String> allowed = TRANSITIONS.getOrDefault(ticket.getStatus(), Set.of());
        if (!allowed.contains(status)) {
            throw new BizException(4001, "不能从 " + ticket.getStatus() + " 流转到 " + status);
        }
        boolean isCreator  = currentUser.getUserId().equals(ticket.getCreatedBy());
        boolean isAssignee = currentUser.getUserId().equals(ticket.getAssigneeId());

        // 状态机已经保证只剩这五种流转，这里只判断谁有资格操作
        if (TicketStatus.PROCESSING.equals(status) && TicketStatus.OPEN.equals(ticket.getStatus())) {
            // 接单只能由当前处理人操作
            if (!isAssignee || !Role.AFTERSALES.equals(currentUser.getRole())) {
                throw new BizException(403, "只有当前处理人可以接单");
            }
        } else if (TicketStatus.RESOLVED.equals(status) && TicketStatus.PROCESSING.equals(ticket.getStatus())) {
            // 标记解决也只能由当前处理人操作
            if (!isAssignee || !Role.AFTERSALES.equals(currentUser.getRole())) {
                throw new BizException(403, "只有当前处理人可以标记解决");
            }
        } else if (TicketStatus.CLOSED.equals(status) && TicketStatus.PROCESSING.equals(ticket.getStatus())) {
            // 处理中直接关闭，提单人或处理人都可以，前面的参与人校验已经拦过外人
        } else if (TicketStatus.PROCESSING.equals(status) && TicketStatus.RESOLVED.equals(ticket.getStatus())) {
            // 客户反馈没解决，只有提单人能退回
            if (!isCreator || !Role.PRESALES.equals(currentUser.getRole())) {
                throw new BizException(403, "只有提单人可以退回");
            }
        } else if (TicketStatus.CLOSED.equals(status) && TicketStatus.RESOLVED.equals(ticket.getStatus())) {
            // 确认解决后关闭，只有提单人能操作
            if (!isCreator || !Role.PRESALES.equals(currentUser.getRole())) {
                throw new BizException(403, "只有提单人可以关闭");
            }
        }
        // 进入已解决时记录时间，退回处理中要清掉，不然统计会把它算成已解决
        LambdaUpdateWrapper<Ticket> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Ticket::getId, id).set(Ticket::getStatus, status);
        if (TicketStatus.RESOLVED.equals(status)) {
            updateWrapper.set(Ticket::getResolvedAt, LocalDateTime.now());
        }
        if (TicketStatus.RESOLVED.equals(ticket.getStatus())
                && TicketStatus.PROCESSING.equals(status)) {
            // updateById 会忽略 null 字段，清空只能走 UpdateWrapper
            updateWrapper.set(Ticket::getResolvedAt, null);
        }
        baseMapper.update(updateWrapper);
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
