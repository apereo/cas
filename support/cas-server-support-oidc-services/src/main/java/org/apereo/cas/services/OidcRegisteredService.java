package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;

import java.io.Serial;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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

    private long jwksCacheDuration;

    private String jwksCacheTimeUnit;

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

    private boolean dynamicallyRegistered;

    private long clientSecretExpiration;

    @JsonIgnore
    @Deprecated(since = "6.2.0")
    @Transient
    private transient boolean implicit;

    private ZonedDateTime dynamicRegistrationDateTime;


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

    /**
     * Indicates the service was dynamically registered.
     * Records the registration time automatically.
     *
     * @param dynamicallyRegistered dynamically registered.
     */
    public void setDynamicallyRegistered(final boolean dynamicallyRegistered) {
        if (dynamicallyRegistered && !this.dynamicallyRegistered && dynamicRegistrationDateTime == null) {
            setDynamicRegistrationDateTime(ZonedDateTime.now(ZoneOffset.UTC));
        }
        this.dynamicallyRegistered = dynamicallyRegistered;
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
}
