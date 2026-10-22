package com.hnty.ticketsystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hnty.ticketsystem.common.Result;
import com.hnty.ticketsystem.service.UserService;
import com.hnty.ticketsystem.vo.UserOptionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Crown
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/aftersales")
    public Result<List<UserOptionVo>> getAfterSales(){
        return Result.ok(userService.getAfterSales());
    }

}
