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
     */
    private int tokenLength = 6;
}
