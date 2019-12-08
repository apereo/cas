package org.apereo.cas.support.oauth.services;

import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.HashSet;

/**
 * An extension of the {@link RegexRegisteredService} that defines the
 * OAuth client id and secret for a given registered service.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Entity
@DiscriminatorValue("oauth")
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class OAuthRegisteredService extends RegexRegisteredService {

    private static final long serialVersionUID = 5318897374067731021L;
    
    private String clientSecret;

    @Column
    private String clientId;

    @Column
    private boolean bypassApprovalPrompt;

    @Column
    private boolean generateRefreshToken;

    @Column
    private boolean renewRefreshToken;

    @Column
    private boolean jwtAccessToken;

    @Lob
    @Column(name = "code_exp_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceOAuthCodeExpirationPolicy codeExpirationPolicy;

    @Lob
    @Column(name = "at_exp_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceOAuthAccessTokenExpirationPolicy accessTokenExpirationPolicy;

    @Lob
    @Column(name = "rt_exp_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceOAuthRefreshTokenExpirationPolicy refreshTokenExpirationPolicy;

    @Lob
    @Column(name = "dt_exp_policy", length = Integer.MAX_VALUE)
    private RegisteredServiceOAuthDeviceTokenExpirationPolicy deviceTokenExpirationPolicy;

    @Lob
    @Column(name = "supported_grants", length = Integer.MAX_VALUE)
    private HashSet<String> supportedGrantTypes = new HashSet<>(0);

    @Lob
    @Column(name = "supported_responses", length = Integer.MAX_VALUE)
    private HashSet<String> supportedResponseTypes = new HashSet<>(0);

    @Override
    protected AbstractRegisteredService newInstance() {
        return new OAuthRegisteredService();
    }

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

}
