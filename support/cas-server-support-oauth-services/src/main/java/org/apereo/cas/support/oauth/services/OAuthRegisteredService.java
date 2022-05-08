package org.apereo.cas.support.oauth.services;

import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.BaseWebBasedRegisteredService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * An extension of the {@link BaseRegisteredService} that defines the
 * OAuth client id and secret for a given registered service.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class OAuthRegisteredService extends BaseWebBasedRegisteredService {

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

    private String userProfileViewType;

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
}
