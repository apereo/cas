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
import com.yubico.u2f.exceptions.U2fAuthenticationException;
import com.yubico.u2f.exceptions.U2fBadInputException;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link U2FAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final U2F u2f;
    private final U2FDeviceRepository u2FDeviceRepository;

    public U2FAuthenticationHandler(final String name,
                                    final ServicesManager servicesManager,
                                    final PrincipalFactory principalFactory,
                                    final U2FDeviceRepository u2FDeviceRepository,
                                    final U2F u2f,
                                    final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.u2f = u2f;
        this.u2FDeviceRepository = u2FDeviceRepository;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws PreventedException {
        val tokenCredential = (U2FTokenCredential) credential;

        val authentication = WebUtils.getInProgressAuthentication();
        if (authentication == null) {
            throw new IllegalArgumentException("CAS has no reference to an authentication event to locate a principal");
        }
        val principal = this.principalFactory.createPrincipal(authentication.getPrincipal().getId());
        var registration = (DeviceRegistration) null;

        try {
            val authenticateResponse = SignResponse.fromJson(tokenCredential.getToken());
            val requestId = authenticateResponse.getRequestId();
            val authJson = u2FDeviceRepository.getDeviceAuthenticationRequest(requestId, principal.getId());
            if (StringUtils.isBlank(authJson)) {
                throw new PreventedException("Could not get device authentication request from repository for request id " + requestId);
            }
            val authenticateRequest = SignRequestData.fromJson(authJson);
            val registeredDevices = u2FDeviceRepository.getRegisteredDevices(principal.getId());
            if (registeredDevices.isEmpty()) {
                throw new PreventedException("No registered devices could be found for " + principal.getId());
            }
            registration = u2f.finishSignature(authenticateRequest, authenticateResponse, registeredDevices);
            return createHandlerResult(tokenCredential, principal);
        } catch (final U2fBadInputException e) {
            throw new PreventedException("Could not accept/parse u2f request: " + e.getMessage(), e);
        } catch (final DeviceCompromisedException e) {
            registration = e.getDeviceRegistration();
            throw new PreventedException("Device possibly compromised and therefore blocked: " + e.getMessage(), e);
        } catch (final U2fAuthenticationException e) {
            throw new PreventedException("Could not authenticate device: " + e.getMessage(), e);
        } finally {
            if (registration != null) {
                u2FDeviceRepository.authenticateDevice(principal.getId(), registration);
            }
        }
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return U2FTokenCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return U2FTokenCredential.class.isAssignableFrom(credential.getClass());
    }
}
