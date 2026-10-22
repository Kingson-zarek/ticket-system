package com.hnty.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 状态流转入参
 *
 * @author Crown
 */
@Data
public class TicketStatusRequest {

    @NotBlank(message = "状态不能为空")
    private String status;

}
