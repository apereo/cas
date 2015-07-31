/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.services.web.beans;

import java.io.Serializable;

/**
 * Attribute release policy defined per JSON feed.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceAttributeReleasePolicyEditBean extends RegisteredServiceAttributeReleasePolicyViewBean
        implements Serializable {
    private static final long serialVersionUID = -7567470297544895709L;

    private String attrFilter;
    private String cachedTimeUnit;
    private long cachedExpiration;

    public String getAttrFilter() {
        return attrFilter;
    }

    public void setAttrFilter(final String attrFilter) {
        this.attrFilter = attrFilter;
    }

    public String getCachedTimeUnit() {
        return cachedTimeUnit;
    }

    public void setCachedTimeUnit(final String cachedTimeUnit) {
        this.cachedTimeUnit = cachedTimeUnit;
    }

    public long getCachedExpiration() {
        return cachedExpiration;
    }

    public void setCachedExpiration(final long cachedExpiration) {
        this.cachedExpiration = cachedExpiration;
    }
}
