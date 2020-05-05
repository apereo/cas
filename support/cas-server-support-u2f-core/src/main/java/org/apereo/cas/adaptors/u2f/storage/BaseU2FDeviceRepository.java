package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;
         
/**
 * This is {@link BaseU2FDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public abstract class BaseU2FDeviceRepository implements U2FDeviceRepository {

    private final LoadingCache<String, String> requestStorage;

    private CipherExecutor<Serializable, String> cipherExecutor;

    @Override
    public String getDeviceRegistrationRequest(final String requestId, final String username) {
        val request = requestStorage.get(requestId);
        requestStorage.invalidate(requestId);
        requestStorage.cleanUp();
        return request;
    }

    @Override
    public String getDeviceAuthenticationRequest(final String requestId, final String username) {
        val request = requestStorage.get(requestId);
        requestStorage.invalidate(requestId);
        requestStorage.cleanUp();
        return request;
    }

    @Override
    public void requestDeviceRegistration(final String requestId, final String username, final String registrationJsonData) {
        requestStorage.put(requestId, registrationJsonData);
    }

    @Override
    public void requestDeviceAuthentication(final String requestId, final String username, final String registrationJsonData) {
        requestStorage.put(requestId, registrationJsonData);
    }

    @Override
    public void authenticateDevice(final String username, final DeviceRegistration registration) {
        val devices = getRegisteredDevices(username);
        LOGGER.trace("Located devices [{}] for username [{}]", devices, username);
        val matched = devices.stream().anyMatch(d -> d.equals(registration));
        if (!matched) {
            throw new AuthenticationException("Failed to authenticate U2F device because "
                + "no matching record was found. Is the device registered?");
        }
    }
}
