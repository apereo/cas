package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Predicates;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
    String getValue();

    /**
     * Contains elements?
     *
     * @param value the value
     * @return true/false
     */
    boolean contains(String value);

    /**
     * Collection of supported properties that control various functionality in CAS.
     */
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @Getter
    @RequiredArgsConstructor
    enum RegisteredServiceProperties {
        /**
         * using when delegating authentication to ADFS to indicate the relying party identifier.
         */
        WSFED_RELYING_PARTY_ID("wsfed.relyingPartyIdentifier", StringUtils.EMPTY),
        /**
         * Produce a JWT as a response when generating service tickets.
         **/
        TOKEN_AS_SERVICE_TICKET("jwtAsServiceTicket", "false"),
        /**
         * Produce a signed JWT as a response when generating service tickets using the provided signing key.
         **/
        TOKEN_AS_SERVICE_TICKET_SIGNING_KEY("jwtAsServiceTicketSigningKey", StringUtils.EMPTY),
        /**
         * Produce an encrypted JWT as a response when generating service tickets using the provided encryption key.
         **/
        TOKEN_AS_SERVICE_TICKET_ENCRYPTION_KEY("jwtAsServiceTicketEncryptionKey", StringUtils.EMPTY),
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
         * Check to see if the property is assigned to this service and is defined with a value.
         *
         * @param service registered service
         * @return true/false
         */
        public boolean isAssignedTo(final RegisteredService service) {
            return isAssignedTo(service, Predicates.alwaysTrue());
        }

        /**
         * Is assigned to value.
         *
         * @param service     the service
         * @param valueFilter the filter
         * @return true/false
         */
        public boolean isAssignedTo(final RegisteredService service, final Predicate<String> valueFilter) {
            return service.getProperties().entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().equalsIgnoreCase(getPropertyName())
                    && StringUtils.isNotBlank(entry.getValue().getValue())
                    && valueFilter.test(entry.getValue().getValue()));
        }
    }
}
