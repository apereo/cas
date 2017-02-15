package org.apereo.cas.configuration.model.core.authentication;

/**
 * This is {@link PasswordEncoderProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class PasswordEncoderProperties {
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
         * Uses {@link org.springframework.security.crypto.password.StandardPasswordEncoder}.
         */
        STANDARD,
        /**
         * Uses {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder}.
         */
        BCRYPT,
        /**
         * Uses {@link org.springframework.security.crypto.scrypt.SCryptPasswordEncoder}.
         */
        SCRYPT,
        /**
         * Uses {@link org.springframework.security.crypto.password.Pbkdf2PasswordEncoder}.
         */
        PBKDF2
    }
    private String type = "NONE";
    
    private String encodingAlgorithm;
    private String characterEncoding;
    private String secret;
    private int strength = 16;

    public int getStrength() {
        return strength;
    }

    public void setStrength(final int strength) {
        this.strength = strength;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(final String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public String getEncodingAlgorithm() {
        return encodingAlgorithm;
    }

    public void setEncodingAlgorithm(final String encodingAlgorithm) {
        this.encodingAlgorithm = encodingAlgorithm;
    }
}


