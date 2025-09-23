package org.apereo.cas.configuration.model.support.mfa.simple;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link CoreCasSimpleMultifactorAuthenticationTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-simple-mfa")
@Getter
@Setter
@Accessors(chain = true)
public class CoreCasSimpleMultifactorAuthenticationTokenProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -6333748853833491119L;

    /**
     * Time in seconds that CAS tokens should be considered live in CAS server.
     */
    private long timeToKillInSeconds = 30;

    /**
     * The length of the generated token.
     * Note that generating codes with a small length (i.e. smaller than `6` characters or digits)
     * may lead to code creation collisions, typically under rather high load. CAS will attempt to retry
     * and generate a unique code for given attempt, but may ultimately give up if a unique code cannot
     * be generated and reserved.
     */
    private int tokenLength = 6;

    /**
     * This setting determines the type of characters used in the token.
     */
    private TokenType tokenType = TokenType.NUMERIC;

    /**
     * The type of characters used in the token.
     */
    public enum TokenType {
        /**
         * Token type is numeric.
         */
        NUMERIC,
        /**
         * Token type is alphanumeric.
         */
        ALPHANUMERIC,
        /**
         * Token type is alphabetic.
         */
        ALPHABETIC
    }
}
