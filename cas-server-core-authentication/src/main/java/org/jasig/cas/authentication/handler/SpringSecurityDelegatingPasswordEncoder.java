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
@Component("springSecurityDelegatingPasswordEncoder")
public class SpringSecurityDelegatingPasswordEncoder implements PasswordEncoder {

    @NotNull
    private org.springframework.security.crypto.password.PasswordEncoder delegate;

    public void setDelegate(final org.springframework.security.crypto.password.PasswordEncoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public String encode(final String password) {
        return delegate.encode(password);
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }


}
