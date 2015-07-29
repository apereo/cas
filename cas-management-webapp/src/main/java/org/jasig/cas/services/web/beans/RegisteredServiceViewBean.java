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

import org.jasig.cas.services.AbstractAttributeReleasePolicy;
import org.jasig.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.jasig.cas.services.ReturnMappedAttributeReleasePolicy;

import java.io.Serializable;

/**
 * Defines the service bean that is produced by the webapp
 * and passed down.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceViewBean implements Serializable {

    private static final long serialVersionUID = 4882440567964605644L;

    private long assignedId;
    private boolean sasCASEnabled;
    private String serviceId;
    private String name;
    private String description;
    private String logoUrl;
    private ProxyPolicyViewBean proxyPolicy = new ProxyPolicyViewBean();
    private AttributeReleasePolicyViewBean attrRelease = new AttributeReleasePolicyViewBean();

    public long getAssignedId() {
        return assignedId;
    }

    public void setAssignedId(final long assignedId) {
        this.assignedId = assignedId;
    }

    public boolean isSasCASEnabled() {
        return sasCASEnabled;
    }

    public void setSasCASEnabled(final boolean sasCASEnabled) {
        this.sasCASEnabled = sasCASEnabled;
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

    public ProxyPolicyViewBean getProxyPolicy() {
        return proxyPolicy;
    }

    public void setProxyPolicy(final ProxyPolicyViewBean proxyPolicy) {
        this.proxyPolicy = proxyPolicy;
    }

    public AttributeReleasePolicyViewBean getAttrRelease() {
        return attrRelease;
    }

    public void setAttrRelease(final AttributeReleasePolicyViewBean attrRelease) {
        this.attrRelease = attrRelease;
    }

    /**
     * From registered service to a service bean.
     *
     * @param svc the svc
     * @return the registered service bean
     */
    public static RegisteredServiceViewBean fromRegisteredService(final RegisteredService svc) {
        final RegisteredServiceViewBean bean = new RegisteredServiceViewBean();
        bean.setAssignedId(svc.getId());
        bean.setServiceId(svc.getServiceId());
        bean.setName(svc.getName());
        bean.setDescription(svc.getDescription());
        if (svc.getLogo() != null) {
            bean.setLogoUrl(svc.getLogo().toExternalForm());
        }

        final RegisteredServiceProxyPolicy policy = svc.getProxyPolicy();
        final ProxyPolicyViewBean proxyPolicyBean = bean.getProxyPolicy();

        if (policy instanceof RefuseRegisteredServiceProxyPolicy) {
            final RefuseRegisteredServiceProxyPolicy refuse = (RefuseRegisteredServiceProxyPolicy) policy;
            proxyPolicyBean.setType(ProxyPolicyViewBean.Types.REFUSE.toString());
        } else if (policy instanceof RegexMatchingRegisteredServiceProxyPolicy) {
            final RegexMatchingRegisteredServiceProxyPolicy option = (RegexMatchingRegisteredServiceProxyPolicy) policy;
            proxyPolicyBean.setType(ProxyPolicyViewBean.Types.ALLOW.toString());
            proxyPolicyBean.setValue(option.getPattern().toString());
        }

        final AbstractAttributeReleasePolicy attrPolicy = (AbstractAttributeReleasePolicy) svc.getAttributeReleasePolicy();
        final AttributeReleasePolicyViewBean attrPolicyBean = bean.getAttrRelease();

        attrPolicyBean.setReleasePassword(attrPolicy.isAuthorizedToReleaseCredentialPassword());
        attrPolicyBean.setReleaseTicket(attrPolicy.isAuthorizedToReleaseProxyGrantingTicket());

        if (attrPolicy instanceof ReturnAllAttributeReleasePolicy) {
            attrPolicyBean.setAttrPolicy(AttributeReleasePolicyStrategyViewBean.Types.ALL.toString());
        } else if (attrPolicy instanceof ReturnAllowedAttributeReleasePolicy) {
            final ReturnAllowedAttributeReleasePolicy attrPolicyAllowed = (ReturnAllowedAttributeReleasePolicy) attrPolicy;
            if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                attrPolicyBean.setAttrPolicy(AttributeReleasePolicyStrategyViewBean.Types.NONE.toString());
            } else {
                attrPolicyBean.setAttrPolicy(AttributeReleasePolicyStrategyViewBean.Types.ALLOWED.toString());
            }
        } else if (attrPolicy instanceof ReturnMappedAttributeReleasePolicy) {
            final ReturnMappedAttributeReleasePolicy attrPolicyAllowed = (ReturnMappedAttributeReleasePolicy) attrPolicy;
            if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                attrPolicyBean.setAttrPolicy(
                        AttributeReleasePolicyStrategyViewBean.Types.NONE.toString());
            } else {
                attrPolicyBean.setAttrPolicy(AttributeReleasePolicyStrategyViewBean.Types.MAPPED.toString());
            }
        }
        bean.setSasCASEnabled(svc.getAccessStrategy().isServiceAccessAllowed());
        return bean;
    }
}
