package org.apereo.cas.configuration.model.core.web.view;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link Cas20ViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class Cas20ViewProperties implements Serializable {

    private static final long serialVersionUID = -7954879759474698003L;

    /**
     * The relative location of the CAS2 success view bean.
     */
    private String success = "protocol/2.0/casServiceValidationSuccess";

    /**
     * The relative location of the CAS3 failure view bean.
     */
    private String failure = "protocol/2.0/casServiceValidationFailure";

    /**
     * Whether v2 protocol support should be forward compatible
     * to act like v3 and match its response, mainly for attribute release.
     */
    private boolean v3ForwardCompatible;

    /**
     * Proxy views and settings.
     */
    private Proxy proxy = new Proxy();

    @RequiresModule(name = "cas-server-core-web", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Proxy implements Serializable {

        private static final long serialVersionUID = 6765987342872282599L;

        /**
         * The relative location of the CAS2 proxy success view bean.
         */
        private String success = "protocol/2.0/casProxySuccessView";

        /**
         * The relative location of the CAS2 proxy failure view bean.
         */
        private String failure = "protocol/2.0/casProxyFailureView";
    }

}
