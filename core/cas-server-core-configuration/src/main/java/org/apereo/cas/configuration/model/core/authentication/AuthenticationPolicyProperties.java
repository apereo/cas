package org.apereo.cas.configuration.model.core.authentication;

/**
 * Configuration properties class for cas.authn.policy.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class AuthenticationPolicyProperties {

    private boolean requiredHandlerAuthenticationPolicyEnabled;
    
    private Any any = new Any();

    private Req req = new Req();

    private All all = new All();
    
    private NotPrevented notPrevented = new NotPrevented();

    public All getAll() {
        return all;
    }

    public void setAll(final All all) {
        this.all = all;
    }

    public NotPrevented getNotPrevented() {
        return notPrevented;
    }

    public void setNotPrevented(final NotPrevented notPrevented) {
        this.notPrevented = notPrevented;
    }

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

    public boolean isRequiredHandlerAuthenticationPolicyEnabled() {
        return requiredHandlerAuthenticationPolicyEnabled;
    }

    public void setRequiredHandlerAuthenticationPolicyEnabled(final boolean v) {
        this.requiredHandlerAuthenticationPolicyEnabled = v;
    }
    
    public static class NotPrevented {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class Any {
        private boolean tryAll;

        public boolean isTryAll() {
            return tryAll;
        }

        public void setTryAll(final boolean tryAll) {
            this.tryAll = tryAll;
        }
    }

    public static class All {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class Req {
        private boolean enabled;
        
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

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }
}
