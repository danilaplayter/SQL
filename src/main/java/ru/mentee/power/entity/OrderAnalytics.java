/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.entity;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAnalytics {
    private long userId;
    private int ordersCount;
    private BigDecimal totalSpent;
    private BigDecimal avgOrderValue;
    private String customerType;

    public OrderAnalytics(
            Long userId, Integer ordersCount, BigDecimal totalSpent, BigDecimal avgOrderValue) {
        this.userId = userId;
        this.ordersCount = ordersCount;
        this.totalSpent = totalSpent;
        this.avgOrderValue = avgOrderValue;
        this.customerType = calculateCustomerType(totalSpent);
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
        this.customerType = calculateCustomerType(totalSpent);
    }

    private String calculateCustomerType(BigDecimal totalSpent) {
        if (totalSpent == null) {
            return "NEW";
        }
        if (totalSpent.compareTo(BigDecimal.valueOf(50000)) > 0) {
            return "VIP";
        } else if (totalSpent.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            return "REGULAR";
        } else {
            return "NEW";
        }
    }
}
