package org.jasig.cas.authentication.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Default password encoder for the case where no password encoder is needed.
 * Encoding results in the same password that was passed in.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Component("plainTextPasswordEncoder")
public class PlainTextPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(final String password) {
        return password;
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        return StringUtils.equals(rawPassword, encodedPassword);
    }
}
