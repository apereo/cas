package org.jasig.cas.authentication.handler;

import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Pass the encode/decode responsibility to a delegated Spring Security
 * password encoder.
 *
 * @author Joe McCall
 * @since 4.3
 */
@Component("delegatingPasswordEncoder")
public class DelegatingPasswordEncoder implements PasswordEncoder {

    @NotNull
    private org.springframework.security.crypto.password.PasswordEncoder springPasswordEncoder;

    public void setSpringPasswordEncoder(final org.springframework.security.crypto.password.PasswordEncoder springPasswordEncoder) {
        this.springPasswordEncoder = springPasswordEncoder;
    }

    @Override
    public String encode(final String password) {
        return springPasswordEncoder.encode(password);
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        return springPasswordEncoder.matches(rawPassword, encodedPassword);
    }


}
