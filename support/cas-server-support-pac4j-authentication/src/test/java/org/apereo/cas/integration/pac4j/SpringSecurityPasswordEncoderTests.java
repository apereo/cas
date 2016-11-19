package org.apereo.cas.integration.pac4j;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.support.Beans;
import org.junit.Test;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.pac4j.core.credentials.password.SpringSecurityPasswordEncoder;

import static org.junit.Assert.*;

/**
 * This is {@link SpringSecurityPasswordEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SpringSecurityPasswordEncoderTests {

    @Test
    public void verifyPasswordEncoderByMD5() {
        final PasswordEncoderProperties p = new PasswordEncoderProperties();
        p.setType(PasswordEncoderProperties.PasswordEncoderTypes.DEFAULT);
        p.setEncodingAlgorithm("MD5");
        p.setCharacterEncoding("UTF-8");
        final PasswordEncoder e = new SpringSecurityPasswordEncoder(Beans.newPasswordEncoder(p));
        assertTrue(e.matches("asd123", "bfd59291e825b5f2bbf1eb76569f8fe7"));
    }

    @Test
    public void verifyPasswordEncoderBySHA1() {
        final PasswordEncoderProperties p = new PasswordEncoderProperties();
        p.setType(PasswordEncoderProperties.PasswordEncoderTypes.DEFAULT);
        p.setEncodingAlgorithm("SHA-1");
        p.setCharacterEncoding("UTF-8");
        final PasswordEncoder e = new SpringSecurityPasswordEncoder(Beans.newPasswordEncoder(p));
        assertTrue(e.matches("asd123", "2891baceeef1652ee698294da0e71ba78a2a4064"));
    }
}
