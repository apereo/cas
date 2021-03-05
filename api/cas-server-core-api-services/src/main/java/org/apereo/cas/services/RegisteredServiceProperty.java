package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The {@link RegisteredServiceProperty} defines a single custom
 * property that is associated with a service.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceProperty extends Serializable {

    /**
     * Gets values.
     *
     * @return the values
     */
    Set<String> getValues();

    /**
     * Gets the first single value.
     *
     * @return the value, or null if the collection is empty.
     */
    @JsonIgnore
    String getValue();

    /**
     * Gets property value.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the property value
     */
    @JsonIgnore
    default <T> T getValue(final Class<T> clazz) {
        val value = getValue();
        if (StringUtils.isNotBlank(value)) {
            return clazz.cast(value);
        }
        return null;
    }

    /**
     * Contains elements?
     *
     * @param value the value
     * @return true/false
     */
    boolean contains(String value);

    /**
     * Gets property value.
     *
     * @return the property value
     */
    @JsonIgnore
    default boolean getBooleanValue() {
        val value = getValue();
        if (StringUtils.isNotBlank(value)) {
            return BooleanUtils.toBoolean(value);
        }
        return false;
    }

    /**
     * Indicates the group for each property.
     */
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @Getter
    @RequiredArgsConstructor
    enum RegisteredServicePropertyGroups {
        CORS,
        DELEGATED_AUTHN_SAML2,
        DELEGATED_AUTHN_WSFED,
        DELEGATED_AUTHN_OIDC,
        HTTP_HEADERS,
        INTERRUPTS,
        JWT_AUTHENTICATION,
        JWT_ACCESS_TOKENS,
        JWT_TOKENS,
        JWT_SERVICE_TICKETS,
        REGISTERED_SERVICES,
        SCIM
    }

    /**
     * Indicates the property type for each property.
     */
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @Getter
    @RequiredArgsConstructor
    enum RegisteredServicePropertyTypes {
        SET,
        STRING,
        INTEGER,
        BOOLEAN,
        LONG
    }

    /**
     * Collection of supported properties that
     * control various functionality in CAS.
     */
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @Getter
    @RequiredArgsConstructor
    enum RegisteredServiceProperties {
        /**
         * used when delegating authentication to ADFS to indicate the relying party identifier.
         */
        WSFED_RELYING_PARTY_ID("wsfed.relyingPartyIdentifier", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_WSFED, RegisteredServicePropertyTypes.STRING),
        /**
         * Produce a JWT as a response when generating service tickets.
         **/
        TOKEN_AS_SERVICE_TICKET("jwtAsServiceTicket", "false",
            RegisteredServicePropertyGroups.JWT_SERVICE_TICKETS, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Indicate the cipher strategy for JWT service tickets to determine order of signing/encryption operations.
         **/
        TOKEN_AS_SERVICE_TICKET_CIPHER_STRATEGY_TYPE("jwtAsServiceTicketCipherStrategyType", "ENCRYPT_AND_SIGN",
            RegisteredServicePropertyGroups.JWT_SERVICE_TICKETS, RegisteredServicePropertyTypes.STRING),
        /**
         * Produce a signed JWT as a response when generating service tickets using the provided signing key.
         **/
        TOKEN_AS_SERVICE_TICKET_SIGNING_KEY("jwtAsServiceTicketSigningKey", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.JWT_SERVICE_TICKETS, RegisteredServicePropertyTypes.STRING),
        /**
         * Produce an encrypted JWT as a response when generating service tickets using the provided encryption key.
         **/
        TOKEN_AS_SERVICE_TICKET_ENCRYPTION_KEY("jwtAsServiceTicketEncryptionKey", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.JWT_SERVICE_TICKETS, RegisteredServicePropertyTypes.STRING),
        /**
         * Produce a signed JWT as a response when generating access tokens using the provided signing key.
         **/
        ACCESS_TOKEN_AS_JWT_SIGNING_KEY("accessTokenAsJwtSigningKey", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.JWT_ACCESS_TOKENS, RegisteredServicePropertyTypes.STRING),
        /**
         * Indicate the cipher strategy for JWTs as access tokens, to determine order of signing/encryption operations.
         */
        ACCESS_TOKEN_AS_JWT_CIPHER_STRATEGY_TYPE("accessTokenAsJwtCipherStrategyType", "ENCRYPT_AND_SIGN",
            RegisteredServicePropertyGroups.JWT_ACCESS_TOKENS, RegisteredServicePropertyTypes.STRING),
        /**
         * Enable signing JWTs as a response when generating access tokens using the provided signing key.
         **/
        ACCESS_TOKEN_AS_JWT_SIGNING_ENABLED("accessTokenAsJwtSigningEnabled", "true",
            RegisteredServicePropertyGroups.JWT_ACCESS_TOKENS, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Enable encryption of JWTs as a response when generating access tokens using the provided encryption key.
         **/
        ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED("accessTokenAsJwtEncryptionEnabled", "false",
            RegisteredServicePropertyGroups.JWT_ACCESS_TOKENS, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Produce an encrypted JWT as a response when generating access tokens using the provided encryption key.
         **/
        ACCESS_TOKEN_AS_JWT_ENCRYPTION_KEY("accessTokenAsJwtEncryptionKey", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.JWT_ACCESS_TOKENS, RegisteredServicePropertyTypes.STRING),
        /**
         * Jwt signing secret defined for a given service.
         **/
        TOKEN_SECRET_SIGNING("jwtSigningSecret", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.JWT_AUTHENTICATION, RegisteredServicePropertyTypes.STRING),
        /**
         * Jwt signing secret alg defined for a given service.
         **/
        TOKEN_SECRET_SIGNING_ALG("jwtSigningSecretAlg", "HS256",
            RegisteredServicePropertyGroups.JWT_AUTHENTICATION, RegisteredServicePropertyTypes.STRING),
        /**
         * Jwt encryption secret defined for a given service.
         **/
        TOKEN_SECRET_ENCRYPTION("jwtEncryptionSecret", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.JWT_AUTHENTICATION, RegisteredServicePropertyTypes.STRING),
        /**
         * Jwt encryption secret alg defined for a given service.
         **/
        TOKEN_SECRET_ENCRYPTION_ALG("jwtEncryptionSecretAlg", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.JWT_AUTHENTICATION, RegisteredServicePropertyTypes.STRING),
        /**
         * Jwt encryption secret method defined for a given service.
         **/
        TOKEN_SECRET_ENCRYPTION_METHOD("jwtEncryptionSecretMethod", "A192CBC-HS384",
            RegisteredServicePropertyGroups.JWT_AUTHENTICATION, RegisteredServicePropertyTypes.STRING),
        /**
         * Secrets are Base64 encoded.
         **/
        TOKEN_SECRETS_ARE_BASE64_ENCODED("jwtSecretsAreBase64Encoded", "false",
            RegisteredServicePropertyGroups.JWT_AUTHENTICATION, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Whether interrupt notifications should be skipped.
         **/
        SKIP_INTERRUPT_NOTIFICATIONS("skipInterrupt", "false",
            RegisteredServicePropertyGroups.INTERRUPTS, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Whether this service should skip qualification for required-service pattern checks.
         **/
        SKIP_REQUIRED_SERVICE_CHECK("skipRequiredServiceCheck", "false",
            RegisteredServicePropertyGroups.REGISTERED_SERVICES, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Whether CAS should inject cache control headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_CACHE_CONTROL("httpHeaderEnableCacheControl", "true",
            RegisteredServicePropertyGroups.HTTP_HEADERS, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Whether CAS should inject xcontent options headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_XCONTENT_OPTIONS("httpHeaderEnableXContentOptions", "true",
            RegisteredServicePropertyGroups.HTTP_HEADERS, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Whether CAS should inject strict transport security headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_STRICT_TRANSPORT_SECURITY("httpHeaderEnableStrictTransportSecurity", "true",
            RegisteredServicePropertyGroups.HTTP_HEADERS, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Whether CAS should inject xframe options headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_XFRAME_OPTIONS("httpHeaderEnableXFrameOptions", "true",
            RegisteredServicePropertyGroups.HTTP_HEADERS, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Whether CAS should override xframe options headers into the response when this service is in process.
         */
        HTTP_HEADER_XFRAME_OPTIONS("httpHeaderXFrameOptions", "DENY",
            RegisteredServicePropertyGroups.HTTP_HEADERS, RegisteredServicePropertyTypes.STRING),
        /**
         * Whether CAS should inject content security policy headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_CONTENT_SECURITY_POLICY("httpHeaderEnableContentSecurityPolicy", "true",
            RegisteredServicePropertyGroups.HTTP_HEADERS, RegisteredServicePropertyTypes.STRING),
        /**
         * Whether CAS should inject xss protection headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_XSS_PROTECTION("httpHeaderEnableXSSProtection", "true",
            RegisteredServicePropertyGroups.HTTP_HEADERS, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Whether CAS should allow credentials in CORS requests.
         */
        CORS_ALLOW_CREDENTIALS("corsAllowCredentials", "false",
            RegisteredServicePropertyGroups.CORS, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Define the max-age property for CORS requests.
         */
        CORS_MAX_AGE("corsMaxAge", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.CORS, RegisteredServicePropertyTypes.INTEGER),
        /**
         * Define allowed origins for CORS requests. Cannot use * when credentials are allowed.
         */
        CORS_ALLOWED_ORIGINS("corsAllowedOrigins", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.CORS, RegisteredServicePropertyTypes.STRING),
        /**
         * Define patterns of allowed origins for CORS requests. (e.g.
         * 'https://*.example.com') Patterns can be used when credentials are allowed.
         */
        CORS_ALLOWED_ORIGIN_PATTERNS("corsAllowedOriginPatterns", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.CORS, RegisteredServicePropertyTypes.STRING),
        /**
         * Define allowed methods for CORS requests.
         */
        CORS_ALLOWED_METHODS("corsAllowedMethods", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.CORS, RegisteredServicePropertyTypes.STRING),
        /**
         * Define allowed headers for CORS requests.
         */
        CORS_ALLOWED_HEADERS("corsAllowedHeaders", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.CORS, RegisteredServicePropertyTypes.STRING),
        /**
         * Define exposed headers in the response for CORS requests.
         */
        CORS_EXPOSED_HEADERS("corsExposedHeaders", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.CORS, RegisteredServicePropertyTypes.STRING),
        /**
         * Indicate binding type, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_AUTHN_REQUEST_BINDING_TYPE("AuthnRequestBindingType", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.STRING),
        /**
         * Indicate assertion consumer service index, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_ASSERTION_CONSUMER_SERVICE_INDEX("AssertionConsumerServiceIndex", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.LONG),
        /**
         * Indicate attribute consuming service index, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_ATTRIBUTE_CONSUMING_SERVICE_INDEX("AttributeConsumingServiceIndex", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.LONG),
        /**
         * Indicate comparison type when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_COMPARISON_TYPE("ComparisonType", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.STRING),
        /**
         * Indicate name id policy format, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_NAME_ID_POLICY_FORMAT("NameIdPolicyFormat", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.STRING),
        /**
         * Indicate name id policy allow create, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_NAME_ID_POLICY_ALLOW_CREATE("NameIdPolicyAllowCreate", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Indicate provider name, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_PROVIDER_NAME("ProviderName", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.STRING),
        /**
         * Indicate issuer format, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_ISSUER_FORMAT("IssuerFormat", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.STRING),
        /**
         * Indicate whether name qualifier should be used, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_USE_NAME_QUALIFIER("UseNameQualifier", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Indicate authn context class refs, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_AUTHN_CONTEXT_CLASS_REFS("AuthnContextClassRefs", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.SET),
        /**
         * Indicate the name id attribute when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_NAME_ID_ATTRIBUTE("NameIdAttribute", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.STRING),
        /**
         * Indicate whether assertions should be signed, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_WANTS_ASSERTIONS_SIGNED("WantsAssertionsSigned", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Indicate whether responses should be signed, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_WANTS_RESPONSES_SIGNED("WantsResponsesSigned", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.BOOLEAN),
        /**
         * Indicate the maximum authentication lifetime to use, when using delegated authentication to saml2 identity providers.
         */
        DELEGATED_AUTHN_SAML2_MAXIMUM_AUTHN_LIFETIME("MaximumAuthenticationLifetime", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_SAML2, RegisteredServicePropertyTypes.LONG),
        /**
         * Indicate {@code max_age} to use, when using delegated authentication to OIDC OP.
         */
        DELEGATED_AUTHN_OIDC_MAX_AGE("max_age", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_OIDC, RegisteredServicePropertyTypes.INTEGER),
        /**
         * Indicate {@code scope} to use, when using delegated authentication to OIDC OP.
         */
        DELEGATED_AUTHN_OIDC_SCOPE("scope", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_OIDC, RegisteredServicePropertyTypes.STRING),
        /**
         * Indicate {@code response_type} to use, when using delegated authentication to OIDC OP.
         */
        DELEGATED_AUTHN_OIDC_RESPONSE_TYPE("response_type", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_OIDC, RegisteredServicePropertyTypes.STRING),
        /**
         * Indicate {@code response_mode} to use, when using delegated authentication to OIDC OP.
         */
        DELEGATED_AUTHN_OIDC_RESPONSE_MODE("response_mode", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.DELEGATED_AUTHN_OIDC, RegisteredServicePropertyTypes.STRING),
        /**
         * Define SCIM oauth token.
         */
        SCIM_OAUTH_TOKEN("scimOAuthToken", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.SCIM, RegisteredServicePropertyTypes.STRING),
        /**
         * Define SCIM username.
         */
        SCIM_USERNAME("scimUsername", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.SCIM, RegisteredServicePropertyTypes.STRING),
        /**
         * Define SCIM password.
         */
        SCIM_PASSWORD("scimPassword", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.SCIM, RegisteredServicePropertyTypes.STRING),
        /**
         * Define SCIM target.
         */
        SCIM_TARGET("scimTarget", StringUtils.EMPTY,
            RegisteredServicePropertyGroups.SCIM, RegisteredServicePropertyTypes.STRING);

        private final String propertyName;

        private final String defaultValue;

        private final RegisteredServicePropertyGroups group;

        private final RegisteredServicePropertyTypes type;

        /**
         * Does property belong to the requested group?
         *
         * @param group the group
         * @return the boolean
         */
        @JsonIgnore
        public boolean isMemberOf(final RegisteredServicePropertyGroups group) {
            return this.group == group;
        }

        /**
         * Gets property value.
         *
         * @param service the service
         * @return the property value
         */
        @JsonIgnore
        public RegisteredServiceProperty getPropertyValue(final RegisteredService service) {
            if (isAssignedTo(service)) {
                val property = service.getProperties().entrySet()
                    .stream().filter(entry -> entry.getKey().equalsIgnoreCase(getPropertyName())
                        && StringUtils.isNotBlank(entry.getValue().getValue()))
                    .distinct().findFirst();
                if (property.isPresent()) {
                    return property.get().getValue();
                }
            }
            return null;
        }

        /**
         * Gets property value.
         *
         * @param <T>     the type parameter
         * @param service the service
         * @param clazz   the clazz
         * @return the property value
         */
        @JsonIgnore
        public <T> T getPropertyValue(final RegisteredService service, final Class<T> clazz) {
            if (isAssignedTo(service)) {
                val prop = getPropertyValue(service);
                if (prop != null) {
                    return clazz.cast(prop.getValue());
                }
            }
            return null;
        }

        /**
         * Gets property values.
         *
         * @param <T>     the type parameter
         * @param service the service
         * @param clazz   the clazz
         * @return the property value
         */
        @JsonIgnore
        public <T> T getPropertyValues(final RegisteredService service, final Class<T> clazz) {
            if (isAssignedTo(service)) {
                val prop = getPropertyValue(service);
                if (prop != null) {
                    return clazz.cast(prop.getValues());
                }
            }
            return null;
        }

        /**
         * Gets property integer value.
         *
         * @param service the service
         * @return the property integer value
         */
        @JsonIgnore
        public int getPropertyIntegerValue(final RegisteredService service) {
            if (isAssignedTo(service)) {
                val prop = getPropertyValue(service);
                if (prop != null) {
                    return Integer.parseInt(prop.getValue());
                }
            }
            return Integer.MIN_VALUE;
        }

        /**
         * Gets property long value.
         *
         * @param service the service
         * @return the property long value
         */
        @JsonIgnore
        public long getPropertyLongValue(final RegisteredService service) {
            if (isAssignedTo(service)) {
                val prop = getPropertyValue(service);
                if (prop != null) {
                    return Long.parseLong(prop.getValue());
                }
            }
            return Long.MIN_VALUE;
        }

        /**
         * Gets property double value.
         *
         * @param service the service
         * @return the property double value
         */
        @JsonIgnore
        public double getPropertyDoubleValue(final RegisteredService service) {
            if (isAssignedTo(service)) {
                val prop = getPropertyValue(service);
                if (prop != null) {
                    return Double.parseDouble(prop.getValue());
                }
            }
            return Double.NaN;
        }

        /**
         * Gets property boolean value.
         *
         * @param service the service
         * @return the property boolean value
         */
        @JsonIgnore
        public boolean getPropertyBooleanValue(final RegisteredService service) {
            if (isAssignedTo(service)) {
                val prop = getPropertyValue(service);
                if (prop != null) {
                    return BooleanUtils.toBoolean(prop.getValue());
                }
            }
            return BooleanUtils.toBoolean(getDefaultValue());
        }

        /**
         * Check to see if the property is assigned to this service and is defined with a value.
         *
         * @param service registered service
         * @return true/false
         */
        @JsonIgnore
        public boolean isAssignedTo(final RegisteredService service) {
            return isAssignedTo(service, s -> true);
        }

        /**
         * Is assigned to value.
         *
         * @param service     the service
         * @param valueFilter the filter
         * @return true/false
         */
        @JsonIgnore
        public boolean isAssignedTo(final RegisteredService service, final Predicate<String> valueFilter) {
            return service != null && service.getProperties().entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().equalsIgnoreCase(getPropertyName())
                    && StringUtils.isNotBlank(entry.getValue().getValue())
                    && valueFilter.test(entry.getValue().getValue()));
        }

        /**
         * Gets typed property value.
         *
         * @param registeredService the registered service
         * @return the typed property value
         */
        public Object getTypedPropertyValue(final RegisteredService registeredService) {
            switch (getType()) {
                case SET:
                    return getPropertyValues(registeredService, Set.class);
                case INTEGER:
                    return getPropertyIntegerValue(registeredService);
                case LONG:
                    return getPropertyLongValue(registeredService);
                case BOOLEAN:
                    return getPropertyBooleanValue(registeredService);
                default:
                    return getPropertyValue(registeredService).getValue();
            }
        }
    }
}
