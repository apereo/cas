package org.jasig.cas.services.web.beans;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.jasig.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.jasig.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.jasig.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.RegisteredServicePublicKey;
import org.jasig.cas.services.RegisteredServicePublicKeyImpl;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.jasig.cas.services.ReturnMappedAttributeReleasePolicy;
import org.jasig.cas.services.TimeBasedRegisteredServiceAccessStrategy;
import org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.jasig.cas.support.oauth.services.OAuthRegisteredCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.springframework.util.AntPathMatcher;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.slf4j.LoggerFactory.getLogger;

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
        private long assignedId;
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

                convertLogoutTypesToService(regSvc);

                if (StringUtils.isNotBlank(this.logoutUrl)) {
                    regSvc.setLogoutUrl(new URL(this.logoutUrl));
                }

                convertAccessStrategyToService(regSvc);

                convertProxyPolicyToService(regSvc);

                convertUsernameAttributeToService(regSvc);

                if (this.publicKey != null && this.publicKey.isValid()) {
                    final RegisteredServicePublicKey publicKey = new RegisteredServicePublicKeyImpl(
                            this.publicKey.getLocation(), this.publicKey.getAlgorithm());
                    regSvc.setPublicKey(publicKey);
                }

                final RegisteredServiceAttributeReleasePolicyStrategyEditBean policyBean =
                        this.attrRelease.getAttrPolicy();
                final String policyType = policyBean.getType();

                AbstractRegisteredServiceAttributeReleasePolicy policy = null;
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
         * Convert proxy policy to service.
         *
         * @param regSvc the reg svc
         */
        private void convertProxyPolicyToService(final AbstractRegisteredService regSvc) {
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
         * Convert username attribute to service.
         *
         * @param regSvc the reg svc
         */
        private void convertUsernameAttributeToService(final AbstractRegisteredService regSvc) {
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
         * Convert access strategy to service.
         *
         * @param regSvc the service
         */
        private void convertAccessStrategyToService(final AbstractRegisteredService regSvc) {


            final TimeBasedRegisteredServiceAccessStrategy accessStrategy =
                    new TimeBasedRegisteredServiceAccessStrategy();

            accessStrategy.setEnabled(this.supportAccess.isCasEnabled());
            accessStrategy.setSsoEnabled(this.supportAccess.isSsoEnabled());
            accessStrategy.setRequireAllAttributes(this.supportAccess.isRequireAll());

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

            accessStrategy.setEndingDateTime(this.supportAccess.getEndingTime());
            accessStrategy.setStartingDateTime(this.supportAccess.getStartingTime());

            regSvc.setAccessStrategy(accessStrategy);

        }

        /**
         * Convert logout types to service.
         *
         * @param regSvc the reg svc
         */
        private void convertLogoutTypesToService(final AbstractRegisteredService regSvc) {
            if (StringUtils.equalsIgnoreCase(this.logoutType,
                    RegisteredServiceLogoutTypeEditBean.BACK.toString())) {
                regSvc.setLogoutType(LogoutType.BACK_CHANNEL);
            } else if (StringUtils.equalsIgnoreCase(this.logoutType,
                    RegisteredServiceLogoutTypeEditBean.FRONT.toString())) {
                regSvc.setLogoutType(LogoutType.FRONT_CHANNEL);
            } else {
                regSvc.setLogoutType(LogoutType.NONE);
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
                LOGGER.debug("Pattern {} is a valid regex.", pattern);
                return true;
            } catch (final PatternSyntaxException exception) {
                return false;
            }
        }
    }
}
