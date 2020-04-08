package org.apereo.cas.configuration.model.core.web.flow;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

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
public class WebflowProperties implements Serializable {

    private static final long serialVersionUID = 4949978905279568311L;

    /**
     * Encryption/signing setting for webflow.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    /**
     * Whether CAS should take control of all spring webflow modifications
     * and dynamically alter views, states and actions.
     */
    private boolean autoconfigure = true;

    /**
     * Whether webflow should remain in "live reload" mode, able to auto detect
     * changes and react. This is useful if the location of the webflow is externalized
     * and changes are done ad-hoc to the webflow to accommodate changes.
     */
    private boolean refresh;

    /**
     * Whether flow executions should redirect after they pause before rendering.
     */
    private boolean alwaysPauseRedirect;

    /**
     * Whether flow executions redirect after they pause for transitions that remain in the same view state.
     */
    private boolean redirectSameState;

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
    private Groovy groovy = new Groovy();

    /**
     * With a base path defined, the algorithm that assigns flow identifiers changes slightly.
     * Flows will now be assigned registry identifiers equal to the the path segment between
     * their base path and file name. For example, if a flow definition is located
     * at {@code /WEB-INF/hotels/booking/booking-flow.xml} and the base path is {@code /WEB-INF} the remaining path
     * to this flow is {@code hotels/booking} which becomes the flow id.
     * If no base path is not specified or if the flow definition is directly on the base
     * path, flow id assignment from the filename (minus the extension) is used. For example,
     * if a flow definition file is {@code booking.xml}, the flow identifier is simply {@code booking}.
     */
    private String basePath;

    @RequiresModule(name = "cas-server-core-webflow", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Groovy extends SpringResourceProperties {

        private static final long serialVersionUID = 8079027843747126083L;
    }
}
