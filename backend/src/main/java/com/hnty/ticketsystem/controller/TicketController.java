package com.hnty.ticketsystem.controller;

import com.hnty.ticketsystem.common.Result;
import com.hnty.ticketsystem.dto.TicketCreateRequest;
import com.hnty.ticketsystem.service.TicketService;
import com.hnty.ticketsystem.vo.TicketCreateVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
