package org.jasig.cas.validation;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.AuthenticationContextValidator;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationContextTicketValidationException;
import org.jasig.cas.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * The {@link AuthenticationContextValidationSpecification} is responsible for
 * evaluating whether a required authentication context is provided
 * and can be satisfied by CAS.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@Component("authenticationContextValidationSpecification")
public class AuthenticationContextValidationSpecification implements ValidationSpecification {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${cas.mfa.request.parameter:authn_method}")
    private String authenticationContextParameter;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private AuthenticationContextValidator authenticationContextValidator;

    public String getAuthenticationContextParameter() {
        return authenticationContextParameter;
    }

    public void setAuthenticationContextParameter(final String authenticationContextParameter) {
        this.authenticationContextParameter = authenticationContextParameter;
    }

    @Override
    public boolean isSatisfiedBy(final Assertion assertion, final HttpServletRequest request) {
        final String requestedContext = request.getParameter(this.authenticationContextParameter);
        if (StringUtils.isBlank(requestedContext)) {
            return true;
        }

        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> result =
                this.authenticationContextValidator.validate(assertion.getPrimaryAuthentication(), requestedContext);

        if (result.getFirst()) {
            return true;
        }

        throw new UnsatisfiedAuthenticationContextTicketValidationException(assertion.getService());
    }
}
