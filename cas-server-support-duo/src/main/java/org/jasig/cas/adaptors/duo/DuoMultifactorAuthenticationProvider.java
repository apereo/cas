package org.jasig.cas.adaptors.duo;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.adaptors.duo.web.flow.DuoMultifactorWebflowConfigurer;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceMultifactorPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This is {@link DuoMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("duoAuthenticationProvider")
public class DuoMultifactorAuthenticationProvider implements MultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${cas.mfa.duo.rank:0}")
    private int rank;

    @Autowired
    @Qualifier("duoAuthenticationService")
    private DuoAuthenticationService duoAuthenticationService;

    @Override
    public boolean verify(final RegisteredService service) throws AuthenticationException {
        if (duoAuthenticationService.canPing()) {
            return true;
        }
        final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
        if (policy != null && policy.getFailureMode() == RegisteredServiceMultifactorPolicy.FailureModes.OPEN) {
            logger.warn("Duo could not be reached. Since the authentication provider is configured to fail-open, authentication will "
                    + "proceed without Duo for service {}", service.getServiceId());
            return false;
        }

        throw new AuthenticationException();
    }

    @Override
    public String getId() {
        return DuoMultifactorWebflowConfigurer.MFA_DUO_EVENT_ID;
    }

    @Override
    public int getOrder() {
        return this.rank;
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
        final DuoMultifactorAuthenticationProvider rhs = (DuoMultifactorAuthenticationProvider) obj;
        return new EqualsBuilder()
                .append(this.rank, rhs.rank)
                .append(this.getId(), rhs.getId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(rank)
                .append(getId())
                .toHashCode();
    }
}
