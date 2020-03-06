package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * This is {@link JsonYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JsonYubiKeyAccountRegistry extends WhitelistYubiKeyAccountRegistry {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final Resource jsonResource;

    public JsonYubiKeyAccountRegistry(final Resource jsonResource, final YubiKeyAccountValidator validator) {
        super(getDevicesFromJsonResource(jsonResource), validator);
        this.jsonResource = jsonResource;
    }

    @SneakyThrows
    private static MultiValueMap<String, String> getDevicesFromJsonResource(final Resource jsonResource) {
        if (!ResourceUtils.doesResourceExist(jsonResource)) {
            val res = jsonResource.getFile().createNewFile();
            if (res) {
                LOGGER.debug("Created JSON resource @ [{}]", jsonResource);
            }
        }
        if (ResourceUtils.doesResourceExist(jsonResource)) {
            val file = jsonResource.getFile();
            if (file.canRead() && file.length() > 0) {
                return MAPPER.readValue(file, LinkedMultiValueMap.class);
            }
        } else {
            LOGGER.warn("JSON resource @ [{}] does not exist", jsonResource);
        }
        return new LinkedMultiValueMap<>(0);
    }

    @SneakyThrows
    @Override
    public boolean registerAccountFor(final String uid, final String token) {
        if (getAccountValidator().isValid(uid, token)) {
            val yubikeyPublicId = getAccountValidator().getTokenPublicId(token);
            val file = jsonResource.getFile();
            this.devices.add(uid, getCipherExecutor().encode(yubikeyPublicId));
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
}
