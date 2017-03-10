package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.support.Beans;
import org.junit.Test;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.pac4j.core.credentials.password.SpringSecurityPasswordEncoder;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultPasswordEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultPasswordEncoderTests {

    @Test
    public void verifyPasswordEncoderByCustomClassName() {
        final PasswordEncoderProperties p = new PasswordEncoderProperties();
        p.setSecret("SECRET");
        final PasswordEncoder e = new SpringSecurityPasswordEncoder(Beans.newPasswordEncoder(p));
        assertNotNull(e);
    }

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
    public void verifyPasswordEncoderByMD5Again() {
        final PasswordEncoderProperties p = new PasswordEncoderProperties();
        p.setType(PasswordEncoderProperties.PasswordEncoderTypes.DEFAULT);
        p.setEncodingAlgorithm("MD5");
        p.setCharacterEncoding("UTF-8");
        final PasswordEncoder e = new SpringSecurityPasswordEncoder(Beans.newPasswordEncoder(p));
        assertTrue(e.matches("testpass", "179ad45c6ce2cb97cf1029e212046e81"));
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

    @Test
    public void verifyPasswordEncoderBySHA256() {
        final PasswordEncoderProperties p = new PasswordEncoderProperties();
        p.setType(PasswordEncoderProperties.PasswordEncoderTypes.DEFAULT);
        p.setEncodingAlgorithm("SHA-256");
        p.setCharacterEncoding("UTF-8");
        final PasswordEncoder e = new SpringSecurityPasswordEncoder(Beans.newPasswordEncoder(p));
        assertTrue(e.matches("asd123", "54d5cb2d332dbdb4850293caae4559ce88b65163f1ea5d4e4b3ac49d772ded14"));
    }

}
