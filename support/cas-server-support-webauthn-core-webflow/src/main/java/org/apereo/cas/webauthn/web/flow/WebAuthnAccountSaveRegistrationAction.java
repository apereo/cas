package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.WebAuthnMultifactorAuthenticationProvider;
import com.yubico.core.RegistrationStorage;
import com.yubico.core.SessionManager;
import com.yubico.webauthn.data.ByteArray;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class WebAuthnAccountSaveRegistrationAction extends AbstractMultifactorAuthenticationAction<WebAuthnMultifactorAuthenticationProvider> {
    protected final RegistrationStorage webAuthnCredentialRepository;

    protected final SessionManager sessionManager;

    protected final TenantExtractor tenantExtractor;
    
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        val sessionToken = WebUtils.getRequestParameterOrAttribute(requestContext, "sessionToken").orElseThrow();
        LOGGER.trace("Checking registration record for [{}] by session id [{}]", principal.getId(), sessionToken);
        val token = ByteArray.fromBase64Url(sessionToken);
        val credentials = webAuthnCredentialRepository.getCredentialIdsForUsername(principal.getId());
        if (!credentials.isEmpty() && sessionManager.getSession(token).isPresent()) {
            return success();
        }
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        WebUtils.produceErrorView(request, HttpStatus.BAD_REQUEST, "Unable to verify registration record");
        return error();
    }
}
