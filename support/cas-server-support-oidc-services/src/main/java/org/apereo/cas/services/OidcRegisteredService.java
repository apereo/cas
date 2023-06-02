package org.apereo.cas.services;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

    @Serial
    private static final long serialVersionUID = 1310899699465091444L;

    @ExpressionLanguageCapable
    private String jwks;

    private String jwksKeyId;

    @DurationCapable
    private String jwksCacheDuration;

    private String tokenEndpointAuthenticationMethod = "client_secret_basic";

    private boolean signIdToken = true;

    private boolean encryptIdToken;

    private String idTokenEncryptionAlg;

    private String idTokenSigningAlg;

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

    /**
     * Gets subject type.
     *
     * @return the subject type
     */
    public String getSubjectType() {
        if (StringUtils.isBlank(this.subjectType)) {
            return OidcSubjectTypes.PUBLIC.getType();
        }
        return subjectType;
    }

    @JsonIgnore
    @Override
    public int getEvaluationPriority() {
        return 1;
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "OpenID Connect Relying Party";
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
