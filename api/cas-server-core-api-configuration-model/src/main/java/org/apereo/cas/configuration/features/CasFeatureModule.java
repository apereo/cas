package org.apereo.cas.configuration.features;

import org.apereo.cas.configuration.support.RequiredProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is {@link CasFeatureModule}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface CasFeatureModule {
    private static String getMethodName(final Field field, final String prefix) {
        return prefix
            + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH)
            + field.getName().substring(1);
    }

    /**
     * Is defined?
     *
     * @return true/false
     */
    @JsonIgnore
    default boolean isDefined() {
        val fields = new HashSet<Field>();
        ReflectionUtils.doWithFields(getClass(), fields::add, field -> AnnotatedElementUtils.isAnnotated(field, RequiredProperty.class));
        return fields
            .stream()
            .allMatch(Unchecked.predicate(field -> {
                var getter = getMethodName(field, "get");
                if (ClassUtils.hasMethod(getClass(), getter)) {
                    val method = ClassUtils.getMethod(getClass(), getter);
                    method.setAccessible(true);
                    val value = method.invoke(this);
                    return value != null && StringUtils.isNotBlank(value.toString());
                }
                getter = getMethodName(field, "is");
                if (ClassUtils.hasMethod(getClass(), getter)) {
                    val method = ClassUtils.getMethod(getClass(), getter);
                    method.setAccessible(true);
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
         * Multitenancy support.
         */
        Multitenancy,
        /**
         * Authorization services and API access management.
         */
        Authorization,
        /**
         * Dashboard and administrative console
         * to manage CAS services, configuration, etc.
         */
        Palantir,
        /**
         * Scripting and Groovy support.
         */
        Scripting,
        /**
         * Just-in-time provisioning users to external systems
         * and identity management solutions.
         */
        Provisioning,
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
         * Token and JWT management.
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
         * Logging support for various providers and platforms.
         */
        Logging,
        /**
         * Authentication events.
         */
        Events,
        /**
         * Account management and profile.
         */
        AccountManagement,

        /**
         * Account management and signup.
         */
        AccountRegistration,

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

        private static final Set<String> PRESENT_FEATURES = Collections.synchronizedSet(new TreeSet<>());

        public static Set<String> getRegisteredFeatures() {
            return new TreeSet<>(PRESENT_FEATURES);
        }

        /**
         * Register and keep track of features that are present at runtime.
         */
        public void register() {
            register(StringUtils.EMPTY);
        }

        /**
         * Register.
         *
         * @param module the module
         */
        public void register(final String module) {
            PRESENT_FEATURES.add(toFullFeatureName(module));
        }

        /**
         * Is registered?
         *
         * @param module the module
         * @return true/false
         */
        public boolean isRegistered(final String module) {
            return PRESENT_FEATURES.contains(toFullFeatureName(module));
        }

        /**
         * Is registered?
         *
         * @return true/false
         */
        public boolean isRegistered() {
            return isRegistered(StringUtils.EMPTY);
        }

        /**
         * To property name.
         *
         * @param module the module
         * @return the string
         */
        public String toProperty(final String module) {
            return toFullFeatureName(module) + ".enabled";
        }

        private String toFullFeatureName(final String module) {
            var propertyName = CasFeatureModule.class.getSimpleName() + '.' + name();
            if (StringUtils.isNotBlank(module)) {
                propertyName += '.' + module;
            }
            return propertyName;
        }
    }

    /**
     * Baseline set of features that must be enabled and present.
     *
     * @return the set
     */
    static Set<FeatureCatalog> baseline() {
        return Set.of(
            FeatureCatalog.ApacheTomcat,
            FeatureCatalog.Audit,
            FeatureCatalog.Authentication,
            FeatureCatalog.CasConfiguration,
            FeatureCatalog.Core,
            FeatureCatalog.Logout,
            FeatureCatalog.Monitoring,
            FeatureCatalog.MultifactorAuthentication,
            FeatureCatalog.Notifications,
            FeatureCatalog.PasswordManagement,
            FeatureCatalog.PersonDirectory,
            FeatureCatalog.ServiceRegistry,
            FeatureCatalog.Thymeleaf,
            FeatureCatalog.TicketRegistry,
            FeatureCatalog.Validation,
            FeatureCatalog.WebApplication,
            FeatureCatalog.Webflow
        );
    }
}
