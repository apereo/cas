package org.apereo.cas.support.oauth.services;

import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * An extension of the {@link RegexRegisteredService} that defines the
 * OAuth client id and secret for a given registered service.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class OAuthRegisteredService extends RegexRegisteredService {

    private static final long serialVersionUID = 5318897374067731021L;

    private String clientSecret;

    private String clientId;

    private boolean bypassApprovalPrompt;

    private boolean generateRefreshToken;

    private boolean renewRefreshToken;

    private boolean jwtAccessToken;

    private RegisteredServiceOAuthCodeExpirationPolicy codeExpirationPolicy;

    private RegisteredServiceOAuthAccessTokenExpirationPolicy accessTokenExpirationPolicy;

    private RegisteredServiceOAuthRefreshTokenExpirationPolicy refreshTokenExpirationPolicy;

    private RegisteredServiceOAuthDeviceTokenExpirationPolicy deviceTokenExpirationPolicy;

    private Set<String> supportedGrantTypes = new HashSet<>(0);

    private Set<String> supportedResponseTypes = new HashSet<>(0);

    @Override
    public void initialize() {
        super.initialize();
        if (this.supportedGrantTypes == null) {
            this.supportedGrantTypes = new HashSet<>(0);
        }
        if (this.supportedResponseTypes == null) {
            this.supportedResponseTypes = new HashSet<>(0);
        }
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "OAuth2 Client";
    }

    @JsonIgnore
    @Override
    public int getEvaluationPriority() {
        return 2;
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new OAuthRegisteredService();
    }
}
