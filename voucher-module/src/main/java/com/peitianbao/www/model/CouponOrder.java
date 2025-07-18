package com.peitianbao.www.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author leg
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponOrder {
    private long orderId;
    private Integer couponId;
    private Integer userId;
}
