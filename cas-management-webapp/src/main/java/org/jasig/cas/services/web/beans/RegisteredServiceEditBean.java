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

import org.jasig.cas.authentication.principal.PersistentIdGenerator;
import org.jasig.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.jasig.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.jasig.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.support.oauth.services.OAuthRegisteredCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;

import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
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
    private RegisteredServiceSupportAccessEditBean supportAccess = new RegisteredServiceSupportAccessEditBean();
    private RegisteredServiceTypeEditBean type = RegisteredServiceTypeEditBean.CAS;
    private RegisteredServiceOAuthTypeEditBean oauth = new RegisteredServiceOAuthTypeEditBean();
    private RegisteredServiceLogoutTypeEditBean logoutType = RegisteredServiceLogoutTypeEditBean.BACK;
    private UsernameAttributeProviderEditBean userAttrProvider = new UsernameAttributeProviderEditBean();
    private RegisteredServicePublicKeyEditBean publicKey = new RegisteredServicePublicKeyEditBean();
    private RegisteredServiceProxyPolicyBean proxyPolicy = new RegisteredServiceProxyPolicyBean();
    private RegisteredServiceAttributeReleasePolicyEditBean attrPolicy
            = new RegisteredServiceAttributeReleasePolicyEditBean();

    public RegisteredServicePublicKeyEditBean getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(final RegisteredServicePublicKeyEditBean publicKey) {
        this.publicKey = publicKey;
    }

    public RegisteredServiceProxyPolicyBean getProxyPolicy() {
        return proxyPolicy;
    }

    public void setProxyPolicy(final RegisteredServiceProxyPolicyBean proxyPolicy) {
        this.proxyPolicy = proxyPolicy;
    }

    public RegisteredServiceAttributeReleasePolicyEditBean getAttrPolicy() {
        return attrPolicy;
    }

    public void setAttrPolicy(final RegisteredServiceAttributeReleasePolicyEditBean attrPolicy) {
        this.attrPolicy = attrPolicy;
    }

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

    public RegisteredServiceSupportAccessEditBean getSupportAccess() {
        return supportAccess;
    }

    public void setSupportAccess(final RegisteredServiceSupportAccessEditBean supportAccess) {
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

        final RegisteredServiceAccessStrategy accessStrategy = svc.getAccessStrategy();
        final RegisteredServiceSupportAccessEditBean accessBean = bean.getSupportAccess();
        accessBean.setCasEnabled(accessStrategy.isServiceAccessAllowed());
        accessBean.setSsoEnabled(accessStrategy.isServiceAccessAllowedForSso());

        if (accessStrategy instanceof DefaultRegisteredServiceAccessStrategy) {
            final DefaultRegisteredServiceAccessStrategy def = (DefaultRegisteredServiceAccessStrategy) accessStrategy;
            accessBean.setRequireAll(def.isRequireAllAttributes());
            accessBean.setRequiredAttr(def.getRequiredAttributes());
        }

        if (svc instanceof OAuthRegisteredCallbackAuthorizeService) {
            bean.setType(RegisteredServiceTypeEditBean.OAUTH_CALLBACK_AUTHZ);
        }

        if (svc instanceof OAuthRegisteredService) {
            bean.setType(RegisteredServiceTypeEditBean.OAUTH);
            final OAuthRegisteredService oauth = (OAuthRegisteredService) svc;
            final RegisteredServiceOAuthTypeEditBean oauthBean = bean.getOauth();
            oauthBean.setBypass(oauth.isBypassApprovalPrompt());
            oauthBean.setClientId(oauth.getClientId());
            oauthBean.setClientSecret(oauth.getClientSecret());
        }

        bean.setTheme(svc.getTheme());
        bean.setEvalOrder(svc.getEvaluationOrder());
        final LogoutType logoutType = svc.getLogoutType();
        switch (logoutType) {
            case BACK_CHANNEL:
                bean.setLogoutType(RegisteredServiceLogoutTypeEditBean.BACK);
                break;
            case FRONT_CHANNEL:
                bean.setLogoutType(RegisteredServiceLogoutTypeEditBean.FRONT);
                break;
            default:
                bean.setLogoutType(RegisteredServiceLogoutTypeEditBean.NONE);
                break;
        }
        final URL url = svc.getLogoutUrl();
        if (url != null) {
            bean.setLogoUrl(url.toExternalForm());
        }
        final RegisteredServiceUsernameAttributeProvider provider = svc.getUsernameAttributeProvider();
        final UsernameAttributeProviderEditBean uBean = bean.getUserAttrProvider();

        if (provider instanceof DefaultRegisteredServiceUsernameProvider) {
            uBean.setType(UsernameAttributeProviderEditBean.Types.DEFAULT);
        } else if (provider instanceof AnonymousRegisteredServiceUsernameAttributeProvider) {
            final AnonymousRegisteredServiceUsernameAttributeProvider anonymous =
                    (AnonymousRegisteredServiceUsernameAttributeProvider) provider;
            uBean.setType(UsernameAttributeProviderEditBean.Types.ANONYMOUS);
            final PersistentIdGenerator generator = anonymous.getPersistentIdGenerator();
            if (generator instanceof ShibbolethCompatiblePersistentIdGenerator) {
                final ShibbolethCompatiblePersistentIdGenerator sh =
                        (ShibbolethCompatiblePersistentIdGenerator) generator;
                uBean.setValue(new String(sh.getSalt(), Charset.defaultCharset()));
            }
        }  else if (provider instanceof PrincipalAttributeRegisteredServiceUsernameProvider) {
            final PrincipalAttributeRegisteredServiceUsernameProvider p =
                    (PrincipalAttributeRegisteredServiceUsernameProvider) provider;
            uBean.setValue(p.getUsernameAttribute());
        }



        return bean;
    }
}
