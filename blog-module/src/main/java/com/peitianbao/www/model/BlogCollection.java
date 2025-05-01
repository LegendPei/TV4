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
public class BlogCollection {
    private Integer blogId;
    private Integer authorId;
    private LocalDateTime collectionDate;
}
