package com.hnty.ticketsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnty.ticketsystem.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Crown
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
