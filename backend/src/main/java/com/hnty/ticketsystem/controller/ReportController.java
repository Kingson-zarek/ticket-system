package com.hnty.ticketsystem.controller;

import com.hnty.ticketsystem.common.Result;
import com.hnty.ticketsystem.service.ReportService;
import com.hnty.ticketsystem.vo.ReportSummaryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计接口
 *
 * @author Crown
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/summary")
    public Result<ReportSummaryVo> summary() {
        return Result.ok(reportService.summary());
    }

}
