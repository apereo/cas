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
        WSFED_RELYING_PARTY_ID("wsfed.relyingPartyIdentifier", StringUtils.EMPTY),
        /**
         * Produce a JWT as a response when generating service tickets.
         **/
        TOKEN_AS_SERVICE_TICKET("jwtAsServiceTicket", "false"),
        /**
         * Indicate the cipher strategy for JWT service tickets to determine order of signing/encryption operations.
         **/
        TOKEN_AS_SERVICE_TICKET_CIPHER_STRATEGY_TYPE("jwtAsServiceTicketCipherStrategyType", "ENCRYPT_AND_SIGN"),
        /**
         * Produce a signed JWT as a response when generating service tickets using the provided signing key.
         **/
        TOKEN_AS_SERVICE_TICKET_SIGNING_KEY("jwtAsServiceTicketSigningKey", StringUtils.EMPTY),
        /**
         * Produce an encrypted JWT as a response when generating service tickets using the provided encryption key.
         **/
        TOKEN_AS_SERVICE_TICKET_ENCRYPTION_KEY("jwtAsServiceTicketEncryptionKey", StringUtils.EMPTY),
        /**
         * Produce a signed JWT as a response when generating access tokens using the provided signing key.
         **/
        ACCESS_TOKEN_AS_JWT_SIGNING_KEY("accessTokenAsJwtSigningKey", StringUtils.EMPTY),
        /**
         * Indicate the cipher strategy for JWTs as access tokens, to determine order of signing/encryption operations.
         */
        ACCESS_TOKEN_AS_JWT_CIPHER_STRATEGY_TYPE("accessTokenAsJwtCipherStrategyType", "ENCRYPT_AND_SIGN"),
        /**
         * Enable signing JWTs as a response when generating access tokens using the provided signing key.
         **/
        ACCESS_TOKEN_AS_JWT_SIGNING_ENABLED("accessTokenAsJwtSigningEnabled", "true"),
        /**
         * Enable encryption of JWTs as a response when generating access tokens using the provided encryption key.
         **/
        ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED("accessTokenAsJwtEncryptionEnabled", "false"),
        /**
         * Produce an encrypted JWT as a response when generating access tokens using the provided encryption key.
         **/
        ACCESS_TOKEN_AS_JWT_ENCRYPTION_KEY("accessTokenAsJwtEncryptionKey", StringUtils.EMPTY),
        /**
         * Jwt signing secret defined for a given service.
         **/
        TOKEN_SECRET_SIGNING("jwtSigningSecret", StringUtils.EMPTY),
        /**
         * Jwt signing secret alg defined for a given service.
         **/
        TOKEN_SECRET_SIGNING_ALG("jwtSigningSecretAlg", "HS256"),
        /**
         * Jwt encryption secret defined for a given service.
         **/
        TOKEN_SECRET_ENCRYPTION("jwtEncryptionSecret", StringUtils.EMPTY),
        /**
         * Jwt encryption secret alg defined for a given service.
         **/
        TOKEN_SECRET_ENCRYPTION_ALG("jwtEncryptionSecretAlg", StringUtils.EMPTY),
        /**
         * Jwt encryption secret method defined for a given service.
         **/
        TOKEN_SECRET_ENCRYPTION_METHOD("jwtEncryptionSecretMethod", "A192CBC-HS384"),
        /**
         * Secrets are Base64 encoded.
         **/
        TOKEN_SECRETS_ARE_BASE64_ENCODED("jwtSecretsAreBase64Encoded", "false"),
        /**
         * Whether interrupt notifications should be skipped.
         **/
        SKIP_INTERRUPT_NOTIFICATIONS("skipInterrupt", "false"),
        /**
         * Whether this service should skip qualification for required-service pattern checks.
         **/
        SKIP_REQUIRED_SERVICE_CHECK("skipRequiredServiceCheck", "false"),
        /**
         * Whether CAS should inject cache control headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_CACHE_CONTROL("httpHeaderEnableCacheControl", "true"),
        /**
         * Whether CAS should inject xcontent options headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_XCONTENT_OPTIONS("httpHeaderEnableXContentOptions", "true"),
        /**
         * Whether CAS should inject strict transport security headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_STRICT_TRANSPORT_SECURITY("httpHeaderEnableStrictTransportSecurity", "true"),
        /**
         * Whether CAS should inject xframe options headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_XFRAME_OPTIONS("httpHeaderEnableXFrameOptions", "true"),
        /**
         * Whether CAS should override xframe options headers into the response when this service is in process.
         */
        HTTP_HEADER_XFRAME_OPTIONS("httpHeaderXFrameOptions", "DENY"),
        /**
         * Whether CAS should inject content security policy headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_CONTENT_SECURITY_POLICY("httpHeaderEnableContentSecurityPolicy", "true"),
        /**
         * Whether CAS should inject xss protection headers into the response when this service is in process.
         */
        HTTP_HEADER_ENABLE_XSS_PROTECTION("httpHeaderEnableXSSProtection", "true");

        private final String propertyName;

        private final String defaultValue;

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
            return service.getProperties().entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().equalsIgnoreCase(getPropertyName())
                    && StringUtils.isNotBlank(entry.getValue().getValue())
                    && valueFilter.test(entry.getValue().getValue()));
        }
    }
}
