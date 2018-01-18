package org.apereo.cas.configuration.model.core.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

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
@Slf4j
@Getter
@Setter
public class PrincipalTransformationProperties implements Serializable {

    private static final long serialVersionUID = 1678602647607236322L;

    public enum CaseConversion {

        /**
         * No conversion.
         */
        NONE, /**
         * Lowercase conversion.
         */
        UPPERCASE, /**
         * Uppcase conversion.
         */
        LOWERCASE
    }

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
     * Transform usernames using a Groovy resource.
     */
    private Groovy groovy = new Groovy();

    /**
     * Indicate whether the principal identifier should be transformed
     * into upper-case, lower-case, etc.
     * Accepted values are {@code NONE, UPPERCASE, LOWERCASE},
     */
    private CaseConversion caseConversion = CaseConversion.NONE;

    @RequiresModule(name = "cas-server-core-authentication", automated = true)
    @Getter
    @Setter
    public static class Groovy extends SpringResourceProperties {

        private static final long serialVersionUID = 8079027843747126083L;
    }
}
