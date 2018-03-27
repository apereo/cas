package org.apereo.cas.support.oauth.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PostLoad;
import java.util.HashSet;

import lombok.Getter;

/**
 * An extension of the {@link RegexRegisteredService} that defines the
 * OAuth client id and secret for a given registered service.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Entity
@DiscriminatorValue("oauth")
@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class OAuthRegisteredService extends RegexRegisteredService {

    private static final long serialVersionUID = 5318897374067731021L;

    @Column
    private String clientSecret;

    @Column
    private String clientId;

    @Column
    private boolean bypassApprovalPrompt;

    @Column
    private boolean generateRefreshToken;

    @Column
    private boolean jsonFormat;

    @Lob
    @Column(name = "supported_grants", length = Integer.MAX_VALUE)
    private HashSet<String> supportedGrantTypes = new HashSet<>();

    @Lob
    @Column(name = "supported_responses", length = Integer.MAX_VALUE)
    private HashSet<String> supportedResponseTypes = new HashSet<>();

    @Override
    public void copyFrom(final RegisteredService source) {
        super.copyFrom(source);
        final OAuthRegisteredService oAuthRegisteredService = (OAuthRegisteredService) source;
        setClientId(oAuthRegisteredService.getClientId());
        setClientSecret(oAuthRegisteredService.getClientSecret());
        setBypassApprovalPrompt(oAuthRegisteredService.isBypassApprovalPrompt());
        setGenerateRefreshToken(oAuthRegisteredService.isGenerateRefreshToken());
        setJsonFormat(oAuthRegisteredService.isJsonFormat());
        setSupportedGrantTypes(oAuthRegisteredService.getSupportedGrantTypes());
        setSupportedResponseTypes(oAuthRegisteredService.getSupportedResponseTypes());
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new OAuthRegisteredService();
    }

    /**
     * Post load processing, once the service is located via JPA.
     */
    @Override
    @PostLoad
    public void postLoad() {
        if (this.supportedGrantTypes == null) {
            this.supportedGrantTypes = new HashSet<>();
        }
        if (this.supportedResponseTypes == null) {
            this.supportedResponseTypes = new HashSet<>();
        }
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "OAuth2 Client";
    }
}
