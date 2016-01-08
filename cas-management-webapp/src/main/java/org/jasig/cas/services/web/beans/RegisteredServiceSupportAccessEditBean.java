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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Defines a JSON bean that is mapped
 * to the CAS service access strategy.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceSupportAccessEditBean implements Serializable {

    private static final long serialVersionUID = 2995938566845586064L;

    private String startingTime;
    private String endingTime;

    private boolean casEnabled;
    private boolean ssoEnabled;
    private boolean requireAll;
    private Map<String, Set<String>> requiredAttr = new HashMap<>();

    public boolean isCasEnabled() {
        return casEnabled;
    }

    public void setCasEnabled(final boolean casEnabled) {
        this.casEnabled = casEnabled;
    }

    public boolean isSsoEnabled() {
        return ssoEnabled;
    }

    public void setSsoEnabled(final boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }

    public boolean isRequireAll() {
        return requireAll;
    }

    public void setRequireAll(final boolean requireAll) {
        this.requireAll = requireAll;
    }

    public Map<String, Set<String>> getRequiredAttr() {
        return requiredAttr;
    }

    public void setRequiredAttr(final Map<String, Set<String>> requiredAttr) {
        this.requiredAttr = requiredAttr;
    }

    public String getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(final String startingTime) {
        this.startingTime = startingTime;
    }

    public String getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(final String endingTime) {
        this.endingTime = endingTime;
    }
}
