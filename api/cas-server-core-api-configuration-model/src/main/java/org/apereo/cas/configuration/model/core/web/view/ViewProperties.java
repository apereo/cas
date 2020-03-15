package org.apereo.cas.configuration.model.core.web.view;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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

    private static final long serialVersionUID = 2719748442042197738L;

    /**
     * The default redirect URL if none is specified
     * after a successful authentication event.
     */
    private String defaultRedirectUrl;

    /**
     * Additional custom fields that should be displayed
     * on the login form and would be bound to the authentication
     * credential during form-authentication
     * to carry additional metadata and tags.
     * Key is the name of the custom field.
     */
    private Map<String, CustomLoginField> customLoginFormFields = new LinkedHashMap<>(0);

    /**
     * Comma separated paths to where CAS templates may be found.
     */
    private List<String> templatePrefixes = new ArrayList<>(1);

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
    private Rest rest = new Rest();

    @RequiresModule(name = "cas-server-core-web", automated = true)
    @Getter
    @Setter
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = -8102345678378393382L;
    }

    @RequiresModule(name = "cas-server-core-web", automated = true)
    @Getter
    @Setter
    public static class CustomLoginField implements Serializable {
        private static final long serialVersionUID = -7122345678378395582L;

        /**
         * The key for this field found in the message bundle
         * used to present a label/text in CAS views.
         */
        private String messageBundleKey;
        /**
         * Whether this field is required to have a value.
         */
        private boolean required;
        /**
         * The id of the custom converter to use to convert bound property values.
         */
        private String converter;
    }
}
