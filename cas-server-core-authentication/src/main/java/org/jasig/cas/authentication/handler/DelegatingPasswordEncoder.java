package org.jasig.cas.authentication.handler;

import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Use reflection to delegate encode() and match() to the object delegate
 */
@Component("delegatingPasswordEncoder")
public class DelegatingPasswordEncoder implements PasswordEncoder {

    @NotNull
    private org.springframework.security.crypto.password.PasswordEncoder springPasswordEncoder;

    public void setSpringPasswordEncoder(org.springframework.security.crypto.password.PasswordEncoder springPasswordEncoder) {
        this.springPasswordEncoder = springPasswordEncoder;
    }

    @Override
    public String encode(String password) {
        return springPasswordEncoder.encode(password);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return springPasswordEncoder.matches(rawPassword, encodedPassword);
    }


}
