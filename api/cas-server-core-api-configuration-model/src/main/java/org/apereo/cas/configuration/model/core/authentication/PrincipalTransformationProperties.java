package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link PrincipalTransformationProperties}.
 * Transform the user id prior to executing the authentication sequence.
 * Each authentication strategy in CAS provides settings to properly transform
 * the principal. Refer to the relevant settings for the authentication strategy at hand to learn more.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("PrincipalTransformationProperties")
public class PrincipalTransformationProperties implements Serializable {

    private static final long serialVersionUID = 1678602647607236322L;

    /**
     * Prefix to add to the principal id prior to authentication.
     */
    private String prefix;

    /**
     * Suffix to add to the principal id prior to authentication.
     */
    private String suffix;

    /**
     * A regular expression that will be used against the provided username
     * for username extractions. On a successful match, the first matched group
     * in the pattern will be used as the extracted username.
     */
    private String pattern;

    /**
     * A regular expression that will be used against the username
     * to match for blocking/forbidden values.
     * If a match is found, an exception will be thrown
     * and principal transformation will fail.
     */
    private String blockingPattern;

    /**
     * Transform usernames using a Groovy resource.
     */
    @NestedConfigurationProperty
    private GroovyPrincipalTransformationProperties groovy = new GroovyPrincipalTransformationProperties();

    /**
     * Indicate whether the principal identifier should be transformed
     * into upper-case, lower-case, etc.
     */
    private CaseConversion caseConversion = CaseConversion.NONE;

    /**
     * Indicate whether the principal identifier should be transformed
     * into upper-case, lower-case, etc.
     */
    public enum CaseConversion {
        /**
         * No conversion.
         */
        NONE,
        /**
         * Lowercase conversion.
         */
        LOWERCASE,
        /**
         * Uppercase conversion.
         */
        UPPERCASE
    }

}
