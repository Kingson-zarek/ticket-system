package com.hnty.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Crown
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

}
