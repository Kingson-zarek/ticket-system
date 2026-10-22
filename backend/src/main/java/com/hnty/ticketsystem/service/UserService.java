package com.hnty.ticketsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hnty.ticketsystem.entity.User;
import com.hnty.ticketsystem.vo.UserOptionVo;

import java.util.List;

/**
 * @author Crown
 */
public interface UserService extends IService<User> {
    List<UserOptionVo> getAfterSales();
}
