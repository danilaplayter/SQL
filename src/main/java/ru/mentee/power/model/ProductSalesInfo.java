/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class ProductSalesInfo {
    private Long productId;
    private String productName;
    private String category;
    private int totalOrdersCount;
    private int totalQuantitySold;
}
