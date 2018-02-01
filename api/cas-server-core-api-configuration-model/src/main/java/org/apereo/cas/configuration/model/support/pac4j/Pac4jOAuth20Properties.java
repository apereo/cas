package org.apereo.cas.configuration.model.support.pac4j;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link Pac4jOAuth20Properties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Slf4j
@Getter
@Setter
public class Pac4jOAuth20Properties extends Pac4jGenericClientProperties {

    private static final long serialVersionUID = -1240711580664148382L;

    /**
     * Authorization endpoint of the provider.
     */
    @RequiredProperty
    private String authUrl;

    /**
     * Token endpoint of the provider.
     */
    @RequiredProperty
    private String tokenUrl;

    /**
     * Profile endpoint of the provider.
     */
    @RequiredProperty
    private String profileUrl;

    /**
     * Profile path portion of the profile endpoint of the provider.
     */
    private String profilePath;

    /**
     * Http method to use when asking for profile.
     */
    private String profileVerb = "POST";

    /**
     * Profile attributes to request and collect in form of key-value pairs.
     */
    private Map<String, String> profileAttrs;

    /**
     * Custsom parameters in form of key-value pairs sent along in authZ requests, etc.
     */
    private Map<String, String> customParams;
}
