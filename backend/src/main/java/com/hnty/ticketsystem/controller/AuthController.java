package com.hnty.ticketsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hnty.ticketsystem.common.BizException;
import com.hnty.ticketsystem.common.Result;
import com.hnty.ticketsystem.dto.LoginRequest;
import com.hnty.ticketsystem.entity.User;
import com.hnty.ticketsystem.mapper.UserMapper;
import com.hnty.ticketsystem.security.UserContext;
import com.hnty.ticketsystem.util.JwtUtil;
import com.hnty.ticketsystem.vo.LoginVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


/**
 * @author Crown
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private  static  final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginRequest loginRequest) {

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null || !ENCODER.matches(password,user.getPassword())) {
            throw new BizException(4001, "用户名或密码错误！");
        }
        if (user.getEnabled() == null || user.getEnabled() != 1){
            throw new BizException(4002,"用户已被禁用！");
        }

        String token = jwtUtil.generate(user.getId(), user.getRole());
        return Result.ok(new LoginVo(token, user.getId(), user.getNickname(),user.getRole()));
    }

    @GetMapping("/me")
    public Result<LoginVo> me() {
        LoginVo loginVo = new LoginVo();
        loginVo.setUserId(UserContext.userId());
        loginVo.setRole(UserContext.role());
        return Result.ok(loginVo);
    }

}
