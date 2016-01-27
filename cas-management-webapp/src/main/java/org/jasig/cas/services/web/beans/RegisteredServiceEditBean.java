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
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredServicePublicKey;
import org.jasig.cas.services.RegisteredServicePublicKeyImpl;
import org.jasig.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.jasig.cas.services.ReturnMappedAttributeReleasePolicy;
import org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.jasig.cas.support.oauth.services.OAuthRegisteredCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.util.RegexUtils;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.jasig.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.jasig.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.jasig.services.persondir.support.merger.ReplacingAttributeAdder;
import org.slf4j.Logger;
import org.springframework.util.AntPathMatcher;

import javax.cache.expiry.Duration;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.*;

/**
 * Defines the service bean that is produced by the webapp
 * and passed down for edit views.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class RegisteredServiceEditBean implements Serializable {
    private static final long serialVersionUID = 4882440567964605644L;

    private static final Logger LOGGER = getLogger(RegisteredServiceEditBean.class);


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

        bean.setAssignedId(Long.toString(svc.getId()));
        bean.setServiceId(svc.getServiceId());
        bean.setName(svc.getName());
        bean.setDescription(svc.getDescription());
        if (svc.getLogo() != null) {
            bean.setLogoUrl(svc.getLogo().toExternalForm());
        }
        bean.setRequiredHandlers(svc.getRequiredHandlers());

        configureAccessStrategy(svc, bean);

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
            bean.setLogoutUrl(url.toExternalForm());
        }
        final RegisteredServiceUsernameAttributeProvider provider = svc.getUsernameAttributeProvider();
        final RegisteredServiceUsernameAttributeProviderEditBean uBean = bean.getUserAttrProvider();

        configureUsernameAttributeProvider(provider, uBean);

        final RegisteredServicePublicKey key = svc.getPublicKey();
        final RegisteredServicePublicKeyEditBean pBean = bean.getPublicKey();
        if (key != null) {
            pBean.setAlgorithm(key.getAlgorithm());
            pBean.setLocation(key.getLocation());
        }
        configureProxyPolicy(svc, bean);
        configureAttributeReleasePolicy(svc, bean);
        return serviceBean;
    }

    /**
     * Configure access strategy.
     *
     * @param svc the svc
     * @param bean the bean
     */
    private static void configureAccessStrategy(final RegisteredService svc, final ServiceData bean) {
        final DefaultRegisteredServiceAccessStrategy accessStrategy = (DefaultRegisteredServiceAccessStrategy) svc.getAccessStrategy();

        final RegisteredServiceSupportAccessEditBean accessBean = bean.getSupportAccess();
        accessBean.setCasEnabled(accessStrategy.isServiceAccessAllowed());
        accessBean.setSsoEnabled(accessStrategy.isServiceAccessAllowedForSso());
        accessBean.setRequireAll(accessStrategy.isRequireAllAttributes());
        accessBean.setRequiredAttr(accessStrategy.getRequiredAttributes());
        accessBean.setEndingTime(accessStrategy.getEndingDateTime());
        accessBean.setStartingTime(accessStrategy.getStartingDateTime());
    }

    /**
     * Configure attribute release policy.
     *
     * @param svc the svc
     * @param bean the bean
     */
    private static void configureAttributeReleasePolicy(final RegisteredService svc, final ServiceData bean) {
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

                final IAttributeMerger merger = cc.getMergingStrategy().getAttributeMerger();

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
    }

    /**
     * Configure proxy policy.
     *
     * @param svc the svc
     * @param bean the bean
     */
    private static void configureProxyPolicy(final RegisteredService svc, final ServiceData bean) {
        final RegisteredServiceProxyPolicy policy = svc.getProxyPolicy();
        final RegisteredServiceProxyPolicyBean cBean = bean.getProxyPolicy();
        if (policy == null || policy instanceof RefuseRegisteredServiceProxyPolicy) {
            cBean.setType(RegisteredServiceProxyPolicyBean.Types.REFUSE.toString());
        } else if (policy instanceof RegexMatchingRegisteredServiceProxyPolicy) {
            final RegexMatchingRegisteredServiceProxyPolicy regex =
                    (RegexMatchingRegisteredServiceProxyPolicy) policy;
            cBean.setType(RegisteredServiceProxyPolicyBean.Types.REGEX.toString());
            cBean.setValue(regex.getPattern().toString());
        }
    }

    /**
     * Configure username attribute provider.
     *
     * @param provider the provider
     * @param uBean the u bean
     */
    private static void configureUsernameAttributeProvider(final RegisteredServiceUsernameAttributeProvider provider,
                                                           final RegisteredServiceUsernameAttributeProviderEditBean uBean) {
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

                final byte[] saltByte = sh.getSalt();
                if (saltByte != null) {
                    final String salt = new String(saltByte, Charset.defaultCharset());
                    uBean.setValue(salt);
                } else {
                    throw new IllegalArgumentException("Salt cannot be null");
                }
            }
        } else if (provider instanceof PrincipalAttributeRegisteredServiceUsernameProvider) {
            final PrincipalAttributeRegisteredServiceUsernameProvider p =
                    (PrincipalAttributeRegisteredServiceUsernameProvider) provider;
            uBean.setType(RegisteredServiceUsernameAttributeProviderEditBean.Types.ATTRIBUTE.toString());
            uBean.setValue(p.getUsernameAttribute());
        }
    }

    /**
     * The type Form data.
     */
    public static class FormData {
        private List<String> availableAttributes = new ArrayList<>();
        public List<String> getAvailableAttributes() {
            return availableAttributes;
        }

        public void setAvailableAttributes(final List<String> availableAttributes) {
            this.availableAttributes = availableAttributes;
        }

    }

    /**
     * The type Service data.
     */
    public static class ServiceData {
        private String assignedId;
        private String serviceId;
        private String name;
        private String description;
        private String logoUrl;
        private String theme;
        private int evalOrder = Integer.MIN_VALUE;
        private Set<String> requiredHandlers = new HashSet<>();
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

        public Set<String> getRequiredHandlers() {
            return requiredHandlers;
        }

        public void setRequiredHandlers(final Set<String> requiredHandlers) {
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

        public String getAssignedId() {
            return assignedId;
        }

        public void setAssignedId(final String assignedId) {
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
         * To registered service.
         *
         * @param dao the dao
         * @return the registered service
         */
        public RegisteredService toRegisteredService(final IPersonAttributeDao dao) {
            try {
                final AbstractRegisteredService regSvc;

                if (StringUtils.equalsIgnoreCase(this.type,
                        RegisteredServiceTypeEditBean.OAUTH_CALLBACK_AUTHZ.toString())) {
                    regSvc = new OAuthRegisteredCallbackAuthorizeService();
                } else if  (StringUtils.equalsIgnoreCase(this.type,
                        RegisteredServiceTypeEditBean.OAUTH.toString())) {
                    regSvc = new OAuthRegisteredService();

                    final RegisteredServiceOAuthTypeEditBean oauthBean = this.oauth;
                    ((OAuthRegisteredService) regSvc).setClientId(oauthBean.getClientId());
                    ((OAuthRegisteredService) regSvc).setClientSecret(oauthBean.getClientSecret());
                    ((OAuthRegisteredService) regSvc).setBypassApprovalPrompt(oauthBean.isBypass());
                } else {
                    regSvc = determineServiceTypeByPattern(this.serviceId);
                }

                final long assignedId = Long.parseLong(this.assignedId);
                if (assignedId <= 0) {
                    regSvc.setId(RegisteredService.INITIAL_IDENTIFIER_VALUE);
                } else {
                    regSvc.setId(assignedId);
                }

                regSvc.setServiceId(this.serviceId);
                regSvc.setName(this.name);
                regSvc.setDescription(this.description);

                if (StringUtils.isNotBlank(this.logoUrl)) {
                    regSvc.setLogo(new URL(this.logoUrl));
                }
                regSvc.setTheme(this.theme);
                regSvc.setEvaluationOrder(this.evalOrder);
                regSvc.setRequiredHandlers(this.requiredHandlers);

                if (StringUtils.equalsIgnoreCase(this.logoutType,
                        RegisteredServiceLogoutTypeEditBean.BACK.toString())) {
                    regSvc.setLogoutType(LogoutType.BACK_CHANNEL);
                } else if (StringUtils.equalsIgnoreCase(this.logoutType,
                        RegisteredServiceLogoutTypeEditBean.FRONT.toString())) {
                    regSvc.setLogoutType(LogoutType.FRONT_CHANNEL);
                } else {
                    regSvc.setLogoutType(LogoutType.NONE);
                }

                if (StringUtils.isNotBlank(this.logoutUrl)) {
                    regSvc.setLogoutUrl(new URL(this.logoutUrl));
                }

                final DefaultRegisteredServiceAccessStrategy accessStrategy =
                        (DefaultRegisteredServiceAccessStrategy) regSvc.getAccessStrategy();

                accessStrategy.setEnabled(this.supportAccess.isCasEnabled());
                accessStrategy.setSsoEnabled(this.supportAccess.isSsoEnabled());
                accessStrategy.setRequireAllAttributes(this.supportAccess.isRequireAll());
                accessStrategy.setStartingDateTime(this.supportAccess.getStartingTime());
                accessStrategy.setEndingDateTime(this.supportAccess.getEndingTime());

                final Map<String, Set<String>> requiredAttrs = this.supportAccess.getRequiredAttr();
                final Set<Map.Entry<String, Set<String>>> entries = requiredAttrs.entrySet();
                final Iterator<Map.Entry<String, Set<String>>> it = entries.iterator();
                while (it.hasNext()) {
                    final Map.Entry<String, Set<String>> entry = it.next();
                    if (entry.getValue().isEmpty()) {
                        it.remove();
                    }
                }
                accessStrategy.setRequiredAttributes(requiredAttrs);

                final String proxyType = this.proxyPolicy.getType();
                if (StringUtils.equalsIgnoreCase(proxyType,
                        RegisteredServiceProxyPolicyBean.Types.REFUSE.toString())) {
                    regSvc.setProxyPolicy(new RefuseRegisteredServiceProxyPolicy());
                } else if (StringUtils.equalsIgnoreCase(proxyType,
                        RegisteredServiceProxyPolicyBean.Types.REGEX.toString())) {
                    final String value = this.proxyPolicy.getValue();
                    if (StringUtils.isNotBlank(value) && RegexUtils.isValidRegex(value)) {
                        regSvc.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy(value));
                    } else {
                        throw new IllegalArgumentException("Invalid regex pattern specified for proxy policy: " + value);
                    }
                }

                final String uidType = this.userAttrProvider.getType();
                if (StringUtils.equalsIgnoreCase(uidType,
                        RegisteredServiceUsernameAttributeProviderEditBean.Types.DEFAULT.toString())) {
                    regSvc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
                } else if (StringUtils.equalsIgnoreCase(uidType,
                        RegisteredServiceUsernameAttributeProviderEditBean.Types.ANONYMOUS.toString())) {
                    final String salt = this.userAttrProvider.getValue();
                    if (StringUtils.isNotBlank(salt)) {

                        final ShibbolethCompatiblePersistentIdGenerator generator =
                                new ShibbolethCompatiblePersistentIdGenerator(salt);
                        regSvc.setUsernameAttributeProvider(
                                new AnonymousRegisteredServiceUsernameAttributeProvider(generator));
                    } else {
                        throw new IllegalArgumentException("Invalid sale value for anonymous ids " + salt);
                    }
                } else if (StringUtils.equalsIgnoreCase(uidType,
                        RegisteredServiceUsernameAttributeProviderEditBean.Types.ATTRIBUTE.toString())) {
                    final String attr = this.userAttrProvider.getValue();

                    if (StringUtils.isNotBlank(attr)) {
                        regSvc.setUsernameAttributeProvider(
                                new PrincipalAttributeRegisteredServiceUsernameProvider(attr));
                    } else {
                        throw new IllegalArgumentException("Invalid attribute specified for username");
                    }
                }

                if (this.publicKey != null && this.publicKey.isValid()) {
                    final RegisteredServicePublicKey publicKey = new RegisteredServicePublicKeyImpl(
                            this.publicKey.getLocation(), this.publicKey.getAlgorithm());
                    regSvc.setPublicKey(publicKey);
                }

                final RegisteredServiceAttributeReleasePolicyStrategyEditBean policyBean =
                        this.attrRelease.getAttrPolicy();
                final String policyType = policyBean.getType();

                AbstractAttributeReleasePolicy policy = null;
                if (StringUtils.equalsIgnoreCase(policyType,
                        AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALL.toString())) {
                    policy = new ReturnAllAttributeReleasePolicy();
                } else if (StringUtils.equalsIgnoreCase(policyType,
                        AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALLOWED.toString())) {
                    policy = new ReturnAllowedAttributeReleasePolicy((List) policyBean.getAttributes());
                } else if (StringUtils.equalsIgnoreCase(policyType,
                        AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.MAPPED.toString())) {
                    policy = new ReturnMappedAttributeReleasePolicy((Map) policyBean.getAttributes());
                } else {
                    policy = new ReturnAllowedAttributeReleasePolicy();
                }

                final String filter = this.attrRelease.getAttrFilter();
                if (StringUtils.isNotBlank(filter)) {
                    policy.setAttributeFilter(new RegisteredServiceRegexAttributeFilter(filter));
                }

                policy.setAuthorizedToReleaseCredentialPassword(this.attrRelease.isReleasePassword());
                policy.setAuthorizedToReleaseProxyGrantingTicket(this.attrRelease.isReleaseTicket());

                final String attrType = this.attrRelease.getAttrOption();
                if (StringUtils.equalsIgnoreCase(attrType,
                        RegisteredServiceAttributeReleasePolicyEditBean.Types.CACHED.toString())) {
                    policy.setPrincipalAttributesRepository(new CachingPrincipalAttributesRepository(
                            TimeUnit.valueOf(this.attrRelease.getCachedTimeUnit().toUpperCase()),
                            this.attrRelease.getCachedExpiration()));
                } else if (StringUtils.equalsIgnoreCase(attrType,
                        RegisteredServiceAttributeReleasePolicyEditBean.Types.DEFAULT.toString())) {
                    policy.setPrincipalAttributesRepository(new DefaultPrincipalAttributesRepository());
                }
                regSvc.setAttributeReleasePolicy(policy);

                return regSvc;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Determine service type by pattern.
         *
         * @param serviceId the service id
         * @return the abstract registered service
         */
        private static AbstractRegisteredService determineServiceTypeByPattern(final String serviceId) {
            if (RegexUtils.isValidRegex(serviceId)) {
                LOGGER.debug("Service id {} is a valid regex.", serviceId);
                return new RegexRegisteredService();
            }

            if (new AntPathMatcher().isPattern(serviceId)) {
                LOGGER.debug("Service id {} is a valid ant pattern.", serviceId);
                return new RegisteredServiceImpl();
            }
            throw new RuntimeException("Service id " + serviceId + " cannot be resolve to a service type");
        }
    }
}
