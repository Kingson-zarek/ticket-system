package com.hnty.ticketsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Crown
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginVo {

    private String token;

    private Long userId;

    private String nickname;

    /** presales / aftersales */
    private String role;

}
