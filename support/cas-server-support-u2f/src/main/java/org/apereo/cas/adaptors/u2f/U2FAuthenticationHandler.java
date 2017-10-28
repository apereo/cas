package org.apereo.cas.adaptors.u2f;

import com.yubico.u2f.U2F;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.SignRequestData;
import com.yubico.u2f.data.messages.SignResponse;
import com.yubico.u2f.exceptions.DeviceCompromisedException;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * This is {@link U2FAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final U2F u2f = new U2F();
    private final U2FDeviceRepository u2FDeviceRepository;

    public U2FAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                    final U2FDeviceRepository u2FDeviceRepository) {
        super(name, servicesManager, principalFactory, null);
        this.u2FDeviceRepository = u2FDeviceRepository;
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws PreventedException {
        final U2FTokenCredential tokenCredential = (U2FTokenCredential) credential;

        final RequestContext context = RequestContextHolder.getRequestContext();
        if (context == null) {
            throw new IllegalArgumentException("No request context could be found to locate an authentication event");
        }
        final Authentication authentication = WebUtils.getAuthentication(context);
        if (authentication == null) {
            throw new IllegalArgumentException("Request context has no reference to an authentication event to locate a principal");
        }
        final Principal p = authentication.getPrincipal();

        final SignResponse authenticateResponse = SignResponse.fromJson(tokenCredential.getToken());
        final String authJson = u2FDeviceRepository.getDeviceAuthenticationRequest(authenticateResponse.getRequestId(), p.getId());
        final SignRequestData authenticateRequest = SignRequestData.fromJson(authJson);
        DeviceRegistration registration = null;
        try {
            registration = u2f.finishSignature(authenticateRequest, authenticateResponse, u2FDeviceRepository.getRegisteredDevices(p.getId()));
            return createHandlerResult(tokenCredential, p, null);
        } catch (final DeviceCompromisedException e) {
            registration = e.getDeviceRegistration();
            throw new PreventedException("Device possibly compromised and therefore blocked: " + e.getMessage(), e);
        } finally {
            u2FDeviceRepository.authenticateDevice(p.getId(), registration);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return U2FTokenCredential.class.isAssignableFrom(credential.getClass());
    }
}
