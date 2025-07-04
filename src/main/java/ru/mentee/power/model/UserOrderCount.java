/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserOrderCount {
    private Long userId;
    private String userName;
    private String email;
    private int ordersCount;
    private BigDecimal totalSpent;
}
