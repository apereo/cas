package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties class for cas.authn.policy.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
public class AuthenticationPolicyProperties implements Serializable {

    private static final long serialVersionUID = 2039700004862120066L;
    /**
     * Global authentication policy that is applied when CAS attempts to vend and validate tickets.
     * Checks to make sure a particular authentication handler has successfully executed and validated credentials.
     * Required handlers are defined per registered service.
     */
    private boolean requiredHandlerAuthenticationPolicyEnabled;

    /**
     * Satisfied if any authentication handler succeeds.
     * Allows options to avoid short circuiting and try every handler even if one prior succeeded.
     */
    private Any any = new Any();

    /**
     * Satisfied if an only if a specified handler successfully authenticates its credential.
     */
    private Req req = new Req();

    /**
     * Satisfied if and only if all given credentials are successfully authenticated.
     * Support for multiple credentials is new in CAS and this handler would
     * only be acceptable in a multi-factor authentication situation.
     */
    private All all = new All();

    /**
     * Execute a groovy script to detect authentication policy.
     */
    private List<GroovyAuthenticationPolicyProperties> groovy = new ArrayList<>();

    /**
     * Execute a rest endpoint to detect authentication policy.
     */
    private List<RestAuthenticationPolicyProperties> rest = new ArrayList<>();

    /**
     * Satisfied if an only if the authentication event is not blocked by a {@code PreventedException}.
     */
    private NotPrevented notPrevented = new NotPrevented();

    /**
     * Satisfied if an only if the principal has not already authenticated
     * and does not have an sso session with CAS. Otherwise, prevents
     * the user from logging in more than once. Note that this policy
     * adds an extra burden to the ticket store/registry as CAS needs
     * to query all relevant tickets found in the registry to cross-check
     * the requesting username with existing tickets.
     */
    private UniquePrincipal uniquePrincipal = new UniquePrincipal();
    
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

    public UniquePrincipal getUniquePrincipal() {
        return uniquePrincipal;
    }

    public void setUniquePrincipal(final UniquePrincipal uniquePrincipal) {
        this.uniquePrincipal = uniquePrincipal;
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

    public static class NotPrevented implements Serializable {
        private static final long serialVersionUID = -4930217018850738715L;
        /**
         * Enables the policy.
         */
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class UniquePrincipal implements Serializable {
        private static final long serialVersionUID = -4930217087310738715L;
        /**
         * Enables the policy.
         */
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Any implements Serializable {
        private static final long serialVersionUID = 4600357071276768175L;
        /**
         * Avoid short circuiting and try every handler even if one prior succeeded.
         * Ensure number of provided credentials does not match the sum of authentication successes and failures
         */
        private boolean tryAll;

        public boolean isTryAll() {
            return tryAll;
        }

        public void setTryAll(final boolean tryAll) {
            this.tryAll = tryAll;
        }
    }

    public static class All implements Serializable {

        private static final long serialVersionUID = 928409456096460793L;
        /**
         * Enables the policy.
         */
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Req implements Serializable {
        private static final long serialVersionUID = -4206244023952305821L;
        /**
         * Enables the policy.
         */
        private boolean enabled;

        /**
         * Ensure number of provided credentials does not match the sum of authentication successes and failures.
         */
        private boolean tryAll;

        /**
         * The handler name which must have successfully executed and validated credentials.
         */
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

    public List<GroovyAuthenticationPolicyProperties> getGroovy() {
        return groovy;
    }

    public void setGroovy(final List<GroovyAuthenticationPolicyProperties> groovy) {
        this.groovy = groovy;
    }

    public List<RestAuthenticationPolicyProperties> getRest() {
        return rest;
    }

    public void setRest(final List<RestAuthenticationPolicyProperties> rest) {
        this.rest = rest;
    }
}
