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

import org.jasig.cas.services.RegisteredService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the service bean that is produced by the webapp
 * and passed down for edit views.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceEditBean implements Serializable {

    private static final long serialVersionUID = 4882440567964605644L;

    private long assignedId;
    private String serviceId;
    private String name;
    private String description;
    private String logoUrl;
    private String theme;
    private int evalOrder;
    private List<String> requiredHandlers = new ArrayList<>();
    private String logoutUrl;
    private SupportAccessEditBean supportAccess = new SupportAccessEditBean();
    private RegisteredServiceTypeEditBean type = RegisteredServiceTypeEditBean.CAS;
    private RegisteredServiceOAuthTypeEditBean oauth = new RegisteredServiceOAuthTypeEditBean();
    private RegisteredServiceLogoutTypeEditBean logoutType = RegisteredServiceLogoutTypeEditBean.BACK;
    private UsernameAttributeProviderEditBean userAttrProvider = new UsernameAttributeProviderEditBean();

    public String getTheme() {
        return theme;
    }

    public void setTheme(final String theme) {
        this.theme = theme;
    }

    public int getEvalOrder() {
        return evalOrder;
    }

    public void setEvalOrder(final int evalOrder) {
        this.evalOrder = evalOrder;
    }

    public List<String> getRequiredHandlers() {
        return requiredHandlers;
    }

    public void setRequiredHandlers(final List<String> requiredHandlers) {
        this.requiredHandlers = requiredHandlers;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(final String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public RegisteredServiceOAuthTypeEditBean getOauth() {
        return oauth;
    }

    public void setOauth(final RegisteredServiceOAuthTypeEditBean oauth) {
        this.oauth = oauth;
    }

    public RegisteredServiceLogoutTypeEditBean getLogoutType() {
        return logoutType;
    }

    public void setLogoutType(final RegisteredServiceLogoutTypeEditBean logoutType) {
        this.logoutType = logoutType;
    }

    public UsernameAttributeProviderEditBean getUserAttrProvider() {
        return userAttrProvider;
    }

    public void setUserAttrProvider(final UsernameAttributeProviderEditBean userAttrProvider) {
        this.userAttrProvider = userAttrProvider;
    }

    public RegisteredServiceTypeEditBean getType() {
        return type;
    }

    public void setType(final RegisteredServiceTypeEditBean type) {
        this.type = type;
    }

    public SupportAccessEditBean getSupportAccess() {
        return supportAccess;
    }

    public void setSupportAccess(final SupportAccessEditBean supportAccess) {
        this.supportAccess = supportAccess;
    }

    public long getAssignedId() {
        return assignedId;
    }

    public void setAssignedId(final long assignedId) {
        this.assignedId = assignedId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(final String serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(final String logoUrl) {
        this.logoUrl = logoUrl;
    }

    /**
     * From registered service to a service bean.
     *
     * @param svc the svc
     * @return the registered service bean
     */
    public static RegisteredServiceEditBean fromRegisteredService(final RegisteredService svc) {
        final RegisteredServiceEditBean bean = new RegisteredServiceEditBean();
        bean.setAssignedId(svc.getId());
        bean.setServiceId(svc.getServiceId());
        bean.setName(svc.getName());
        bean.setDescription(svc.getDescription());
        if (svc.getLogo() != null) {
            bean.setLogoUrl(svc.getLogo().toExternalForm());
        }


        return bean;
    }
}
