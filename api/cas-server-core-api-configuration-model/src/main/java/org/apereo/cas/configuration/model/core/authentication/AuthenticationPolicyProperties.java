package org.apereo.cas.configuration.model.core.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties class for cas.authn.policy.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Slf4j
@Getter
@Setter
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

    @Getter
    @Setter
    public static class NotPrevented implements Serializable {

        private static final long serialVersionUID = -4930217018850738715L;

        /**
         * Enables the policy.
         */
        private boolean enabled;
    }

    @Getter
    @Setter
    public static class UniquePrincipal implements Serializable {

        private static final long serialVersionUID = -4930217087310738715L;

        /**
         * Enables the policy.
         */
        private boolean enabled;
    }

    @Getter
    @Setter
    public static class Any implements Serializable {

        private static final long serialVersionUID = 4600357071276768175L;

        /**
         * Enables the policy.
         */
        private boolean enabled = true;

        /**
         * Avoid short circuiting and try every handler even if one prior succeeded.
         * Ensure number of provided credentials does not match the sum of authentication successes and failures
         */
        private boolean tryAll;
    }

    @Getter
    @Setter
    public static class All implements Serializable {

        private static final long serialVersionUID = 928409456096460793L;

        /**
         * Enables the policy.
         */
        private boolean enabled;
    }

    @Getter
    @Setter
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
    }
}
