package org.apereo.cas.configuration.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jooq.lambda.Unchecked;
import org.reflections.ReflectionUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;

/**
 * This is {@link CasFeatureModule}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface CasFeatureModule {

    private static String getMethodName(final Field field, final String prefix) {
        return prefix
               + field.getName().substring(0, 1).toUpperCase()
               + field.getName().substring(1);
    }

    /**
     * Is defined?
     *
     * @return true/false
     */
    @JsonIgnore
    default boolean isDefined() {
        val fields = ReflectionUtils.getAllFields(getClass(), field -> field.getAnnotation(RequiredProperty.class) != null);
        return fields
            .stream()
            .allMatch(Unchecked.predicate(field -> {
                var getter = getMethodName(field, "get");
                if (ClassUtils.hasMethod(getClass(), getter)) {
                    val method = ClassUtils.getMethod(getClass(), getter);
                    val value = method.invoke(this);
                    return value != null && StringUtils.isNotBlank(value.toString());
                }
                getter = getMethodName(field, "is");
                if (ClassUtils.hasMethod(getClass(), getter)) {
                    val method = ClassUtils.getMethod(getClass(), getter);
                    val value = method.invoke(this);
                    return value != null && BooleanUtils.toBoolean(value.toString());
                }
                return false;
            }));
    }

    /**
     * Is undefined ?.
     *
     * @return true/false
     */
    @JsonIgnore
    default boolean isUndefined() {
        return !isDefined();
    }

    enum FeatureCatalog {
        /**
         * Web flow, actions and event routing core functionality.
         */
        Webflow,
        /**
         * Logout and SLO functionality.
         */
        Logout,
        /**
         * Allow CAS to be discoverable/discovered,
         * and/or integration with service discovery systems.
         */
        Discovery,
        /**
         * Core/baseline functionality
         * that provides ground support for a particular integration.
         */
        Core,
        /**
         * HTTP session management.
         */
        SessionManagement,
        /**
         * JDBC and RDBMS.
         */
        JDBC,
        /**
         * Geo and IP location mapping.
         */
        GeoLocation,
        /**
         * Metrics and statistics.
         */
        Metrics,
        /**
         * Monitoring.
         */
        Monitoring,
        /**
         * CAS configuration and Spring Cloud Config.
         */
        CasConfiguration,
        /**
         * Jetty Webapp configuration.
         */
        Jetty,
        /**
         * Undertow webapp configuration.
         */
        Undertow,
        /**
         * Spring Boot Admin Server.
         */
        SpringBootAdmin,
        /**
         * WebApp and web-related functionality.
         */
        WebApplication,
        /**
         * Apache Tomcat server configuration.
         */
        ApacheTomcat,
        /**
         * Notifications and messaging.
         */
        Notifications,
        /**
         * Protocol validation.
         */
        Validation,
        /**
         * Thymeleaf and view management.
         */
        Thymeleaf,
        /**
         * Token & JWT management.
         */
        Tokens,
        /**
         * WS-federation.
         */
        WsFederation,
        /**
         * SAML functionality.
         */
        SAML,
        /**
         * WS IdP and STS functionality.
         */
        WsFederationIdentityProvider,
        /**
         * Authentication and login.
         */
        Authentication,
        /**
         * MFA.
         */
        MultifactorAuthentication,
        /**
         * MFA trusted devices.
         */
        MultifactorAuthenticationTrustedDevices,
        /**
         * Delegated authn.
         */
        DelegatedAuthentication,
        /**
         * Auditing and audit log.
         */
        Audit,
        /**
         * Authy MFA.
         */
        Authy,
        /**
         * Authentication events.
         */
        Events,
        /**
         * Account management and signup.
         */
        AccountManagement,
        /**
         * AUP feature.
         */
        AcceptableUsagePolicy,
        /**
         * Person directory and attribute resolution feature.
         */
        PersonDirectory,
        /**
         * SPNEGO authentication.
         */
        SPNEGO,
        /**
         * Passwordless authN.
         */
        PasswordlessAuthn,
        /**
         * U2F MFA.
         */
        U2F,
        /**
         * YubiKey MFA.
         */
        YubiKey,
        /**
         * Electrofence adaptive authentication.
         */
        Electrofence,
        /**
         * ACME.
         */
        ACME,
        /**
         * CAPTCHA integrations.
         */
        CAPTCHA,
        /**
         * Forgot/reset username.
         */
        ForgotUsername,
        /**
         * LDAP authentication and general integrations.
         */
        LDAP,
        /**
         * Interrupt notifications.
         */
        InterruptNotifications,
        /**
         * Radius authn.
         */
        Radius,
        /**
         * RADIUS MFA.
         */
        RadiusMFA,
        /**
         * WebAuthn MFA.
         */
        WebAuthn,
        /**
         * Google Auth MFA.
         */
        GoogleAuthenticator,
        /**
         * SCIM Integration.
         */
        SCIM,
        /**
         * Service registry and management.
         */
        ServiceRegistry,
        /**
         * Service registry streaming files and services.
         */
        ServiceRegistryStreaming,
        /**
         * Surrogate Authn.
         */
        SurrogateAuthentication,
        /**
         * SAML IDP.
         */
        SAMLIdentityProvider,
        /**
         * SAML IDP metadata management.
         */
        SAMLIdentityProviderMetadata,
        /**
         * SAML SP metadata management.
         */
        SAMLServiceProviderMetadata,
        /**
         * OAuth.
         */
        OAuth,
        /**
         * OIDC.
         */
        OpenIDConnect,
        /**
         * Authn throttling.
         */
        Throttling,
        /**
         * Password management.
         */
        PasswordManagement,
        /**
         * Password history management for history.
         */
        PasswordManagementHistory,
        /**
         * Ticket registry operations.
         */
        TicketRegistry,
        /**
         * Ticket registry locking operations.
         */
        TicketRegistryLocking,
        /**
         * Attribute consent management.
         */
        Consent,
        /**
         * OAuth user managed access.
         */
        UMA,
        /**
         * REST Protocol.
         */
        RestProtocol,
        /**
         * Simple multifactor authentication.
         */
        SimpleMFA,
        /**
         * X509 authentication.
         */
        X509,
        /**
         * Reports.
         */
        Reports;

        /**
         * To property name.
         *
         * @param module the module
         * @return the string
         */
        public String toProperty(final String module) {
            var propertyName = CasFeatureModule.class.getSimpleName() + '.' + name();
            if (StringUtils.isNotBlank(module)) {
                propertyName += '.' + module;
            }
            propertyName += ".enabled";
            return propertyName;
        }
    }
}
