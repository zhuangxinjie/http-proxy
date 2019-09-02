/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.tenxcloud.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
public final class K8sServiceURL {

    @Setter(AccessLevel.NONE)
    private String service;

    @Setter(AccessLevel.NONE)
    private String pathAndQuery;
}
