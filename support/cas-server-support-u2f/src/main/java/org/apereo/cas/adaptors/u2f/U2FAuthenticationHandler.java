package org.apereo.cas.adaptors.u2f;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import com.yubico.u2f.U2F;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.SignRequestData;
import com.yubico.u2f.data.messages.SignResponse;
import com.yubico.u2f.exceptions.DeviceCompromisedException;
import lombok.SneakyThrows;
import lombok.val;

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
    @SneakyThrows
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) {
        val tokenCredential = (U2FTokenCredential) credential;

        val authentication = WebUtils.getInProgressAuthentication();
        if (authentication == null) {
            throw new IllegalArgumentException("CAS has no reference to an authentication event to locate a principal");
        }
        val p = authentication.getPrincipal();

        val authenticateResponse = SignResponse.fromJson(tokenCredential.getToken());
        val authJson = u2FDeviceRepository.getDeviceAuthenticationRequest(authenticateResponse.getRequestId(), p.getId());
        val authenticateRequest = SignRequestData.fromJson(authJson);

        var registration = (DeviceRegistration) null;
        try {
            registration = u2f.finishSignature(authenticateRequest, authenticateResponse, u2FDeviceRepository.getRegisteredDevices(p.getId()));
            return createHandlerResult(tokenCredential, p);
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
