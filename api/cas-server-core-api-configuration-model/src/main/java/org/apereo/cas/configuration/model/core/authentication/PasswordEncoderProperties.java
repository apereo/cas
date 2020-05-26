package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link PasswordEncoderProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class PasswordEncoderProperties implements Serializable {

    private static final long serialVersionUID = -2396781005262069816L;

    /**
     * Define the password encoder type to use.
     * Type may be specified as blank or 'NONE' to disable password encoding.
     * It may also refer to a fully-qualified class name that implements
     * the Spring Security's {@code PasswordEncoder} interface
     * if you wish you define your own encoder.
     *
     * @see PasswordEncoderTypes
     */
    private String type = "NONE";

    /**
     * The encoding algorithm to use such as 'MD5'.
     * Relevant when the type used is 'DEFAULT' or 'GLIBC_CRYPT'.
     */
    private String encodingAlgorithm;

    /**
     * The encoding algorithm to use such as 'UTF-8'.
     * Relevant when the type used is 'DEFAULT'.
     */
    private String characterEncoding = "UTF-8";

    /**
     * Secret to use with STANDARD, PBKDF2, BCRYPT, GLIBC_CRYPT password encoders.
     * Secret usually is an optional setting.
     */
    private String secret;

    /**
     * Strength or number of iterations to use for password hashing.
     * Usually relevant when dealing with PBKDF2 or BCRYPT encoders.
     * Used by GLIBC_CRYPT encoders as well.
     */
    private int strength = 16;

    public enum PasswordEncoderTypes {

        /**
         * No password encoding will take place.
         */
        NONE,
        /**
         * Uses an encoding algorithm and a char encoding algorithm.
         */
        DEFAULT,
        /**
         * Uses Spring Security's {@code StandardPasswordEncoder}.
         * A standard {@code PasswordEncoder} implementation that uses SHA-256 hashing with 1024
         * iterations and a random 8-byte random salt value.
         */
        STANDARD,
        /**
         * Uses Spring Security's {@code BCryptPasswordEncoder}.
         */
        BCRYPT,
        /**
         * Uses Spring Security's {@code SCryptPasswordEncoder}.
         */
        SCRYPT,
        /**
         * Uses Spring Security's {@code Pbkdf2PasswordEncoder}.
         */
        PBKDF2,
        /**
         * Uses {@code org.apereo.cas.util.crypto.GlibcCryptPasswordEncoder}.
         * GNU libc crypt(3) compatible hash method.
         */
        GLIBC_CRYPT,
        /**
         * Uses Spring Security's {@code LdapShaPasswordEncoder}.
         */
        SSHA
    }
}
