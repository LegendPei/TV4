package com.peitianbao.www.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author leg
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {
    private Integer couponId;
    private String couponName;
    private int couponType;
    private int discountAmount;
    private int minSpend;
    private int totalStock;
    private int availableStock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int maxPerUser;
    private Integer shopId;
}
