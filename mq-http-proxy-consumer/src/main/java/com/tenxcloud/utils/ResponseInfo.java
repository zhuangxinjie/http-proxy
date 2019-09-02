/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.tenxcloud.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ResponseInfo
 *
 * @author huhu
 * @version v1.0
 * @date 2019-03-06 18:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseInfo {
    Integer code;
    String data;
}
