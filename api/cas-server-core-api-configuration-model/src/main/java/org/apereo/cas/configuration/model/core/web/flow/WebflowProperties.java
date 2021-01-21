package org.apereo.cas.configuration.model.core.web.flow;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * Configuration properties class for webflow.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("WebflowProperties")
public class WebflowProperties implements Serializable {

    private static final long serialVersionUID = 4949978905279568311L;

    /**
     * Encryption/signing setting for webflow.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto =
        new EncryptionRandomizedSigningJwtCryptographyProperties();

    /**
     * Webflow auto configuration settings.
     */
    @NestedConfigurationProperty
    private WebflowAutoConfigurationProperties autoConfiguration = new WebflowAutoConfigurationProperties();
    
    /**
     * Webflow session management settings.
     */
    @NestedConfigurationProperty
    private WebflowSessionManagementProperties session = new WebflowSessionManagementProperties();

    /**
     * Configuration settings relevant for login flow and view decoration.
     */
    @NestedConfigurationProperty
    private WebflowLoginDecoratorProperties loginDecorator = new WebflowLoginDecoratorProperties();

    /**
     * Path to groovy resource that may auto-configure the webflow context
     * dynamically creating/removing states and actions.
     */
    @NestedConfigurationProperty
    private GroovyWebflowProperties groovy = new GroovyWebflowProperties();
}
