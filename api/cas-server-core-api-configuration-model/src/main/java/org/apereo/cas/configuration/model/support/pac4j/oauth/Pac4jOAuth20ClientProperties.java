package org.apereo.cas.configuration.model.support.pac4j.oauth;

import org.apereo.cas.configuration.model.support.pac4j.Pac4jIdentifiableClientProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link Pac4jOAuth20ClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jOAuth20ClientProperties extends Pac4jIdentifiableClientProperties {

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
     * The scope requested from the identity provider.
     */
    private String scope;

    /**
     * Profile path portion of the profile endpoint of the provider.
     */
    private String profilePath;

    /**
     * Http method to use when asking for profile.
     */
    private String profileVerb = "POST";

    /**
     * Response type determines the authentication flow on the Authentication Server.
     */
    private String responseType = "code";

    /**
     * Profile attributes to request and collect in form of key-value pairs.
     */
    private Map<String, String> profileAttrs = new LinkedHashMap<>(1);

    /**
     * Custom parameters in form of key-value pairs sent along in authZ requests, etc.
     */
    private Map<String, String> customParams = new LinkedHashMap<>(1);

    public Pac4jOAuth20ClientProperties() {
        setCallbackUrlType(CallbackUrlTypes.PATH_PARAMETER);
    }
}
