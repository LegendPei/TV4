package com.peitianbao.www.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author leg
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogCollection implements Serializable {
    private Integer blogId;
    private Integer userId;
    private LocalDateTime collectionDate;
}
