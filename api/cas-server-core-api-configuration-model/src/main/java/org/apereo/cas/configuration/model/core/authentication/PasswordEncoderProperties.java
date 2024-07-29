package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = -2396781005262069816L;

    /**
     * Define the password encoder type to use.
     * Type may be specified as blank or {@code NONE} to disable password encoding.
     * It may also refer to a fully-qualified class name that implements
     * the Spring Security's {@code PasswordEncoder} interface
     * if you wish you define your own encoder.
     * <p>
     * The following types may be used:
     * <ul>
     *     <li>{@code NONE}: No password encoding (i.e. plain-text) takes place.</li>
     *     <li>{@code DEFAULT}: Use the {@code DefaultPasswordEncoder} of CAS. For message-digest
     *     algorithms via {@code character-encoding} and {@code encoding-algorithm}.</li>
     *     <li>{@code BCRYPT}: Use the {@code BCryptPasswordEncoder} based on the strength provided and an optional secret.</li>
     *     <li>{@code SCRYPT}: Use the {@code SCryptPasswordEncoder}.</li>
     *     <li>{@code PBKDF2}: Use the {@code Pbkdf2PasswordEncoder} based on the strength provided and an optional secret.</li>
     *     <li>{@code STANDARD}: Use the {@code StandardPasswordEncoder} based on the secret provided.</li>
     *     <li>{@code SSHA}: Use the {@code LdapShaPasswordEncoder} supports Ldap SHA and SSHA (salted-SHA). The values
     *     are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash.</li>
     *     <li>{@code GLIBC_CRYPT}: Use the {@code GlibcCryptPasswordEncoder} based on the
     *     {@code encoding-algorithm}, strength provided and an optional secret.</li>
     *     <li>{@code org.example.MyEncoder}: An implementation of {@code PasswordEncoder} of your own choosing.</li>
     *     <li>{@code file:///path/to/script.groovy}: Path to a Groovy script charged with handling password encoding operations.</li>
     * </ul>
     */
    @RequiredProperty
    private String type = "NONE";

    /**
     * The encoding algorithm to use such as {@code MD5}.
     * Relevant when the type used is {@code DEFAULT} or {@code GLIBC_CRYPT}.
     * When used with {@link PasswordEncoderTypes#PBKDF2}, it should be one of {@code PBKDF2WithHmacSHA1},
     * {@code PBKDF2WithHmacSHA256} or {@code PBKDF2WithHmacSHA512}.
     */
    @RequiredProperty
    private String encodingAlgorithm;

    /**
     * The encoding algorithm to use such as 'UTF-8'.
     * Relevant when the type used is {@code DEFAULT}.
     */
    private String characterEncoding = "UTF-8";

    /**
     * Secret to use with {@link PasswordEncoderTypes#STANDARD}, {@link PasswordEncoderTypes#PBKDF2},
     * {@link PasswordEncoderTypes#BCRYPT}, {@link PasswordEncoderTypes#GLIBC_CRYPT} password encoders.
     * Secret usually is an optional setting.
     */
    private String secret;

    /**
     * Strength or number of iterations to use for password hashing.
     * Usually relevant when dealing with {@link PasswordEncoderTypes#BCRYPT}, {@link PasswordEncoderTypes#PBKDF2} or {@link PasswordEncoderTypes#GLIBC_CRYPT}.
     * When used by {@link PasswordEncoderTypes#ARGON2} or {@link PasswordEncoderTypes#PBKDF2}, it indicates the salt strength.
     */
    private int strength = 16;

    /**
     * When used by {@link PasswordEncoderTypes#PBKDF2}, it indicates the required number of iterations.
     */
    private int iterations = 310000;


    /**
     * When used by {@link PasswordEncoderTypes#ARGON2}, it indicates the hash strength/length.
     */
    private int hashLength = 16;

    public enum PasswordEncoderTypes {
        /**
         * Uses the Argon2 hashing function. Users can optionally supply the length of the salt to use,
         * and the length of the generated hash.
         */
        ARGON2,
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
