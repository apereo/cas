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

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.CachingPrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.PersistentIdGenerator;
import org.jasig.cas.authentication.principal.PrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.jasig.cas.services.AbstractAttributeReleasePolicy;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.AttributeFilter;
import org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.jasig.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.jasig.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredServicePublicKey;
import org.jasig.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.jasig.cas.services.ReturnMappedAttributeReleasePolicy;
import org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.jasig.cas.support.oauth.services.OAuthRegisteredCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.jasig.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.jasig.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.jasig.services.persondir.support.merger.ReplacingAttributeAdder;

import javax.cache.expiry.Duration;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the service bean that is produced by the webapp
 * and passed down for edit views.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceEditBean implements Serializable {

    private static final long serialVersionUID = 4882440567964605644L;

    private FormData formData = new FormData();
    private ServiceData serviceData = new ServiceData();
    private int status = -1;

    public ServiceData getServiceData() {
        return serviceData;
    }

    public void setServiceData(final ServiceData serviceData) {
        this.serviceData = serviceData;
    }

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(final FormData formData) {
        this.formData = formData;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    /**
     * From registered service to a service bean.
     *
     * @param svc the svc
     * @return the registered service bean
     */
    public static RegisteredServiceEditBean fromRegisteredService(final RegisteredService svc) {
        final RegisteredServiceEditBean serviceBean = new RegisteredServiceEditBean();
        final ServiceData bean = serviceBean.getServiceData();

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
            bean.setType(RegisteredServiceTypeEditBean.OAUTH_CALLBACK_AUTHZ.toString());
        }

        if (svc instanceof OAuthRegisteredService) {
            bean.setType(RegisteredServiceTypeEditBean.OAUTH.toString());
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
                bean.setLogoutType(RegisteredServiceLogoutTypeEditBean.BACK.toString());
                break;
            case FRONT_CHANNEL:
                bean.setLogoutType(RegisteredServiceLogoutTypeEditBean.FRONT.toString());
                break;
            default:
                bean.setLogoutType(RegisteredServiceLogoutTypeEditBean.NONE.toString());
                break;
        }
        final URL url = svc.getLogoutUrl();
        if (url != null) {
            bean.setLogoUrl(url.toExternalForm());
        }
        final RegisteredServiceUsernameAttributeProvider provider = svc.getUsernameAttributeProvider();
        final RegisteredServiceUsernameAttributeProviderEditBean uBean = bean.getUserAttrProvider();

        if (provider instanceof DefaultRegisteredServiceUsernameProvider) {
            uBean.setType(RegisteredServiceUsernameAttributeProviderEditBean.Types.DEFAULT.toString());
        } else if (provider instanceof AnonymousRegisteredServiceUsernameAttributeProvider) {
            final AnonymousRegisteredServiceUsernameAttributeProvider anonymous =
                    (AnonymousRegisteredServiceUsernameAttributeProvider) provider;
            uBean.setType(RegisteredServiceUsernameAttributeProviderEditBean.Types.ANONYMOUS.toString());
            final PersistentIdGenerator generator = anonymous.getPersistentIdGenerator();
            if (generator instanceof ShibbolethCompatiblePersistentIdGenerator) {
                final ShibbolethCompatiblePersistentIdGenerator sh =
                        (ShibbolethCompatiblePersistentIdGenerator) generator;
                uBean.setValue(new String(sh.getSalt(), Charset.defaultCharset()));
            }
        } else if (provider instanceof PrincipalAttributeRegisteredServiceUsernameProvider) {
            final PrincipalAttributeRegisteredServiceUsernameProvider p =
                    (PrincipalAttributeRegisteredServiceUsernameProvider) provider;
            uBean.setValue(p.getUsernameAttribute());
        }

        final RegisteredServicePublicKey key = svc.getPublicKey();
        final RegisteredServicePublicKeyEditBean pBean = bean.getPublicKey();
        if (key != null) {
            pBean.setAlgorithm(key.getAlgorithm());
            pBean.setLocation(key.getLocation());
        }
        final RegisteredServiceProxyPolicy policy = svc.getProxyPolicy();
        final RegisteredServiceProxyPolicyBean cBean = bean.getProxyPolicy();
        if (policy == null || policy instanceof RefuseRegisteredServiceProxyPolicy) {
            cBean.setType(RegisteredServiceProxyPolicyBean.Types.REFUSE.toString());
        } else if (policy instanceof RegexMatchingRegisteredServiceProxyPolicy) {
            final RegexMatchingRegisteredServiceProxyPolicy regex =
                    (RegexMatchingRegisteredServiceProxyPolicy) policy;
            cBean.setType(RegisteredServiceProxyPolicyBean.Types.ALLOW.toString());
            cBean.setValue(regex.getPattern().toString());
        }

        final AbstractAttributeReleasePolicy attrPolicy = (AbstractAttributeReleasePolicy) svc.getAttributeReleasePolicy();
        if (attrPolicy != null) {
            final RegisteredServiceAttributeReleasePolicyEditBean attrPolicyBean = bean.getAttrRelease();

            attrPolicyBean.setReleasePassword(attrPolicy.isAuthorizedToReleaseCredentialPassword());
            attrPolicyBean.setReleaseTicket(attrPolicy.isAuthorizedToReleaseProxyGrantingTicket());

            final AttributeFilter filter = attrPolicy.getAttributeFilter();
            if (filter != null) {
                if (filter instanceof RegisteredServiceRegexAttributeFilter) {
                    final RegisteredServiceRegexAttributeFilter regex =
                            (RegisteredServiceRegexAttributeFilter) filter;
                    attrPolicyBean.setAttrFilter(regex.getPattern().pattern());
                }
            }

            final PrincipalAttributesRepository pr = attrPolicy.getPrincipalAttributesRepository();
            if (pr instanceof DefaultPrincipalAttributesRepository) {
                attrPolicyBean.setAttrOption(
                        RegisteredServiceAttributeReleasePolicyEditBean.Types.DEFAULT.toString());
            } else if (pr instanceof CachingPrincipalAttributesRepository) {
                attrPolicyBean.setAttrOption(
                        RegisteredServiceAttributeReleasePolicyEditBean.Types.CACHED.toString());
                final CachingPrincipalAttributesRepository cc = (CachingPrincipalAttributesRepository) pr;
                final Duration duration = cc.getDuration();
                attrPolicyBean.setCachedExpiration(duration.getDurationAmount());
                attrPolicyBean.setCachedTimeUnit(duration.getTimeUnit().name());

                final IAttributeMerger merger = cc.getMergingStrategy();

                if (merger != null) {
                    if (merger instanceof NoncollidingAttributeAdder) {
                        attrPolicyBean.setMergingStrategy(
                                RegisteredServiceAttributeReleasePolicyEditBean.AttributeMergerTypes.ADD.toString());
                    } else if (merger instanceof MultivaluedAttributeMerger) {
                        attrPolicyBean.setMergingStrategy(
                                RegisteredServiceAttributeReleasePolicyEditBean.AttributeMergerTypes.MULTIVALUED.toString());
                    } else if (merger instanceof ReplacingAttributeAdder) {
                        attrPolicyBean.setMergingStrategy(
                                RegisteredServiceAttributeReleasePolicyEditBean.AttributeMergerTypes.REPLACE.toString());
                    }
                }
            }
            final RegisteredServiceAttributeReleasePolicyStrategyEditBean sBean = attrPolicyBean.getAttrPolicy();
            if (attrPolicy instanceof ReturnAllAttributeReleasePolicy) {
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALL.toString());
            } else if (attrPolicy instanceof ReturnAllowedAttributeReleasePolicy) {
                final ReturnAllowedAttributeReleasePolicy attrPolicyAllowed = (ReturnAllowedAttributeReleasePolicy) attrPolicy;
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALLOWED.toString());
                sBean.setAttributes(attrPolicyAllowed.getAllowedAttributes());
            } else if (attrPolicy instanceof ReturnMappedAttributeReleasePolicy) {
                final ReturnMappedAttributeReleasePolicy attrPolicyAllowed = (ReturnMappedAttributeReleasePolicy) attrPolicy;
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.MAPPED.toString());
                sBean.setAttributes(attrPolicyAllowed.getAllowedAttributes());
            }
        }
        return serviceBean;
    }

    /**
     * To registered service.
     *
     * @return the registered service
     */
    public RegisteredService toRegisteredService() {
        try {
            final AbstractRegisteredService regSvc;

            if (StringUtils.equalsIgnoreCase(this.serviceData.getType(),
                    RegisteredServiceTypeEditBean.OAUTH_CALLBACK_AUTHZ.toString())) {
                regSvc = new OAuthRegisteredCallbackAuthorizeService();
            } else if  (StringUtils.equalsIgnoreCase(this.serviceData.getType(),
                    RegisteredServiceTypeEditBean.OAUTH.toString())) {
                regSvc = new OAuthRegisteredService();

                final RegisteredServiceOAuthTypeEditBean oauthBean = this.serviceData.getOauth();
                ((OAuthRegisteredService) regSvc).setClientId(oauthBean.getClientId());
                ((OAuthRegisteredService) regSvc).setClientSecret(oauthBean.getClientSecret());
                ((OAuthRegisteredService) regSvc).setBypassApprovalPrompt(oauthBean.isBypass());
            } else {
                regSvc = new RegexRegisteredService();
            }

            regSvc.setId(this.serviceData.assignedId);
            regSvc.setServiceId(this.serviceData.serviceId);
            regSvc.setName(this.serviceData.name);
            regSvc.setDescription(this.serviceData.description);

            if (StringUtils.isNotBlank(this.serviceData.logoUrl)) {
                regSvc.setLogo(new URL(this.serviceData.logoUrl));
            }
            regSvc.setTheme(this.serviceData.theme);
            regSvc.setEvaluationOrder(this.serviceData.evalOrder);


            if (StringUtils.equalsIgnoreCase(this.serviceData.logoutType,
                    RegisteredServiceLogoutTypeEditBean.BACK.toString())) {
                regSvc.setLogoutType(LogoutType.BACK_CHANNEL);
            } else if (StringUtils.equalsIgnoreCase(this.serviceData.logoutType,
                    RegisteredServiceLogoutTypeEditBean.FRONT.toString())) {
                regSvc.setLogoutType(LogoutType.FRONT_CHANNEL);
            } else {
                regSvc.setLogoutType(LogoutType.NONE);
            }

            if (StringUtils.isNotBlank(this.serviceData.logoutUrl)) {
                regSvc.setLogoutUrl(new URL(this.serviceData.logoutUrl));
            }

            final RegisteredServiceAccessStrategy accessStrategy = regSvc.getAccessStrategy();

            ((DefaultRegisteredServiceAccessStrategy) accessStrategy)
                    .setEnabled(this.serviceData.supportAccess.isCasEnabled());
            ((DefaultRegisteredServiceAccessStrategy) accessStrategy)
                    .setSsoEnabled(this.serviceData.supportAccess.isSsoEnabled());
            ((DefaultRegisteredServiceAccessStrategy) accessStrategy)
                    .setRequireAllAttributes(this.serviceData.supportAccess.isRequireAll());
            ((DefaultRegisteredServiceAccessStrategy) accessStrategy)
                    .setRequiredAttributes(this.serviceData.supportAccess.getRequiredAttr());

            return regSvc;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The type Form data.
     */
    public static class FormData {
        private List<String> availableAttributes = new ArrayList<>();
        private List<String> availableUsernameAttributes = new ArrayList<>();

        public List<String> getAvailableAttributes() {
            return availableAttributes;
        }

        public void setAvailableAttributes(final List<String> availableAttributes) {
            this.availableAttributes = availableAttributes;
        }

        public List<String> getAvailableUsernameAttributes() {
            return availableUsernameAttributes;
        }

        public void setAvailableUsernameAttributes(final List<String> availableUsernameAttributes) {
            this.availableUsernameAttributes = availableUsernameAttributes;
        }
    }

    /**
     * The type Service data.
     */
    public static class ServiceData {
        private long assignedId;
        private String serviceId;
        private String name;
        private String description;
        private String logoUrl;
        private String theme;
        private int evalOrder = Integer.MIN_VALUE;
        private List<String> requiredHandlers = new ArrayList<>();
        private String logoutUrl;
        private RegisteredServiceSupportAccessEditBean supportAccess = new RegisteredServiceSupportAccessEditBean();
        private String type = RegisteredServiceTypeEditBean.CAS.toString();
        private RegisteredServiceOAuthTypeEditBean oauth = new RegisteredServiceOAuthTypeEditBean();
        private String logoutType = RegisteredServiceLogoutTypeEditBean.BACK.toString();
        private RegisteredServiceUsernameAttributeProviderEditBean userAttrProvider =
                new RegisteredServiceUsernameAttributeProviderEditBean();
        private RegisteredServicePublicKeyEditBean publicKey = new RegisteredServicePublicKeyEditBean();
        private RegisteredServiceProxyPolicyBean proxyPolicy = new RegisteredServiceProxyPolicyBean();
        private RegisteredServiceAttributeReleasePolicyEditBean attrRelease
                = new RegisteredServiceAttributeReleasePolicyEditBean();

        public RegisteredServiceAttributeReleasePolicyEditBean getAttrRelease() {
            return attrRelease;
        }

        public void setAttrRelease(final RegisteredServiceAttributeReleasePolicyEditBean attrRelease) {
            this.attrRelease = attrRelease;
        }

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

        public String getLogoutType() {
            return logoutType;
        }

        public void setLogoutType(final String logoutType) {
            this.logoutType = logoutType;
        }

        public RegisteredServiceUsernameAttributeProviderEditBean getUserAttrProvider() {
            return userAttrProvider;
        }

        public void setUserAttrProvider(final RegisteredServiceUsernameAttributeProviderEditBean userAttrProvider) {
            this.userAttrProvider = userAttrProvider;
        }

        public String getType() {
            return type;
        }

        public void setType(final String type) {
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

    }
}
