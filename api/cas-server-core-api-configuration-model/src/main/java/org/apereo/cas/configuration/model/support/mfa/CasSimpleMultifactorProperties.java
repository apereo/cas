package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link CasSimpleMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-simple-mfa")
@Getter
@Setter
public class CasSimpleMultifactorProperties extends BaseMultifactorProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-simple";

    private static final long serialVersionUID = -9211748853833491119L;

    public CasSimpleMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
