package org.apereo.cas.authentication.support;

import org.apereo.cas.util.CollectionUtils;
import org.ldaptive.ResultCode;
import org.ldaptive.auth.AuthenticationResponse;

import java.util.List;

/**
 * This is {@link RejectResultCodeLdapPasswordPolicyHandlingStrategy}.
 * Handles password policy only if the authentication response result code is not blacklisted.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RejectResultCodeLdapPasswordPolicyHandlingStrategy extends DefaultLdapPasswordPolicyHandlingStrategy {
    private final List<ResultCode> resultCodes;

    public RejectResultCodeLdapPasswordPolicyHandlingStrategy() {
        this(CollectionUtils.wrapList(ResultCode.INVALID_CREDENTIALS));
    }
    
    public RejectResultCodeLdapPasswordPolicyHandlingStrategy(final List<ResultCode> resultCodes) {
        this.resultCodes = resultCodes;
    }

    @Override
    public boolean supports(final AuthenticationResponse response) {
        return response.getResult() && this.resultCodes.contains(ResultCode.INVALID_CREDENTIALS);
    }
}
