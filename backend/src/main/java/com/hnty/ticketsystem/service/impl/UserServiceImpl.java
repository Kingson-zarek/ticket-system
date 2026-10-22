package com.hnty.ticketsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hnty.ticketsystem.common.Role;
import com.hnty.ticketsystem.entity.User;
import com.hnty.ticketsystem.mapper.UserMapper;
import com.hnty.ticketsystem.service.UserService;
import com.hnty.ticketsystem.vo.UserOptionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Crown
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService  {


    @Override
    public List<UserOptionVo> getAfterSales() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, Role.AFTERSALES);
        queryWrapper.eq(User::getEnabled,1);
//        用这个wrapper拿到id和nickname
//        queryWrapper.select(User::getId, User::getNickname);
        List<User> users = baseMapper.selectList(queryWrapper);
        List<UserOptionVo> userOptionVos = new ArrayList<>();
        for (User user : users) {
            UserOptionVo userOptionVo = new UserOptionVo();
            userOptionVo.setId(user.getId());
            userOptionVo.setNickname(user.getNickname());
            userOptionVos.add(userOptionVo);
        }
        return userOptionVos;
    }
}
