package com.hnty.ticketsystem.controller;

import com.hnty.ticketsystem.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Crown
 * @date 2026/10/20 15:50
 */
@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public Result<String> ping() {
        String result = "Hello World!  PONG!!!";
        return Result.ok(result);
    }

}
