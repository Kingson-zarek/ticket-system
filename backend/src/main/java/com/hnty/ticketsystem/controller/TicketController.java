package com.hnty.ticketsystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hnty.ticketsystem.common.Result;
import com.hnty.ticketsystem.dto.TicketAssignRequest;
import com.hnty.ticketsystem.dto.TicketCreateRequest;
import com.hnty.ticketsystem.dto.TicketQueryRequest;
import com.hnty.ticketsystem.dto.TicketStatusRequest;
import com.hnty.ticketsystem.service.TicketService;
import com.hnty.ticketsystem.vo.PageVo;
import com.hnty.ticketsystem.vo.TicketCreateVo;
import com.hnty.ticketsystem.vo.TicketVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 工单接口
 * 只负责收参、调 service、包 Result，不做业务判断
 *
 * @author Crown
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public Result<TicketCreateVo> createTicket(@Valid @RequestBody TicketCreateRequest ticketCreateRequest) {
        return Result.ok(ticketService.create(ticketCreateRequest));
    }

    @GetMapping("/{id}")
    public Result<TicketVo> getTicketById(@PathVariable Long id) {
        return Result.ok(ticketService.getTicketById(id));
    }

    @GetMapping
    public Result<PageVo<TicketVo>> list(TicketQueryRequest request){
        return Result.ok(ticketService.list(request));
    }

    @PutMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @Valid @RequestBody TicketAssignRequest request){
        ticketService.assign(id,request.getAssigneeId());
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Void> transStatus(@PathVariable Long id, @Valid @RequestBody TicketStatusRequest request){
        ticketService.transStatus(id,request.getStatus());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        ticketService.delete(id);
        return Result.ok();
    }


}
