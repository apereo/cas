package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.WebAuthnCredential;
import org.apereo.cas.webauthn.WebAuthnMultifactorAuthenticationProvider;

import com.yubico.core.RegistrationStorage;
import com.yubico.core.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnValidateSessionCredentialTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class WebAuthnValidateSessionCredentialTokenAction extends AbstractMultifactorAuthenticationAction<WebAuthnMultifactorAuthenticationProvider> {
    private final RegistrationStorage webAuthnCredentialRepository;

    private final SessionManager sessionManager;

    private final PrincipalFactory principalFactory;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val token = request.getParameter("token");
        if (StringUtils.isBlank(token)) {
            LOGGER.warn("Missing web authn token from the request");
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE);
        }

        LOGGER.debug("Received web authn token [{}]", token);
        val credential = new WebAuthnCredential(token);
        WebUtils.putCredential(requestContext, credential);

        val session = sessionManager.getSession(WebAuthnCredential.from(credential));
        if (session.isEmpty()) {
            LOGGER.warn("Unable to locate existing session from the current token [{}]", token);
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE);
        }
        val result = webAuthnCredentialRepository.getUsernameForUserHandle(session.get());
        if (result.isEmpty()) {
            LOGGER.warn("Unable to locate user based on the given user handle");
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE);
        }
        val username = result.get();
        return FunctionUtils.doUnchecked(() -> {
            val authentication = DefaultAuthenticationBuilder.newInstance()
                .addCredential(credential)
                .setPrincipal(principalFactory.createPrincipal(username))
                .build();
            LOGGER.debug("Finalized authentication attempt based on [{}]", authentication);
            WebUtils.putAuthentication(authentication, requestContext);
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_FINALIZE);
        });
    }
}
