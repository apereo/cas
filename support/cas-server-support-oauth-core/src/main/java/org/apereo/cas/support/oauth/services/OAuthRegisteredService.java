package org.apereo.cas.support.oauth.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * An extension of the {@link RegexRegisteredService} that defines the
 * OAuth client id and secret for a given registered service.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Entity
@DiscriminatorValue("oauth")
public class OAuthRegisteredService extends RegexRegisteredService {

    private static final long serialVersionUID = 5318897374067731021L;

    @Column(length = 255, updatable = true, insertable = true)
    private String clientSecret;

    @Column(length = 255, updatable = true, insertable = true)
    private String clientId;

    @Column(updatable = true, insertable = true)
    private Boolean bypassApprovalPrompt = Boolean.FALSE;

    @Column(updatable = true, insertable = true)
    private Boolean generateRefreshToken = Boolean.FALSE;

    @Column(updatable = true, insertable = true)
    private Boolean jsonFormat = Boolean.FALSE;

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Boolean isBypassApprovalPrompt() {
        return this.bypassApprovalPrompt;
    }

    public void setBypassApprovalPrompt(final Boolean bypassApprovalPrompt) {
        this.bypassApprovalPrompt = bypassApprovalPrompt;
    }

    public Boolean isGenerateRefreshToken() {
        return this.generateRefreshToken;
    }

    public void setGenerateRefreshToken(final Boolean generateRefreshToken) {
        this.generateRefreshToken = generateRefreshToken;
    }

    public Boolean isJsonFormat() {
        return this.jsonFormat;
    }

    public void setJsonFormat(final Boolean jsonFormat) {
        this.jsonFormat = jsonFormat;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.appendSuper(super.toString());
        builder.append("clientId", getClientId());
        builder.append("approvalPrompt", isBypassApprovalPrompt());
        builder.append("generateRefreshToken", isGenerateRefreshToken());
        builder.append("jsonFormat", isJsonFormat());
        return builder.toString();
    }

    @Override
    public void copyFrom(final RegisteredService source) {
        super.copyFrom(source);
        final OAuthRegisteredService oAuthRegisteredService = (OAuthRegisteredService) source;
        this.setClientId(oAuthRegisteredService.getClientId());
        this.setClientSecret(oAuthRegisteredService.getClientSecret());
        this.setBypassApprovalPrompt(oAuthRegisteredService.isBypassApprovalPrompt());
        this.setGenerateRefreshToken(oAuthRegisteredService.isGenerateRefreshToken());
        this.setJsonFormat(oAuthRegisteredService.isJsonFormat());
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new OAuthRegisteredService();
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
        final OAuthRegisteredService rhs = (OAuthRegisteredService) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.clientSecret, rhs.clientSecret)
                .append(this.clientId, rhs.clientId)
                .append(this.bypassApprovalPrompt, rhs.bypassApprovalPrompt)
                .append(this.generateRefreshToken, rhs.generateRefreshToken)
                .append(this.jsonFormat, rhs.jsonFormat)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.clientSecret)
                .append(this.clientId)
                .append(this.bypassApprovalPrompt)
                .append(this.generateRefreshToken)
                .append(this.jsonFormat)
                .toHashCode();
    }
}
