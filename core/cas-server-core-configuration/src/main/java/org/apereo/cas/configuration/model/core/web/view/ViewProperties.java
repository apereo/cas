package org.apereo.cas.configuration.model.core.web.view;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link ViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
public class ViewProperties implements Serializable {
    private static final long serialVersionUID = 2719748442042197738L;
    /**
     * The default redirect URL if none is specified
     * after a successful authentication event.
     */
    private String defaultRedirectUrl;

    /**
     * CAS2 views and locations.
     */
    private Cas2 cas2 = new Cas2();
    /**
     * CAS3 views and locations.
     */
    private Cas3 cas3 = new Cas3();

    public Cas2 getCas2() {
        return cas2;
    }

    public Cas3 getCas3() {
        return cas3;
    }

    public void setCas2(final Cas2 cas2) {
        this.cas2 = cas2;
    }

    public void setCas3(final Cas3 cas3) {
        this.cas3 = cas3;
    }

    public String getDefaultRedirectUrl() {
        return defaultRedirectUrl;
    }

    public void setDefaultRedirectUrl(final String defaultRedirectUrl) {
        this.defaultRedirectUrl = defaultRedirectUrl;
    }

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

        public boolean isV3ForwardCompatible() {
            return v3ForwardCompatible;
        }

        public void setV3ForwardCompatible(final boolean v3ForwardCompatible) {
            this.v3ForwardCompatible = v3ForwardCompatible;
        }

        public Proxy getProxy() {
            return proxy;
        }

        public void setProxy(final Proxy proxy) {
            this.proxy = proxy;
        }

        public String getSuccess() {
            return success;
        }

        public void setSuccess(final String success) {
            this.success = success;
        }

        public String getFailure() {
            return failure;
        }

        public void setFailure(final String failure) {
            this.failure = failure;
        }

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

            public String getSuccess() {
                return success;
            }

            public void setSuccess(final String success) {
                this.success = success;
            }

            public String getFailure() {
                return failure;
            }

            public void setFailure(final String failure) {
                this.failure = failure;
            }
        }
    }

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

        public String getSuccess() {
            return success;
        }

        public void setSuccess(final String success) {
            this.success = success;
        }

        public String getFailure() {
            return failure;
        }

        public void setFailure(final String failure) {
            this.failure = failure;
        }
    }
}
