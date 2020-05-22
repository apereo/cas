package org.apereo.cas.pm;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.RegexUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * This is {@link DefaultPasswordValidationService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultPasswordValidationService implements PasswordValidationService {
    private final String policyPattern;
    private final PasswordHistoryService passwordHistoryService;
    
    @Override
    public boolean isValid(final UsernamePasswordCredential c, final PasswordChangeRequest bean) {
        if (StringUtils.isEmpty(bean.getPassword())) {
            LOGGER.error("Provided password is blank");
            return false;
        }
        if (!bean.getPassword().equals(bean.getConfirmedPassword())) {
            LOGGER.error("Provided password does not match the confirmed password");
            return false;
        }
        if (!RegexUtils.find(policyPattern, bean.getPassword())) {
            LOGGER.error("Provided password does not match the pattern required for password policy [{}]", policyPattern);
            return false;
        }
        if (passwordHistoryService.exists(bean)) {
            LOGGER.error("Recycled password from password history is not allowed for [{}]", bean.getUsername());
            return false;
        }
        return validatePassword(c, bean);
    }

    /**
     * Validate password.
     *
     * @param credential the credential
     * @param bean       the bean
     * @return true/false
     */
    protected boolean validatePassword(final UsernamePasswordCredential credential, final PasswordChangeRequest bean) {
        return true;
    }
}
