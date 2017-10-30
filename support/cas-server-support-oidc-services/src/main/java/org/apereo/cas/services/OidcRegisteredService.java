package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PostLoad;
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
public class OidcRegisteredService extends OAuthRegisteredService {

    private static final long serialVersionUID = 1310899699465091444L;

    @Column(length = 255, updatable = true, insertable = true)
    private String jwks;

    @Column(updatable = true, insertable = true)
    private boolean signIdToken = true;

    @Column(updatable = true, insertable = true)
    private boolean encryptIdToken;

    @Column(length = 255, updatable = true, insertable = true)
    private String idTokenEncryptionAlg;

    @Column(length = 255, updatable = true, insertable = true)
    private String idTokenEncryptionEncoding;

    @Column(length = 255, updatable = true, insertable = true)
    private String sectorIdentifierUri;

    @Column(length = 255, updatable = true, insertable = true)
    private String subjectType;
    
    @Column(updatable = true, insertable = true)
    private boolean dynamicallyRegistered;

    @Column(updatable = true, insertable = true)
    private boolean implicit;

    @Column(name = "DYNAMIC_REG_TIME")
    private ZonedDateTime dynamicRegistrationDateTime;

    @Lob
    @Column(name = "scopes", length = Integer.MAX_VALUE)
    private HashSet<String> scopes = new HashSet<>();

    public OidcRegisteredService() {
        setJsonFormat(Boolean.TRUE);
    }

    public boolean isEncryptIdToken() {
        return encryptIdToken;
    }

    public void setEncryptIdToken(final boolean encryptIdToken) {
        this.encryptIdToken = encryptIdToken;
    }

    public boolean isSignIdToken() {
        return signIdToken;
    }

    public void setSignIdToken(final boolean signIdToken) {
        this.signIdToken = signIdToken;
    }

    public String getJwks() {
        return jwks;
    }

    public void setJwks(final String jwks) {
        this.jwks = jwks;
    }

    public boolean isImplicit() {
        return implicit;
    }

    public void setImplicit(final boolean implicit) {
        this.implicit = implicit;
    }

    public String getIdTokenEncryptionAlg() {
        return idTokenEncryptionAlg;
    }

    public void setIdTokenEncryptionAlg(final String idTokenEncryptionAlg) {
        this.idTokenEncryptionAlg = idTokenEncryptionAlg;
    }

    public String getIdTokenEncryptionEncoding() {
        return idTokenEncryptionEncoding;
    }

    public void setIdTokenEncryptionEncoding(final String idTokenEncryptionEncoding) {
        this.idTokenEncryptionEncoding = idTokenEncryptionEncoding;
    }

    public boolean isDynamicallyRegistered() {
        return dynamicallyRegistered;
    }

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
     * Sets subject type.
     *
     * @param subjectType the subject type
     */
    public void setSubjectType(final String subjectType) {
        if (StringUtils.isBlank(this.subjectType)) {
            this.subjectType = OidcSubjectTypes.PUBLIC.getType();
        } else {
            this.subjectType = subjectType;
        }
    }

    /**
     * Indicates the service was dynamically registered.
     * Records the registration time automatically.
     *
     * @param dynamicallyRegistered dynamically registered.
     */
    public void setDynamicallyRegistered(final boolean dynamicallyRegistered) {
        if (dynamicallyRegistered && !this.dynamicallyRegistered && dynamicRegistrationDateTime == null) {
            setDynamicRegistrationDateTime(ZonedDateTime.now());
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
            this.scopes = new HashSet<>();
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

    public ZonedDateTime getDynamicRegistrationDateTime() {
        return dynamicRegistrationDateTime;
    }

    public void setDynamicRegistrationDateTime(final ZonedDateTime dynamicRegistrationDateTime) {
        this.dynamicRegistrationDateTime = dynamicRegistrationDateTime;
    }

    public String getSectorIdentifierUri() {
        return sectorIdentifierUri;
    }

    public void setSectorIdentifierUri(final String sectorIdentifierUri) {
        this.sectorIdentifierUri = sectorIdentifierUri;
    }

    /**
     * Initializes the registered service with default values
     * for fields that are unspecified. Only triggered by JPA.
     */
    @PostLoad
    public void postLoad() {
        if (this.scopes == null) {
            this.scopes = new HashSet<>();
        }
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new OidcRegisteredService();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final OidcRegisteredService rhs = (OidcRegisteredService) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        return builder
                .appendSuper(super.equals(obj))
                .append(this.jwks, rhs.jwks)
                .append(this.implicit, rhs.implicit)
                .append(this.signIdToken, rhs.signIdToken)
                .append(this.encryptIdToken, rhs.encryptIdToken)
                .append(this.idTokenEncryptionAlg, rhs.idTokenEncryptionAlg)
                .append(this.idTokenEncryptionEncoding, rhs.idTokenEncryptionEncoding)
                .append(this.getScopes(), rhs.getScopes())
                .append(this.sectorIdentifierUri, rhs.sectorIdentifierUri)
                .append(this.getSubjectType(), rhs.getSubjectType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(jwks)
                .append(signIdToken)
                .append(implicit)
                .append(encryptIdToken)
                .append(idTokenEncryptionAlg)
                .append(idTokenEncryptionEncoding)
                .append(dynamicallyRegistered)
                .append(getScopes())
                .append(sectorIdentifierUri)
                .append(subjectType)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("jwks", jwks)
                .append("implicit", implicit)
                .append("signIdToken", signIdToken)
                .append("idTokenEncryptionAlg", idTokenEncryptionAlg)
                .append("idTokenEncryptionEncoding", idTokenEncryptionEncoding)
                .append("encryptIdToken", encryptIdToken)
                .append("dynamicallyRegistered", dynamicallyRegistered)
                .append("scopes", getScopes())
                .append("sectorIdentifierUri", sectorIdentifierUri)
                .append("subjectType", subjectType)
                .toString();
    }

    @Override
    public void copyFrom(final RegisteredService source) {
        super.copyFrom(source);
        try {
            final OidcRegisteredService oidcService = (OidcRegisteredService) source;
            setJwks(oidcService.getJwks());
            setImplicit(oidcService.isImplicit());
            setSignIdToken(oidcService.isSignIdToken());
            setIdTokenEncryptionAlg(oidcService.getIdTokenEncryptionAlg());
            setIdTokenEncryptionEncoding(oidcService.idTokenEncryptionEncoding);
            setEncryptIdToken(oidcService.isEncryptIdToken());
            setDynamicallyRegistered(oidcService.isDynamicallyRegistered());
            setScopes(oidcService.getScopes());
            setSectorIdentifierUri(oidcService.getSectorIdentifierUri());
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "OpenID Connect Relying Party";
    }
}
