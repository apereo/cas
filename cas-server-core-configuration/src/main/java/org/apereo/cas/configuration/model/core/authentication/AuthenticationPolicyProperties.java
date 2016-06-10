package org.apereo.cas.configuration.model.core.authentication;

/**
 * Configuration properties class for cas.authn.policy.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class AuthenticationPolicyProperties {

    private Any any = new Any();

    private Req req = new Req();

    public Any getAny() {
        return any;
    }

    public void setAny(final Any any) {
        this.any = any;
    }

    public Req getReq() {
        return req;
    }

    public void setReq(final Req req) {
        this.req = req;
    }

    /**
     * Any.
     */
    public static class Any {
        private boolean tryAll;

        public boolean isTryAll() {
            return tryAll;
        }

        public void setTryAll(final boolean tryAll) {
            this.tryAll = tryAll;
        }
    }

    /**
     * Req.
     */
    public static class Req {
        private boolean tryAll;
        private String handlerName = "handlerName";

        public boolean isTryAll() {
            return tryAll;
        }

        public void setTryAll(final boolean tryAll) {
            this.tryAll = tryAll;
        }

        public String getHandlerName() {
            return handlerName;
        }

        public void setHandlerName(final String handlerName) {
            this.handlerName = handlerName;
        }
    }
}
