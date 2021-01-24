package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

import java.util.Collection;
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

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final Resource jsonResource;

    public JsonYubiKeyAccountRegistry(final Resource jsonResource, final YubiKeyAccountValidator validator) {
        super(getDevicesFromJsonResource(jsonResource), validator);
        this.jsonResource = jsonResource;
    }

    @Override
    public void delete(final String uid) {
        super.delete(uid);
        writeDevicesToFile();
    }

    @Override
    public void delete(final String username, final long deviceId) {
        super.delete(username, deviceId);
        writeDevicesToFile();
    }

    @Override
    public void deleteAll() {
        super.deleteAll();
        writeDevicesToFile();
    }

    @SneakyThrows
    private void writeDevicesToFile() {
        val file = jsonResource.getFile();
        MAPPER.writer().withDefaultPrettyPrinter().writeValue(file, this.devices);
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccountsInternal() {
        this.devices.putAll(getDevicesFromJsonResource(this.jsonResource));
        return super.getAccountsInternal();
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
                return MAPPER.readValue(file, new TypeReference<>() {
                });
            }
        } else {
            LOGGER.warn("JSON resource @ [{}] does not exist", jsonResource);
        }
        return new HashMap<>(0);
    }

    @Override
    public YubiKeyAccount save(final YubiKeyDeviceRegistrationRequest request,
                                  final YubiKeyRegisteredDevice... device) {
        val acct = super.save(request, device);
        writeDevicesToFile();
        return acct;
    }

    @Override
    public boolean update(final YubiKeyAccount account) {
        val result = super.update(account);
        writeDevicesToFile();
        return result;
    }
}
