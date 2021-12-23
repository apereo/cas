package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * This is {@link BaseU2FDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class BaseU2FDeviceRepository implements U2FDeviceRepository {
    /**
     * CAS configuration settings.
     */
    protected final CasConfigurationProperties casProperties;

    private final LoadingCache<String, String> requestStorage;

    private final CipherExecutor<Serializable, String> cipherExecutor;

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
    public U2FDeviceRegistration verifyRegisteredDevice(final U2FDeviceRegistration registration) {
        val devices = getRegisteredDevices(registration.getUsername());
        val decoded = decode(registration);
        LOGGER.trace("Located devices [{}] for username [{}]", devices, registration.getUsername());
        val matched = devices.stream()
            .map(this::decode)
            .anyMatch(device -> device.matches(decoded));
        if (!matched) {
            throw new AuthenticationException("Failed to authenticate U2F device because "
                + "no matching record was found. Is the device registered?");
        }
        return registration;
    }

    /**
     * Gets device expiration starting from now.
     *
     * @return the device expiration
     */
    protected LocalDate getDeviceExpiration() {
        val expiration = casProperties.getAuthn().getMfa().getU2f().getCore().getExpireDevices();
        val expirationTimeUnit = casProperties.getAuthn().getMfa().getU2f().getCore().getExpireDevicesTimeUnit();
        return LocalDate.now(ZoneId.systemDefault()).minus(expiration, DateTimeUtils.toChronoUnit(expirationTimeUnit));
    }
}
