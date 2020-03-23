package org.apereo.cas.configuration.model.support.interrupt;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link InterruptProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-interrupt-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class InterruptProperties implements Serializable {
    private static final long serialVersionUID = -4945287309473842615L;

    /**
     * A regex pattern on the attribute name that if matches will successfully
     * complete the first condition for the interrupt notifications trigger.
     */
    private String attributeName;

    /**
     * A regex pattern on the attribute value that if matches will successfully
     * complete the first condition for the interrupt notifications trigger.
     */
    private String attributeValue;

    /**
     * Inquire for interrupt using a JSON resource.
     */
    private Json json = new Json();

    /**
     * Inquire for interrupt using a Groovy resource.
     */
    private Groovy groovy = new Groovy();

    /**
     * Inquire for interrupt using a REST resource.
     */
    private Rest rest = new Rest();

    @RequiresModule(name = "cas-server-support-interrupt-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Json extends SpringResourceProperties {

        private static final long serialVersionUID = 1079027840047126083L;
    }

    @RequiresModule(name = "cas-server-support-interrupt-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Groovy extends SpringResourceProperties {

        private static final long serialVersionUID = 8079027843747126083L;
    }

    @RequiresModule(name = "cas-server-support-interrupt-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Rest extends RestEndpointProperties {

        private static final long serialVersionUID = 1833594332973137011L;
    }
}
