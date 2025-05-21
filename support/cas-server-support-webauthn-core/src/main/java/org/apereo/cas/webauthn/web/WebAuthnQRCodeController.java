package org.apereo.cas.webauthn.web;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SecurityContextUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.webauthn.WebAuthnCredential;
import com.yubico.core.RegistrationStorage;
import com.yubico.core.SessionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This is {@link WebAuthnQRCodeController}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
@Tag(name = "WebAuthN")
@RequestMapping(BaseWebAuthnController.BASE_ENDPOINT_WEBAUTHN)
public class WebAuthnQRCodeController extends BaseWebAuthnController {

    /**
     * Base endpoint.
     */
    public static final String ENDPOINT_QR_VERIFY = "/qrverify";

    protected final CasConfigurationProperties casProperties;
    protected final TicketRegistry ticketRegistry;
    protected final TicketFactory ticketFactory;
    protected final RegistrationStorage webAuthnCredentialRepository;
    protected final SessionManager sessionManager;
    protected final SecurityContextRepository securityContextRepository;

    /**
     * Start authentication model and view.
     *
     * @param request  the request
     * @param ticketId the ticket id
     * @return the model and view
     * @throws Throwable the throwable
     */
    @GetMapping(ENDPOINT_QR_VERIFY + "/{ticketId}")
    @Operation(summary = "Start WebAuthn QR code authentication",
        parameters = @Parameter(name = "ticketId", in = ParameterIn.PATH, required = true, description = "Ticket id"))
    public ModelAndView startAuthentication(
        final HttpServletRequest request,
        final HttpServletResponse response,
        @PathVariable("ticketId")
        final String ticketId) throws Throwable {
        try {
            verifyQRCodeAuthenticationIsEnabled();
            val transientTicket = ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
            Assert.isTrue(transientTicket != null && !transientTicket.isExpired(), "Ticket not found or has expired");
            val context = SecurityContextUtils.createSecurityContext(transientTicket, request);
            securityContextRepository.saveContext(context, request, response);

            val principal = transientTicket.getProperty(Principal.class.getName(), Principal.class);
            return new ModelAndView(CasWebflowConstants.VIEW_ID_WEBAUTHN_QRCODE_VERIFY,
                Map.of("QRCodeTicket", transientTicket, "principal", principal, "QRCodeAuthentication", true));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return new ModelAndView(CasWebflowConstants.VIEW_ID_WEBAUTHN_QRCODE_VERIFY_DONE, Map.of("success", false));
        }
    }

    /**
     * Check qr ticket status.
     *
     * @param request  the request
     * @param ticketId the ticket id
     * @return the response entity
     * @throws Throwable the throwable
     */
    @Operation(summary = "Check QR ticket status",
        parameters = @Parameter(name = "ticketId", in = ParameterIn.PATH, required = true, description = "Ticket id"))
    @GetMapping(ENDPOINT_QR_VERIFY + "/{ticketId}/status")
    @ResponseBody
    public ResponseEntity checkQRTicketStatus(
        final HttpServletRequest request,
        @PathVariable("ticketId")
        final String ticketId) throws Throwable {
        try {
            verifyQRCodeAuthenticationIsEnabled();
            val transientTicket = ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
            Assert.isTrue(transientTicket != null && !transientTicket.isExpired(), "Ticket not found or has expired");
            val webAuthnCredential = transientTicket.getProperty(WebAuthnCredential.class.getName(), WebAuthnCredential.class);
            if (webAuthnCredential == null) {
                return ResponseEntity.unprocessableEntity().body(
                    Map.of("ticketId", ticketId, "message", "WebAuthn credential not found in the ticket"));
            }
            Assert.notNull(webAuthnCredential, "WebAuthn credential not found in the ticket");
            val principal = transientTicket.getProperty(Principal.class.getName(), Principal.class);

            val session = sessionManager.getSession(WebAuthnCredential.from(webAuthnCredential));
            Assert.isTrue(session.isPresent(), "Session is not found for the given credential");
            ticketRegistry.deleteTicket(ticketId);
            return ResponseEntity.ok(Map.of("principal", principal, "sessionToken", webAuthnCredential.getToken()));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return ResponseEntity.badRequest().body(
                Map.of("ticketId", ticketId, "message", e.getMessage()));
        }
    }

    /**
     * Verify code.
     *
     * @param sessionToken the session token
     * @param ticketId     the ticket id
     * @param principalId  the principal id
     * @return the model and view
     * @throws Throwable the throwable
     */
    @PostMapping(ENDPOINT_QR_VERIFY)
    @ResponseBody
    @Operation(summary = "Verify QR code token",
        parameters = {
            @Parameter(name = "token", required = true, in = ParameterIn.QUERY, description = "Session token"),
            @Parameter(name = "ticket", required = true, in = ParameterIn.QUERY, description = "Ticket id"),
            @Parameter(name = "principal", required = true, in = ParameterIn.QUERY, description = "Principal id")
        })
    public ModelAndView verifyCode(
        @RequestParam("token")
        final String sessionToken,
        @RequestParam("ticket")
        final String ticketId,
        @RequestParam("principal")
        final String principalId) throws Throwable {
        try {
            verifyQRCodeAuthenticationIsEnabled();
            val transientTicket = ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
            Assert.isTrue(transientTicket != null && !transientTicket.isExpired(), "Ticket is not found or has expired");
            Assert.isTrue(webAuthnCredentialRepository.userExists(principalId), "Principal not found");
            val principal = transientTicket.getProperty(Principal.class.getName(), Principal.class);
            val credential = new WebAuthnCredential(sessionToken);
            val session = sessionManager.getSession(WebAuthnCredential.from(credential));
            FunctionUtils.throwIf(session.isEmpty(), () -> new IllegalStateException("Session not found for the given credential"));
            val result = webAuthnCredentialRepository.getUsernameForUserHandle(session.get());
            FunctionUtils.throwIf(result.isEmpty(), () -> new IllegalStateException("Unable to locate user based on the given user handle"));
            transientTicket.putProperty(WebAuthnCredential.class.getName(), credential);
            ticketRegistry.updateTicket(transientTicket);
            return new ModelAndView(CasWebflowConstants.VIEW_ID_WEBAUTHN_QRCODE_VERIFY_DONE,
                Map.of("principal", principal, "success", true));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            ticketRegistry.deleteTicket(ticketId);
            return new ModelAndView(CasWebflowConstants.VIEW_ID_WEBAUTHN_QRCODE_VERIFY_DONE, Map.of("success", false));
        }
    }

    private boolean isQRCodeAuthenticationEnabled() {
        return casProperties.getAuthn().getMfa().getWebAuthn().getCore().isQrCodeAuthenticationEnabled();
    }

    protected void verifyQRCodeAuthenticationIsEnabled() throws Throwable {
        FunctionUtils.throwIf(!isQRCodeAuthenticationEnabled(), () -> new NotImplementedException("QR code authentication is not enabled"));
    }
}
