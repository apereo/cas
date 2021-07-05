package org.apereo.cas.services;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

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
public class OidcRegisteredService extends OAuthRegisteredService {

    private static final long serialVersionUID = 1310899699465091444L;

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

    @JsonIgnore
    @Deprecated(since = "6.2.0")
    @Transient
    private transient boolean implicit;

    private ZonedDateTime dynamicRegistrationDateTime;

    private Set<String> scopes = new HashSet<>(0);

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

    /**
     * Gets scopes.
     *
     * @return the scopes
     */
    public Set<String> getScopes() {
        if (this.scopes == null) {
            this.scopes = new HashSet<>(0);
        }
        return scopes;
    }

    /**
     * Sets scopes.
     *
     * @param scopes the scopes
     */
    public void setScopes(final Set<String> scopes) {
        getScopes().clear();
        getScopes().addAll(scopes);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (this.scopes == null) {
            this.scopes = new HashSet<>(0);
        }
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

    @Override
    protected AbstractRegisteredService newInstance() {
        return new OidcRegisteredService();
    }
}
