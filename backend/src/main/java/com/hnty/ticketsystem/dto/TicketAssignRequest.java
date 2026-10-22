package com.hnty.ticketsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Crown
 */
@Data
public class TicketAssignRequest {

    @NotNull(message = "处理人不能为空")
    private Long assigneeId;

}
