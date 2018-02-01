package org.apereo.cas.configuration.model.support.interrupt;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link InterruptProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-interrupt-webflow")
@Slf4j
@Getter
@Setter
public class InterruptProperties implements Serializable {

    private static final long serialVersionUID = -4945287309473842615L;

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
    public static class Json extends SpringResourceProperties {

        private static final long serialVersionUID = 1079027840047126083L;
    }

    @RequiresModule(name = "cas-server-support-interrupt-webflow")
    @Getter
    @Setter
    public static class Groovy extends SpringResourceProperties {

        private static final long serialVersionUID = 8079027843747126083L;
    }

    @RequiresModule(name = "cas-server-support-interrupt-webflow")
    @Getter
    @Setter
    public static class Rest extends RestEndpointProperties {

        private static final long serialVersionUID = 1833594332973137011L;
    }
}
