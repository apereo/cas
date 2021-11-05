package org.apereo.cas.configuration.model.support.pac4j.oauth;

import org.apereo.cas.configuration.model.support.pac4j.Pac4jIdentifiableClientProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("Pac4jOAuth20ClientProperties")
public class Pac4jOAuth20ClientProperties extends Pac4jIdentifiableClientProperties implements CasFeatureModule {

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
     * Key is the attribute name, and value is the mapped attribute name, if necessary.
     * If remapping is not required, key and value should match.
     * It's also possible to define values as {@code CONVERTER|mapped-attribute}.
     * {@code CONVERTER} should be the attribute converter specified by its acceptable type
     * and when acceptable, the converter attempts to transform the provided attribute value.
     * Accepted converters are {@code Locale, Integer, Color, Date, Gender, Boolean, Long, String, Url}.
     * CAS can also provide a special attribute converter that does the transformation and conversion
     * based on an <i>inline groovy script</i>. This special groovy converter can be specified
     * using this example syntax for the value, {@code groovy { return attribute + '-test'}|mapped-attribute}.
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
