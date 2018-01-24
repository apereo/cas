package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;

/**
 * The {@link RegisteredServiceProperty} defines a single custom
 * property that is associated with a service.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface RegisteredServiceProperty extends Serializable {

    /**
     * Collection of supported properties that control various functionality in CAS.
     */
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @Getter
    @AllArgsConstructor
    enum RegisteredServiceProperties {

        /**
         * using when delegating authentication to ADFS to indicate the relying party identifier.
         */
        WSFED_RELYING_PARTY_ID("wsfed.relyingPartyIdentifier", StringUtils.EMPTY),
        /**
         * Produce a JWT as a response when generating service tickets.
         *
         * @deprecated Use {@link #TOKEN_AS_SERVICE_TICKET} instead.
         **/
        @Deprecated
        TOKEN_AS_RESPONSE("jwtAsResponse", "true"),
        /**
         * Produce a JWT as a response when generating service tickets.
         **/
        TOKEN_AS_SERVICE_TICKET("jwtAsServiceTicket", "false"),
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
        public RegisteredServiceProperty getPropertyValue(final RegisteredService service) {
            if (isAssignedTo(service)) {
                final Optional<Map.Entry<String, RegisteredServiceProperty>> property = service.getProperties().entrySet()
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
        public <T> T getPropertyValue(final RegisteredService service, final Class<T> clazz) {
            if (isAssignedTo(service)) {
                final RegisteredServiceProperty prop = getPropertyValue(service);
                if (prop != null) {
                    return clazz.cast(prop.getValue());
                }
            }
            return null;
        }

        /**
         * Check to see if the property is assigned to this service and is defined with a value.
         *
         * @param service registered service
         * @return true/false
         */
        public boolean isAssignedTo(final RegisteredService service) {
            return service.getProperties().entrySet().stream()
                .anyMatch(entry -> entry.getKey().equalsIgnoreCase(getPropertyName()) && StringUtils.isNotBlank(entry.getValue().getValue()));
        }
    }

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
    String getValue();

    /**
     * Contains elements?
     *
     * @param value the value
     * @return true/false
     */
    boolean contains(String value);
}
