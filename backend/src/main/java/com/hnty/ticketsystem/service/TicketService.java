package com.hnty.ticketsystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hnty.ticketsystem.dto.TicketCreateRequest;
import com.hnty.ticketsystem.dto.TicketQueryRequest;
import com.hnty.ticketsystem.entity.Ticket;
import com.hnty.ticketsystem.vo.PageVo;
import com.hnty.ticketsystem.vo.TicketCreateVo;
import com.hnty.ticketsystem.vo.TicketVo;

/**
 * 工单业务接口
 * 继承 IService 之后 save/list/getById 这些基础方法都有实现，这里只放业务方法
 *
 * @author Crown
 */
public interface TicketService extends IService<Ticket> {

    /**
     * 创建工单，只有售前能调
     *
     * @param ticketCreateRequest 建单入参
     * @return 新工单的 id 和工单号
     */
    TicketCreateVo create(TicketCreateRequest ticketCreateRequest);

    TicketVo getTicketById(Long id);

    PageVo<TicketVo> list(TicketQueryRequest request);

    void assign(Long id, Long assigneeId);
}
