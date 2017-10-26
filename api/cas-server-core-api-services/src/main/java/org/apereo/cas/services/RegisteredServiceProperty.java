package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.BooleanUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    enum RegisteredServiceProperties {

        /**
         * using when delegating authentication to ADFS to indicate the relying party identifier.
         */
        WSFED_RELYING_PARTY_ID("wsfed.relyingPartyIdentifier"),
        /**
         * Produce a JWT as a response when generating service tickets.
         * @deprecated Use {@link #TOKEN_AS_SERVICE_TICKET} instead.
         **/
        @Deprecated
        TOKEN_AS_RESOPONSE("jwtAsResponse"),

        /**
         * Produce a JWT as a response when generating service tickets.
         **/
        TOKEN_AS_SERVICE_TICKET("jwtAsServiceTicket"),

        /**
         * Produce a JWT as a response when generating ticket-granting tickets.
         **/
        TOKEN_AS_TICKET_GRANTING_TICKET("jwtAsTicketGrantingTicket"),
        
        /**
         * Jwt signing secret defined for a given service.
         **/
        TOKEN_SECRET_SIGNING("jwtSigningSecret"),

        /**
         * Jwt signing secret alg defined for a given service.
         **/
        TOKEN_SECRET_SIGNING_ALG("jwtSigningSecretAlg"),

        /**
         * Jwt encryption secret defined for a given service.
         **/
        TOKEN_SECRET_ENCRYPTION("jwtEncryptionSecret"),

        /**
         * Jwt encryption secret alg defined for a given service.
         **/
        TOKEN_SECRET_ENCRYPTION_ALG("jwtEncryptionSecretAlg"),

        /**
         * Jwt encryption secret method defined for a given service.
         **/
        TOKEN_SECRET_ENCRYPTION_METHOD("jwtEncryptionSecretMethod"),

        /**
         * Secrets are Base64 encoded.
         **/
        TOKEN_SECRETS_ARE_BASE64_ENCODED("jwtSecretsAreBase64Encoded");

        private final String propertyName;

        RegisteredServiceProperties(final String name) {
            this.propertyName = name;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public RegisteredServiceProperty getPropertyValue(final RegisteredService service) {
            if (isAssignedTo(service)) {
                final Optional<Map.Entry<String, RegisteredServiceProperty>> property = service.getProperties()
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase(getPropertyName()) && BooleanUtils.toBoolean(entry.getValue().getValue()))
                        .distinct()
                        .findFirst();
                if (property.isPresent()) {
                    return property.get().getValue();
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
            return service.getProperties()
                    .entrySet()
                    .stream()
                    .anyMatch(entry -> entry.getKey().equalsIgnoreCase(getPropertyName()) && BooleanUtils.toBoolean(entry.getValue().getValue()));
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
