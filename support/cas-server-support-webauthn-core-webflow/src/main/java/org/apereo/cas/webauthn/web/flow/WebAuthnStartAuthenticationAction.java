package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.otp.util.QRUtils;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.WebAuthnMultifactorAuthenticationProvider;
import org.apereo.cas.webauthn.web.BaseWebAuthnController;
import org.apereo.cas.webauthn.web.WebAuthnQRCodeController;
import com.yubico.core.RegistrationStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;

/**
 * This is {@link WebAuthnStartAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class WebAuthnStartAuthenticationAction extends AbstractMultifactorAuthenticationAction<WebAuthnMultifactorAuthenticationProvider> {
    protected final CasConfigurationProperties casProperties;
    protected final TicketRegistry ticketRegistry;
    protected final TicketFactory ticketFactory;
    protected final RegistrationStorage webAuthnCredentialRepository;
    protected final TenantExtractor tenantExtractor;
    
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        LOGGER.trace("Checking registration record for [{}]", principal.getId());
        val registrations = webAuthnCredentialRepository.getRegistrationsByUsername(principal.getId());
        if (registrations.isEmpty()) {
            LOGGER.warn("No registration records could be found for [{}]", principal.getId());
            return error();
        }

        if (casProperties.getAuthn().getMfa().getWebAuthn().getCore().isQrCodeAuthenticationEnabled()) {
            val transientFactory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
            val ticket = transientFactory.create(Map.of(Principal.class.getName(), principal));
            val storedTicket = ticketRegistry.addTicket(ticket);
            val urlBuilder = new URIBuilder(casProperties.getServer().getPrefix());
            urlBuilder.appendPath(BaseWebAuthnController.BASE_ENDPOINT_WEBAUTHN);
            urlBuilder.appendPath(WebAuthnQRCodeController.ENDPOINT_QR_VERIFY);
            requestContext.getFlowScope().put("QRCodeUri", urlBuilder.toString());
            urlBuilder.appendPath(storedTicket.getId());
            val qrCodeBase64 = QRUtils.generateQRCode(urlBuilder.toString(), QRUtils.SIZE, QRUtils.SIZE);
            requestContext.getFlowScope().put("QRCode", qrCodeBase64);
            requestContext.getFlowScope().put("QRCodeTicket", storedTicket);
            WebUtils.putPrincipal(requestContext, principal);
        }

        return success();
    }
}
