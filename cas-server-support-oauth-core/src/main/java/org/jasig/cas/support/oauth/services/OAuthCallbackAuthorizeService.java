package org.jasig.cas.support.oauth.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.support.oauth.OAuthConstants;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * An extension of the {@link RegexRegisteredService} that attempts to enforce the
 * correct url syntax for the OAuth callback authorize url. The url must end with
 * {@link OAuthConstants#CALLBACK_AUTHORIZE_URL}.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Entity
@DiscriminatorValue("oauthcba")
public final class OAuthCallbackAuthorizeService extends RegexRegisteredService {

    private static final long serialVersionUID = 1365893114273585648L;

    /**
     * {@inheritDoc}.
     * @throws IllegalArgumentException if the received url does not end with
     *         {@link OAuthConstants#CALLBACK_AUTHORIZE_URL}
     */
    @Override
    public void setServiceId(final String id) {
        if (!id.endsWith(OAuthConstants.CALLBACK_AUTHORIZE_URL)) {
            final String msg = String.format("OAuth callback authorize service id must end with [%s]",
                    OAuthConstants.CALLBACK_AUTHORIZE_URL);
            throw new IllegalArgumentException(msg);
        }
        super.setServiceId(id);
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
        final OAuthCallbackAuthorizeService rhs = (OAuthCallbackAuthorizeService) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(rhs))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 137)
                .appendSuper(super.hashCode())
                .toHashCode();
    }
}
