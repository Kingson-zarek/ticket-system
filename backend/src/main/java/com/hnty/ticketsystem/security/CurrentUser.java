package com.hnty.ticketsystem.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Crown
 */

@Data
@AllArgsConstructor
public class CurrentUser {

    private Long userId;

    private String role;
}
