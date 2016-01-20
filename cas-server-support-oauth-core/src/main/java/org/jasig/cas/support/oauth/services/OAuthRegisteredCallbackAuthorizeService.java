package org.jasig.cas.support.oauth.services;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.support.oauth.OAuthConstants;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * OAuth registered service that denotes the callback authorized url.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Entity
@DiscriminatorValue("oauthrcba")
public final class OAuthRegisteredCallbackAuthorizeService extends RegexRegisteredService {

    private static final long serialVersionUID = 2993846310010319047L;

    /**
     * Sets the callback authorize url.
     *
     * @param url the new callback authorize url
     */
    public void setCallbackAuthorizeUrl(final String url) {
        if (!url.endsWith(OAuthConstants.CALLBACK_AUTHORIZE_URL)) {
            throw new IllegalArgumentException("Calllback authorize url must end with "
                                                + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        }
        super.setServiceId(url);
    }

    @Override
    public void setServiceId(final String id) {
        this.setCallbackAuthorizeUrl(id);
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new OAuthRegisteredCallbackAuthorizeService();
    }
}
