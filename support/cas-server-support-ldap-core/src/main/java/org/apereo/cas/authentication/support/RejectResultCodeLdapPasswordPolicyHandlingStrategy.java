package org.apereo.cas.authentication.support;

import org.apereo.cas.util.CollectionUtils;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is {@link RejectResultCodeLdapPasswordPolicyHandlingStrategy}.
 * Handles password policy only if the authentication response result code is not blacklisted.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RejectResultCodeLdapPasswordPolicyHandlingStrategy extends DefaultLdapPasswordPolicyHandlingStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RejectResultCodeLdapPasswordPolicyHandlingStrategy.class);
    
    private final List<AuthenticationResultCode> resultCodes;

    public RejectResultCodeLdapPasswordPolicyHandlingStrategy() {
        this(CollectionUtils.wrapList(AuthenticationResultCode.INVALID_CREDENTIAL));
    }
    
    public RejectResultCodeLdapPasswordPolicyHandlingStrategy(final List<AuthenticationResultCode> resultCodes) {
        this.resultCodes = resultCodes;
    }

    @Override
    public boolean supports(final AuthenticationResponse response) {
        if (response == null) {
            LOGGER.debug("Unable to support authentication response given none is provided");
            return false;
        }
        
        if (!response.getResult()) {
            LOGGER.debug("Unable to support authentication response [{}] with a negative/false result");
            return false;
        }
        
        if (this.resultCodes.contains(response.getAuthenticationResultCode())) {
            LOGGER.debug("Unable to support authentication response [{}] with a blacklisted authentication result code [{}]", 
                    response.getAuthenticationResultCode());
            return false;
        }
        LOGGER.debug("Authentication response [{}] is supported by password policy handling strategy [{}]", getClass().getSimpleName());
        return true;
    }
}
