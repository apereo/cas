package org.apereo.cas.configuration.model.core.web.view;

import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link ViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class ViewProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 2719748442042197738L;

    /**
     * When set to {@code true}, attempts to calculate
     * and display the list of authorized services
     * for the authenticated user on successful
     * authentication attempts.
     */
    private boolean authorizedServicesOnSuccessfulLogin;

    /**
     * The default redirect URL if none is specified
     * after a successful login or logout event.
     * For logout redirects, this setting is closely
     * related to and requires {@link LogoutProperties#isFollowServiceRedirects()}.
     * This URL must be registered i the CAS server's service registry.
     */
    private String defaultRedirectUrl;

    /**
     * Additional custom fields that should be displayed
     * on the login form and would be bound to the authentication
     * credential during form-authentication
     * to carry additional metadata and tags.
     * Key is the name of the custom field.
     */
    private Map<String, CustomLoginFieldViewProperties> customLoginFormFields = new LinkedHashMap<>();

    /**
     * Comma separated paths to where CAS templates may be found.
     * Example might be {@code classpath:templates,file:/templates}.
     */
    private List<String> templatePrefixes = new ArrayList<>(1);

    /**
     * How to search for theme resource bundles and how to deal with multiple property files found for a given theme.
     * The {@link ThemeSourceTypes#DEFAULT} type uses the first theme resource bundle found across the template prefixes.
     * The {@link ThemeSourceTypes#AGGREGATE} type combines all the bundles found across template prefixes with the last
     * prefix overriding the first.
     */
    private ThemeSourceTypes themeSourceType = ThemeSourceTypes.DEFAULT;

    public enum ThemeSourceTypes {
        /**
         * Theme source that gets the first theme property file found in the prefix locations.
         */
        DEFAULT,
        /**
         * Theme source that combines all themes property files in all template prefix locations.
         */
        AGGREGATE
    }

    /**
     * CAS1 views and locations.
     */
    @NestedConfigurationProperty
    private Cas10ViewProperties cas1 = new Cas10ViewProperties();

    /**
     * CAS2 views and locations.
     */
    @NestedConfigurationProperty
    private Cas20ViewProperties cas2 = new Cas20ViewProperties();

    /**
     * CAS3 views and locations.
     */
    @NestedConfigurationProperty
    private Cas30ViewProperties cas3 = new Cas30ViewProperties();

    /**
     * Resolve CAS views via REST.
     */
    @NestedConfigurationProperty
    private RestfulViewProperties rest = new RestfulViewProperties();
}
