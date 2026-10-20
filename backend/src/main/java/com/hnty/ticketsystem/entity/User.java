package com.hnty.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Crown
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    /** presales 售前 / aftersales 售后 */
    private String role;

    /** 1 启用 0 禁用（用户表用禁用，不用软删除） */
    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
