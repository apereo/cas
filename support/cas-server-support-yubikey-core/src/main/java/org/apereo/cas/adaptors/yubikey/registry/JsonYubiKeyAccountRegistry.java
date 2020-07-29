package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link JsonYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JsonYubiKeyAccountRegistry extends PermissiveYubiKeyAccountRegistry {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        .findAndRegisterModules();

    private final Resource jsonResource;

    public JsonYubiKeyAccountRegistry(final Resource jsonResource, final YubiKeyAccountValidator validator) {
        super(getDevicesFromJsonResource(jsonResource), validator);
        this.jsonResource = jsonResource;
    }

    @SneakyThrows
    @Override
    public boolean registerAccountFor(final YubiKeyDeviceRegistrationRequest request) {
        val accountValidator = getAccountValidator();
        if (accountValidator.isValid(request.getUsername(), request.getToken())) {
            val yubikeyPublicId = accountValidator.getTokenPublicId(request.getToken());
            val file = jsonResource.getFile();

            val device = YubiKeyRegisteredDevice.builder()
                .id(System.currentTimeMillis())
                .name(request.getName())
                .publicId(getCipherExecutor().encode(yubikeyPublicId))
                .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
                .build();

            if (devices.containsKey(request.getUsername())) {
                val account = devices.get(request.getUsername());
                account.getDevices().add(device);
                this.devices.put(request.getUsername(), account);
            } else {
                val account = YubiKeyAccount.builder()
                    .username(request.getUsername())
                    .devices(CollectionUtils.wrapList(device))
                    .build();
                this.devices.put(request.getUsername(), account);
            }
            MAPPER.writer().withDefaultPrettyPrinter().writeValue(file, this.devices);
            return true;
        }
        return false;
    }

    @Override
    @SneakyThrows
    public void delete(final String uid) {
        this.devices.remove(uid);
        val file = jsonResource.getFile();
        MAPPER.writer().withDefaultPrettyPrinter().writeValue(file, this.devices);
    }

    @Override
    @SneakyThrows
    public void deleteAll() {
        this.devices.clear();
        val file = jsonResource.getFile();
        MAPPER.writer().withDefaultPrettyPrinter().writeValue(file, this.devices);
    }

    @SneakyThrows
    private static Map<String, YubiKeyAccount> getDevicesFromJsonResource(final Resource jsonResource) {
        if (!ResourceUtils.doesResourceExist(jsonResource)) {
            val res = jsonResource.getFile().createNewFile();
            if (res) {
                LOGGER.debug("Created JSON resource @ [{}]", jsonResource);
            }
        }
        if (ResourceUtils.doesResourceExist(jsonResource)) {
            val file = jsonResource.getFile();
            if (file.canRead() && file.length() > 0) {
                return MAPPER.readValue(file, new TypeReference<Map<String, YubiKeyAccount>>() {
                });
            }
        } else {
            LOGGER.warn("JSON resource @ [{}] does not exist", jsonResource);
        }
        return new HashMap<>(0);
    }
}
