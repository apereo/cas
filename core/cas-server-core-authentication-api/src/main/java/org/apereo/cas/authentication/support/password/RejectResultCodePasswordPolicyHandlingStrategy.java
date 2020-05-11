package org.apereo.cas.authentication.support.password;

import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * This is {@link RejectResultCodePasswordPolicyHandlingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class RejectResultCodePasswordPolicyHandlingStrategy<AuthnResponse> extends DefaultPasswordPolicyHandlingStrategy<AuthnResponse> {
    private static final String DEFAULT_REJECTED_RESULT_CODE = "INVALID_CREDENTIAL";

    private final Set<String> resultCodes;

    public RejectResultCodePasswordPolicyHandlingStrategy() {
        this(CollectionUtils.wrapSet(DEFAULT_REJECTED_RESULT_CODE));
    }

    @Override
    public boolean supports(final AuthnResponse response) {
        if (response == null) {
            LOGGER.debug("Unable to support authentication response given none is provided");
            return false;
        }

        if (!isAuthenticationResponseWithResult(response)) {
            LOGGER.debug("Unable to support authentication response [{}] with a negative/false result", response);
            return false;
        }

        val currentCodes = getAuthenticationResponseResultCodes(response);
        val result = this.resultCodes.stream().filter(currentCodes::contains).findAny();

        if (result.isPresent()) {
            LOGGER.debug("Unable to support authentication response [{}] with a blacklisted authentication result code [{}]", response, result.get());
            return false;
        }
        LOGGER.debug("Authentication response [{}] is supported by password policy handling strategy [{}]", response, getClass().getSimpleName());
        return true;
    }

    /**
     * Is authentication response with result boolean.
     *
     * @param response the response
     * @return true/false
     */
    protected boolean isAuthenticationResponseWithResult(final AuthnResponse response) {
        return false;
    }

    /**
     * Gets authentication response result codes.
     *
     * @param response the response
     * @return the authentication response result codes
     */
    protected Collection<String> getAuthenticationResponseResultCodes(final AuthnResponse response) {
        return new ArrayList<>(0);
    }
}
