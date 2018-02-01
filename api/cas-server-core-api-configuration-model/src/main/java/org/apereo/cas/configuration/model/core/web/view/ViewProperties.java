package org.apereo.cas.configuration.model.core.web.view;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link ViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Slf4j
@Getter
@Setter
public class ViewProperties implements Serializable {

    private static final long serialVersionUID = 2719748442042197738L;

    /**
     * The default redirect URL if none is specified
     * after a successful authentication event.
     */
    private String defaultRedirectUrl;

    /**
     * Comma separated paths to where CAS templates may be found.
     */
    private List<String> templatePrefixes = new ArrayList<>();

    /**
     * CAS2 views and locations.
     */
    private Cas2 cas2 = new Cas2();

    /**
     * CAS3 views and locations.
     */
    private Cas3 cas3 = new Cas3();

    @Getter
    @Setter
    public static class Cas2 implements Serializable {

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

        @Getter
        @Setter
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

    @Getter
    @Setter
    public static class Cas3 implements Serializable {

        private static final long serialVersionUID = -2345062034300650858L;

        /**
         * The relative location of the CAS3 success validation bean.
         */
        private String success = "protocol/3.0/casServiceValidationSuccess";

        /**
         * The relative location of the CAS3 success validation bean.
         */
        private String failure = "protocol/3.0/casServiceValidationFailure";
    }
}
