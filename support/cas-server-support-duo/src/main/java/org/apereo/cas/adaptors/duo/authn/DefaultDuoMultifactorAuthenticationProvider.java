package org.apereo.cas.adaptors.duo.authn;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;

/**
 * This is {@link DefaultDuoMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultDuoMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider
        implements DuoMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    private DuoAuthenticationService duoAuthenticationService;

    public void setDuoAuthenticationService(final DuoAuthenticationService duoAuthenticationService) {
        this.duoAuthenticationService = duoAuthenticationService;
    }

    @Override
    public DuoAuthenticationService getDuoAuthenticationService() {
        return this.duoAuthenticationService;
    }


    @Override
    protected boolean isAvailable() {
        return this.duoAuthenticationService.ping();
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
        final DefaultDuoMultifactorAuthenticationProvider rhs = (DefaultDuoMultifactorAuthenticationProvider) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.duoAuthenticationService, rhs.duoAuthenticationService)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(duoAuthenticationService)
                .toHashCode();
    }
}
