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

import org.apache.commons.codec.binary.Base64;
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
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.jasig.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.jasig.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.jasig.services.persondir.support.merger.ReplacingAttributeAdder;
import org.slf4j.Logger;
import org.springframework.util.AntPathMatcher;

import javax.cache.expiry.Duration;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.slf4j.LoggerFactory.*;

/**
 * Defines the service bean that is produced by the webapp
 * and passed down for edit views.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class RegisteredServiceEditBean implements Serializable {
    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 4882440567964605644L;

    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = getLogger(RegisteredServiceEditBean.class);


    /**
     * The Form data.
     */
    private FormData formData = new FormData();
    /**
     * The Service data.
     */
    private ServiceData serviceData = new ServiceData();
    /**
     * The Status.
     */
    private int status = -1;

    /**
     * Gets service data.
     *
     * @return the service data
     */
    public ServiceData getServiceData() {
        return serviceData;
    }

    /**
     * Sets service data.
     *
     * @param serviceData the service data
     */
    public void setServiceData(final ServiceData serviceData) {
        this.serviceData = serviceData;
    }

    /**
     * Gets form data.
     *
     * @return the form data
     */
    public FormData getFormData() {
        return formData;
    }

    /**
     * Sets form data.
     *
     * @param formData the form data
     */
    public void setFormData(final FormData formData) {
        this.formData = formData;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
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
        bean.setRequiredHandlers(svc.getRequiredHandlers());

        configureAccessStrategy(svc, bean);

        configureOauthSettings(svc, bean);

        bean.setTheme(svc.getTheme());
        bean.setEvalOrder(svc.getEvaluationOrder());
        configureLogoutTypeAndUrl(svc, bean);

        final RegisteredServiceUsernameAttributeProvider provider = svc.getUsernameAttributeProvider();
        final RegisteredServiceUsernameAttributeProviderEditBean uBean = bean.getUserAttrProvider();

        configureUsernameAttributeProvider(provider, uBean);

        configurePublicKey(svc, bean);
        configureProxyPolicy(svc, bean);
        configureAttributeReleasePolicy(svc, bean);
        return serviceBean;
    }

    /**
     * Configure oauth settings.
     *
     * @param svc the svc
     * @param bean the bean
     */
    private static void configureOauthSettings(final RegisteredService svc, final ServiceData bean) {
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
    }

    /**
     * Configure logout type and url.
     *
     * @param svc the svc
     * @param bean the bean
     */
    private static void configureLogoutTypeAndUrl(final RegisteredService svc, final ServiceData bean) {
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
    }

    /**
     * Configure public key.
     *
     * @param svc the svc
     * @param bean the bean
     */
    private static void configurePublicKey(final RegisteredService svc, final ServiceData bean) {
        final RegisteredServicePublicKey key = svc.getPublicKey();
        final RegisteredServicePublicKeyEditBean pBean = bean.getPublicKey();
        if (key != null) {
            pBean.setAlgorithm(key.getAlgorithm());
            pBean.setLocation(key.getLocation());
        }
    }

    /**
     * Configure access strategy.
     *
     * @param svc the svc
     * @param bean the bean
     */
    private static void configureAccessStrategy(final RegisteredService svc, final ServiceData bean) {
        final RegisteredServiceAccessStrategy accessStrategy = svc.getAccessStrategy();
        final RegisteredServiceSupportAccessEditBean accessBean = bean.getSupportAccess();
        accessBean.setCasEnabled(accessStrategy.isServiceAccessAllowed());
        accessBean.setSsoEnabled(accessStrategy.isServiceAccessAllowedForSso());

        if (accessStrategy instanceof DefaultRegisteredServiceAccessStrategy) {
            final DefaultRegisteredServiceAccessStrategy def = (DefaultRegisteredServiceAccessStrategy) accessStrategy;
            accessBean.setRequireAll(def.isRequireAllAttributes());
            accessBean.setRequiredAttr(def.getRequiredAttributes());
        }
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

                String salt = new String(sh.getSalt(), Charset.defaultCharset());
                if (Base64.isBase64(salt)) {
                    salt = new String(Base64.decodeBase64(salt));
                }

                uBean.setValue(salt);
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
        /**
         * The Available attributes.
         */
        private List<String> availableAttributes = new ArrayList<>();

        /**
         * Gets available attributes.
         *
         * @return the available attributes
         */
        public List<String> getAvailableAttributes() {
            return availableAttributes;
        }

        /**
         * Sets available attributes.
         *
         * @param availableAttributes the available attributes
         */
        public void setAvailableAttributes(final List<String> availableAttributes) {
            this.availableAttributes = availableAttributes;
        }

    }

    /**
     * The type Service data.
     */
    public static class ServiceData {
        /**
         * The Assigned id.
         */
        private long assignedId;
        /**
         * The Service id.
         */
        private String serviceId;
        /**
         * The Name.
         */
        private String name;
        /**
         * The Description.
         */
        private String description;
        /**
         * The Logo url.
         */
        private String logoUrl;
        /**
         * The Theme.
         */
        private String theme;
        /**
         * The Eval order.
         */
        private int evalOrder = Integer.MIN_VALUE;
        /**
         * The Required handlers.
         */
        private Set<String> requiredHandlers = new HashSet<>();
        /**
         * The Logout url.
         */
        private String logoutUrl;
        /**
         * The Support access.
         */
        private RegisteredServiceSupportAccessEditBean supportAccess = new RegisteredServiceSupportAccessEditBean();
        /**
         * The Type.
         */
        private String type = RegisteredServiceTypeEditBean.CAS.toString();
        /**
         * The Oauth.
         */
        private RegisteredServiceOAuthTypeEditBean oauth = new RegisteredServiceOAuthTypeEditBean();
        /**
         * The Logout type.
         */
        private String logoutType = RegisteredServiceLogoutTypeEditBean.BACK.toString();
        /**
         * The User attr provider.
         */
        private RegisteredServiceUsernameAttributeProviderEditBean userAttrProvider =
                new RegisteredServiceUsernameAttributeProviderEditBean();
        /**
         * The Public key.
         */
        private RegisteredServicePublicKeyEditBean publicKey = new RegisteredServicePublicKeyEditBean();
        /**
         * The Proxy policy.
         */
        private RegisteredServiceProxyPolicyBean proxyPolicy = new RegisteredServiceProxyPolicyBean();
        /**
         * The Attr release.
         */
        private RegisteredServiceAttributeReleasePolicyEditBean attrRelease
                = new RegisteredServiceAttributeReleasePolicyEditBean();

        /**
         * Gets attr release.
         *
         * @return the attr release
         */
        public RegisteredServiceAttributeReleasePolicyEditBean getAttrRelease() {
            return attrRelease;
        }

        /**
         * Sets attr release.
         *
         * @param attrRelease the attr release
         */
        public void setAttrRelease(final RegisteredServiceAttributeReleasePolicyEditBean attrRelease) {
            this.attrRelease = attrRelease;
        }

        /**
         * Gets public key.
         *
         * @return the public key
         */
        public RegisteredServicePublicKeyEditBean getPublicKey() {
            return publicKey;
        }

        /**
         * Sets public key.
         *
         * @param publicKey the public key
         */
        public void setPublicKey(final RegisteredServicePublicKeyEditBean publicKey) {
            this.publicKey = publicKey;
        }

        /**
         * Gets proxy policy.
         *
         * @return the proxy policy
         */
        public RegisteredServiceProxyPolicyBean getProxyPolicy() {
            return proxyPolicy;
        }

        /**
         * Sets proxy policy.
         *
         * @param proxyPolicy the proxy policy
         */
        public void setProxyPolicy(final RegisteredServiceProxyPolicyBean proxyPolicy) {
            this.proxyPolicy = proxyPolicy;
        }

        /**
         * Gets theme.
         *
         * @return the theme
         */
        public String getTheme() {
            return theme;
        }

        /**
         * Sets theme.
         *
         * @param theme the theme
         */
        public void setTheme(final String theme) {
            this.theme = theme;
        }

        /**
         * Gets eval order.
         *
         * @return the eval order
         */
        public int getEvalOrder() {
            return evalOrder;
        }

        /**
         * Sets eval order.
         *
         * @param evalOrder the eval order
         */
        public void setEvalOrder(final int evalOrder) {
            this.evalOrder = evalOrder;
        }

        /**
         * Gets required handlers.
         *
         * @return the required handlers
         */
        public Set<String> getRequiredHandlers() {
            return requiredHandlers;
        }

        /**
         * Sets required handlers.
         *
         * @param requiredHandlers the required handlers
         */
        public void setRequiredHandlers(final Set<String> requiredHandlers) {
            this.requiredHandlers = requiredHandlers;
        }

        /**
         * Gets logout url.
         *
         * @return the logout url
         */
        public String getLogoutUrl() {
            return logoutUrl;
        }

        /**
         * Sets logout url.
         *
         * @param logoutUrl the logout url
         */
        public void setLogoutUrl(final String logoutUrl) {
            this.logoutUrl = logoutUrl;
        }

        /**
         * Gets oauth.
         *
         * @return the oauth
         */
        public RegisteredServiceOAuthTypeEditBean getOauth() {
            return oauth;
        }

        /**
         * Sets oauth.
         *
         * @param oauth the oauth
         */
        public void setOauth(final RegisteredServiceOAuthTypeEditBean oauth) {
            this.oauth = oauth;
        }

        /**
         * Gets logout type.
         *
         * @return the logout type
         */
        public String getLogoutType() {
            return logoutType;
        }

        /**
         * Sets logout type.
         *
         * @param logoutType the logout type
         */
        public void setLogoutType(final String logoutType) {
            this.logoutType = logoutType;
        }

        /**
         * Gets user attr provider.
         *
         * @return the user attr provider
         */
        public RegisteredServiceUsernameAttributeProviderEditBean getUserAttrProvider() {
            return userAttrProvider;
        }

        /**
         * Sets user attr provider.
         *
         * @param userAttrProvider the user attr provider
         */
        public void setUserAttrProvider(final RegisteredServiceUsernameAttributeProviderEditBean userAttrProvider) {
            this.userAttrProvider = userAttrProvider;
        }

        /**
         * Gets type.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets type.
         *
         * @param type the type
         */
        public void setType(final String type) {
            this.type = type;
        }

        /**
         * Gets support access.
         *
         * @return the support access
         */
        public RegisteredServiceSupportAccessEditBean getSupportAccess() {
            return supportAccess;
        }

        /**
         * Sets support access.
         *
         * @param supportAccess the support access
         */
        public void setSupportAccess(final RegisteredServiceSupportAccessEditBean supportAccess) {
            this.supportAccess = supportAccess;
        }

        /**
         * Gets assigned id.
         *
         * @return the assigned id
         */
        public long getAssignedId() {
            return assignedId;
        }

        /**
         * Sets assigned id.
         *
         * @param assignedId the assigned id
         */
        public void setAssignedId(final long assignedId) {
            this.assignedId = assignedId;
        }

        /**
         * Gets service id.
         *
         * @return the service id
         */
        public String getServiceId() {
            return serviceId;
        }

        /**
         * Sets service id.
         *
         * @param serviceId the service id
         */
        public void setServiceId(final String serviceId) {
            this.serviceId = serviceId;
        }

        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets name.
         *
         * @param name the name
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * Gets description.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets description.
         *
         * @param description the description
         */
        public void setDescription(final String description) {
            this.description = description;
        }

        /**
         * Gets logo url.
         *
         * @return the logo url
         */
        public String getLogoUrl() {
            return logoUrl;
        }

        /**
         * Sets logo url.
         *
         * @param logoUrl the logo url
         */
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

                if (this.assignedId <=0) {
                    regSvc.setId(RegisteredService.INITIAL_IDENTIFIER_VALUE);
                } else {
                    regSvc.setId(this.assignedId);
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

                setLogoutTypeAndUrlOnService(regSvc);

                final RegisteredServiceAccessStrategy accessStrategy = regSvc.getAccessStrategy();
                setAccessStrategyOnService((DefaultRegisteredServiceAccessStrategy) accessStrategy);

                setProxyPolicyOnService(regSvc);

                setUsernameAttributeOnService(regSvc);

                if (this.publicKey != null && this.publicKey.isValid()) {
                    final RegisteredServicePublicKey publicKey = new RegisteredServicePublicKeyImpl(
                            this.publicKey.getLocation(), this.publicKey.getAlgorithm());
                    regSvc.setPublicKey(publicKey);
                }

                setAttributeReleasePolicyOnService(dao, regSvc);

                return regSvc;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Sets attribute release policy on service.
         *
         * @param dao the dao
         * @param regSvc the reg svc
         */
        private void setAttributeReleasePolicyOnService(final IPersonAttributeDao dao, final AbstractRegisteredService regSvc) {
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
                policy.setPrincipalAttributesRepository(new CachingPrincipalAttributesRepository(dao,
                        TimeUnit.valueOf(this.attrRelease.getCachedTimeUnit().toUpperCase()),
                        this.attrRelease.getCachedExpiration()));
            } else if (StringUtils.equalsIgnoreCase(attrType,
                    RegisteredServiceAttributeReleasePolicyEditBean.Types.DEFAULT.toString())) {
                policy.setPrincipalAttributesRepository(new DefaultPrincipalAttributesRepository());
            }
            regSvc.setAttributeReleasePolicy(policy);
        }

        /**
         * Sets username attribute on service.
         *
         * @param regSvc the reg svc
         */
        private void setUsernameAttributeOnService(final AbstractRegisteredService regSvc) {
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
        }

        /**
         * Sets proxy policy on service.
         *
         * @param regSvc the reg svc
         */
        private void setProxyPolicyOnService(final AbstractRegisteredService regSvc) {
            final String proxyType = this.proxyPolicy.getType();
            if (StringUtils.equalsIgnoreCase(proxyType,
                    RegisteredServiceProxyPolicyBean.Types.REFUSE.toString())) {
                regSvc.setProxyPolicy(new RefuseRegisteredServiceProxyPolicy());
            } else if (StringUtils.equalsIgnoreCase(proxyType,
                    RegisteredServiceProxyPolicyBean.Types.REGEX.toString())) {
                final String value = this.proxyPolicy.getValue();
                if (StringUtils.isNotBlank(value) && isValidRegex(value)) {
                    regSvc.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy(value));
                } else {
                    throw new IllegalArgumentException("Invalid regex pattern specified for proxy policy: " + value);
                }
            }
        }

        /**
         * Sets access strategy on service.
         *
         * @param accessStrategy the access strategy
         */
        private void setAccessStrategyOnService(final DefaultRegisteredServiceAccessStrategy accessStrategy) {
            accessStrategy
                    .setEnabled(this.supportAccess.isCasEnabled());
            accessStrategy
                    .setSsoEnabled(this.supportAccess.isSsoEnabled());
            accessStrategy
                    .setRequireAllAttributes(this.supportAccess.isRequireAll());

            final Map<String, Set<String>> requiredAttrs = this.supportAccess.getRequiredAttr();
            final Set<Map.Entry<String, Set<String>>> entries = requiredAttrs.entrySet();
            final Iterator<Map.Entry<String, Set<String>>> it = entries.iterator();
            while (it.hasNext()) {
                final Map.Entry<String, Set<String>> entry = it.next();
                if (entry.getValue().isEmpty()) {
                    it.remove();
                }
            }
            accessStrategy
                    .setRequiredAttributes(requiredAttrs);
        }

        /**
         * Sets logout type and url on service.
         *
         * @param regSvc the reg svc
         * @throws MalformedURLException the malformed uRL exception
         */
        private void setLogoutTypeAndUrlOnService(final AbstractRegisteredService regSvc) throws MalformedURLException {
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
        }

        /**
         * Determine service type by pattern.
         *
         * @param serviceId the service id
         * @return the abstract registered service
         */
        private AbstractRegisteredService determineServiceTypeByPattern(final String serviceId) {
            try {
                Pattern.compile(serviceId);
                LOGGER.debug("Service id {} is a valid regex.", serviceId);
                return new RegexRegisteredService();
            } catch (final PatternSyntaxException exception) {
                LOGGER.debug("Service id {} is not a valid regex. Checking ant patterns...", serviceId);
                if (new AntPathMatcher().isPattern(serviceId)) {
                    LOGGER.debug("Service id {} is a valid ant pattern.", serviceId);
                    return new RegisteredServiceImpl();
                }
            }
            throw new RuntimeException("Service id " + serviceId + " cannot be resolve to a service type");
        }

        /**
         * Determine service type by pattern.
         *
         * @param pattern the pattern
         * @return the abstract registered service
         */
        private boolean isValidRegex(final String pattern) {
            try {
                Pattern.compile(serviceId);
                LOGGER.debug("Pattern is a valid regex.", pattern);
                return true;
            } catch (final PatternSyntaxException exception) {
                return false;
            }
        }
    }
}
