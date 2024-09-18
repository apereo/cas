package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MutableCredential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationRequest;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialParser;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Objects;

/**
 * This is {@link SurrogateInitialAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SurrogateInitialAuthenticationAction extends BaseCasWebflowAction {
    private final SurrogateCredentialParser surrogateCredentialParser;

    @Override
    protected Event doExecuteInternal(final RequestContext context) {
        val credential = WebUtils.getCredential(context, Credential.class);
        if (credential instanceof final MutableCredential mc) {
            val surrogateRequest = surrogateCredentialParser.parse(mc);
            surrogateRequest.ifPresentOrElse(payload -> addSurrogateInformation(context, payload),
                () -> removeSurrogateInformation(context, Objects.requireNonNull(mc)));
        }
        return null;
    }

    private static void addSurrogateInformation(final RequestContext context, final SurrogateAuthenticationRequest surrogateRequest) {
        val credential = surrogateRequest.getCredential();
        credential.setId(surrogateRequest.getUsername());

        if (surrogateRequest.hasSurrogateUsername()) {
            credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait(surrogateRequest.getSurrogateUsername()));
            WebUtils.putSurrogateAuthenticationRequest(context, Boolean.FALSE);
            LOGGER.debug("Converted credential to surrogate for username [{}] and assigned it to webflow", surrogateRequest.getUsername());
        } else {
            WebUtils.putSurrogateAuthenticationRequest(context, Boolean.TRUE);
            LOGGER.debug("No surrogate username is defined; Signal webflow to request for surrogate credentials");
        }
        WebUtils.putCredential(context, credential);
    }

    private static void removeSurrogateInformation(final RequestContext context, final MutableCredential credential) {
        credential.getCredentialMetadata().removeTrait(SurrogateCredentialTrait.class);
        WebUtils.putCredential(context, credential);
    }
}
