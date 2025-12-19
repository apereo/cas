package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.OptBoolean;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link OidcRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class OidcRegisteredService extends OAuthRegisteredService {
    /**
     * Friendly name for this OAuth service.
     */
    public static final String OIDC_REGISTERED_SERVICE_FRIENDLY_NAME = "OpenID Connect Relying Party";

    @Serial
    private static final long serialVersionUID = 1310899699465091444L;
    
    @ExpressionLanguageCapable
    private String jwks;

    @JacksonInject(value = "jwksKeyId", optional = OptBoolean.TRUE)
    private String jwksKeyId;

    @DurationCapable
    private String jwksCacheDuration;

    @JacksonInject(value = "signIdToken", optional = OptBoolean.TRUE)
    private boolean signIdToken = true;

    @JacksonInject(value = "encryptIdToken", optional = OptBoolean.TRUE)
    private boolean encryptIdToken;

    @JacksonInject(value = "includeIdTokenClaims", optional = OptBoolean.TRUE)
    private boolean includeIdTokenClaims;

    @JacksonInject(value = "idTokenEncryptionOptional", optional = OptBoolean.TRUE)
    private boolean idTokenEncryptionOptional;

    @JacksonInject(value = "idTokenEncryptionAlg", optional = OptBoolean.TRUE)
    private String idTokenEncryptionAlg;

    @JacksonInject(value = "idTokenSigningAlg", optional = OptBoolean.TRUE)
    private String idTokenSigningAlg;

    @JacksonInject(value = "userInfoSigningAlg", optional = OptBoolean.TRUE)
    private String userInfoSigningAlg;

    private String userInfoEncryptedResponseAlg;

    private String userInfoEncryptedResponseEncoding;

    private String idTokenEncryptionEncoding;

    private String idTokenIssuer;

    private String sectorIdentifierUri;

    private String applicationType = "web";

    private String subjectType = OidcSubjectTypes.PUBLIC.getType();

    private long clientSecretExpiration;

    private RegisteredServiceOidcIdTokenExpirationPolicy idTokenExpirationPolicy;

    @JacksonInject(value = "backchannelTokenDeliveryMode", optional = OptBoolean.TRUE)
    private String backchannelTokenDeliveryMode = OidcBackchannelTokenDeliveryModes.POLL.getMode();
    
    private String backchannelClientNotificationEndpoint;

    @JacksonInject(value = "backchannelAuthenticationRequestSigningAlg", optional = OptBoolean.TRUE)
    private String backchannelAuthenticationRequestSigningAlg;

    private boolean backchannelUserCodeParameterSupported;
    
    /**
     * Gets subject type.
     *
     * @return the subject type
     */
    public String getSubjectType() {
        return StringUtils.defaultIfBlank(subjectType, OidcSubjectTypes.PUBLIC.getType());
    }

    @JsonIgnore
    @Override
    public int getEvaluationPriority() {
        return 1;
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return OIDC_REGISTERED_SERVICE_FRIENDLY_NAME;
    }

    /**
     * Mark the service as one that is as dynamically registered
     * via the OIDC dynamic registration flow.
     * This operation will assign specific properties
     * to the service definition to carry the registration signal/data.
     */
    @JsonIgnore
    public void markAsDynamicallyRegistered() {
        getProperties().put(RegisteredServiceProperties.OIDC_DYNAMIC_CLIENT_REGISTRATION.getPropertyName(),
            new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));
        getProperties().put(RegisteredServiceProperties.OIDC_DYNAMIC_CLIENT_REGISTRATION_DATE.getPropertyName(),
            new DefaultRegisteredServiceProperty(LocalDateTime.now(ZoneOffset.UTC).toString()));
    }
}
