package com.hnty.ticketsystem.service.impl;

import com.hnty.ticketsystem.mapper.TicketMapper;
import com.hnty.ticketsystem.service.ReportService;
import com.hnty.ticketsystem.vo.ReportSummaryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 统计服务实现
 *
 * @author Crown
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private TicketMapper ticketMapper;

    @Override
    public ReportSummaryVo summary() {
        return ticketMapper.selectSummary();
    }
}