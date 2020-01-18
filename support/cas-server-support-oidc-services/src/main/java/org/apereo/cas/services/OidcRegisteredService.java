package org.apereo.cas.services;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

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
@Entity
@DiscriminatorValue("oidc")
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OidcRegisteredService extends OAuthRegisteredService {

    private static final long serialVersionUID = 1310899699465091444L;

    @Column
    private String jwks;

    @Column
    private long jwksCacheDuration;

    @Column
    private String jwksCacheTimeUnit;

    @Column(name = "token_auth_method")
    private String tokenEndpointAuthenticationMethod = "client_secret_basic";

    @Column
    private boolean signIdToken = true;

    @Column
    private boolean encryptIdToken;

    @Column
    private String idTokenEncryptionAlg;

    @Column
    private String idTokenSigningAlg;

    @Column
    private String userInfoSigningAlg;

    @Column(name = "userinfo_enc_alg")
    private String userInfoEncryptedResponseAlg;

    @Column(name = "userinfo_enc_enc")
    private String userInfoEncryptedResponseEncoding;

    @Column
    private String idTokenEncryptionEncoding;

    @Column
    private String sectorIdentifierUri;

    @Column
    private String applicationType = "web";

    @Column
    private String subjectType = OidcSubjectTypes.PUBLIC.getType();

    @Column
    private boolean dynamicallyRegistered;

    @JsonIgnore
    @Column
    @Deprecated(since = "6.2.0")
    @Transient
    @org.springframework.data.annotation.Transient
    private transient boolean implicit;

    @Column(name = "DYNAMIC_REG_TIME")
    private ZonedDateTime dynamicRegistrationDateTime;

    @Lob
    @Column(name = "scopes", length = Integer.MAX_VALUE)
    private HashSet<String> scopes = new HashSet<>(0);

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

    @Override
    protected AbstractRegisteredService newInstance() {
        return new OidcRegisteredService();
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "OpenID Connect Relying Party";
    }
}
