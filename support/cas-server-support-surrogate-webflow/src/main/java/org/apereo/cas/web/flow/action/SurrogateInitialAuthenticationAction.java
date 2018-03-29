package org.apereo.cas.web.flow.action;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.flow.SurrogateWebflowEventResolver;
import org.apereo.cas.web.flow.actions.InitialAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SurrogateInitialAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class SurrogateInitialAuthenticationAction extends InitialAuthenticationAction {
    private final String separator;

    public SurrogateInitialAuthenticationAction(final CasDelegatingWebflowEventResolver delegatingWebflowEventResolver,
                                                final CasWebflowEventResolver webflowEventResolver,
                                                final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                final String separator) {
        super(delegatingWebflowEventResolver, webflowEventResolver, adaptiveAuthenticationPolicy);
        this.separator = separator;
    }

    @Override
    protected Event doPreExecute(final RequestContext context) {
        final var up = WebUtils.getCredential(context, UsernamePasswordCredential.class);
        if (up instanceof SurrogateUsernamePasswordCredential) {
            return null;
        }
        if (up.getUsername().contains(this.separator)) {
            convertToSurrogateCredential(context, up);
        }
        return null;
    }

    @Override
    protected void doPostExecute(final RequestContext context) {
        deconvertFromSurrogatePrincipal(context);
    }

    private void convertToSurrogateCredential(final RequestContext context, final UsernamePasswordCredential up) {
        final var sc = new SurrogateUsernamePasswordCredential();

        final var tUsername = up.getUsername();
        final var surrogateUsername = tUsername.substring(0, tUsername.indexOf(this.separator));
        final var realUsername = tUsername.substring(tUsername.indexOf(this.separator) + 1);

        if (StringUtils.isBlank(surrogateUsername)) {
            up.setUsername(realUsername);
            context.getFlowScope().put(SurrogateWebflowEventResolver.CONTEXT_ATTRIBUTE_REQUEST_SURROGATE, Boolean.TRUE);
            WebUtils.putCredential(context, up);
            return;
        }

        sc.setUsername(realUsername);
        sc.setSurrogateUsername(surrogateUsername);
        sc.setPassword(up.getPassword());
        if (up instanceof RememberMeCredential) {
            sc.setRememberMe(((RememberMeCredential) up).isRememberMe());
        }
        context.getFlowScope().put(SurrogateWebflowEventResolver.CONTEXT_ATTRIBUTE_REQUEST_SURROGATE, Boolean.FALSE);
        WebUtils.putCredential(context, sc);
    }

    private static void deconvertFromSurrogatePrincipal(final RequestContext context) {
        final var c = WebUtils.getCredential(context);
        if (c instanceof SurrogateUsernamePasswordCredential) {
            final var sc = SurrogateUsernamePasswordCredential.class.cast(c);
            final var up = new UsernamePasswordCredential();
            up.setUsername(sc.getUsername());
            up.setPassword(sc.getPassword());
            WebUtils.putCredential(context, up);
        }
    }
}
