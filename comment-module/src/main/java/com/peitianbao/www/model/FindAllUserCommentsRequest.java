package com.peitianbao.www.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author leg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindAllUserCommentsRequest implements Serializable {
    private Comments comments;
    private SortRequest sortRequest;
}
