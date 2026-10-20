package com.hnty.ticketsystem.controller;

import com.hnty.ticketsystem.common.Result;
import com.hnty.ticketsystem.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Crown
 */
@RestController
@RequestMapping("/api")
public class PingController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/ping")
    public Result<String> ping() {
        String result = "Hello World!  PONG!!!";
        return Result.ok(result);
    }

    @GetMapping("/getUser")
    public Result ping2() {
        return Result.ok(userMapper.selectList(null));
    }

}
