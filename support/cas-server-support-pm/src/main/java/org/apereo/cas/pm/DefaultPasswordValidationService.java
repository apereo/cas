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

    @Override
    public boolean isValid(final UsernamePasswordCredential c, final PasswordChangeBean bean) {
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
        return true;
    }
}
