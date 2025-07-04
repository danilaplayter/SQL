/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserOrderSummary {
    private long userId;
    private String userName;
    private String email;
    private int ordersCount;
    private BigDecimal totalSpent;
}
