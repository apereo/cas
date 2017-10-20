package org.apereo.cas.configuration.model.support.interrupt;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import java.io.Serializable;

/**
 * This is {@link InterruptProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-interrupt-webflow")
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

    public Json getJson() {
        return json;
    }

    public void setJson(final Json json) {
        this.json = json;
    }

    public Groovy getGroovy() {
        return groovy;
    }

    public void setGroovy(final Groovy groovy) {
        this.groovy = groovy;
    }

    public Rest getRest() {
        return rest;
    }

    public void setRest(final Rest rest) {
        this.rest = rest;
    }

    @RequiresModule(name = "cas-server-support-interrupt-webflow")  
    public static class Json extends SpringResourceProperties {
        private static final long serialVersionUID = 1079027840047126083L;
    }

    @RequiresModule(name = "cas-server-support-interrupt-webflow")
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 8079027843747126083L;
    }

    @RequiresModule(name = "cas-server-support-interrupt-webflow")
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = 1833594332973137011L;
    }
}
