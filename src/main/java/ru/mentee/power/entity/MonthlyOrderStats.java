/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.entity;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyOrderStats {

    private int year;
    private int month;
    private int ordersCount;
    private BigDecimal monthlyRevenue;
    private BigDecimal avgOrderValue;
}
