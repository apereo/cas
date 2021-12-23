package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link U2FJsonResourceDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class U2FJsonResourceDeviceRepository extends BaseResourceU2FDeviceRepository {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @SneakyThrows
    public U2FJsonResourceDeviceRepository(final LoadingCache<String, String> requestStorage,
                                           final CasConfigurationProperties casProperties,
                                           final CipherExecutor<Serializable, String> cipherExecutor) {
        super(requestStorage, casProperties, cipherExecutor);
        val jsonResource = casProperties.getAuthn().getMfa().getU2f().getJson().getLocation();
        if (!ResourceUtils.doesResourceExist(jsonResource)) {
            if (jsonResource.getFile().createNewFile()) {
                LOGGER.debug("Created JSON resource [{}] for U2F device registrations", jsonResource);
            }
        }
    }

    @Override
    public Map<String, List<U2FDeviceRegistration>> readDevicesFromResource() throws Exception {
        val jsonResource = casProperties.getAuthn().getMfa().getU2f().getJson().getLocation();
        if (!ResourceUtils.doesResourceExist(jsonResource)) {
            LOGGER.debug("JSON resource [{}] does not exist or is empty", jsonResource);
            return new HashMap<>(0);
        }
        return MAPPER.readValue(jsonResource.getInputStream(),
            new TypeReference<>() {
            });
    }

    @Override
    public void writeDevicesBackToResource(final List<U2FDeviceRegistration> list) throws Exception {
        val jsonResource = casProperties.getAuthn().getMfa().getU2f().getJson().getLocation();
        val newDevices = new HashMap<String, List<U2FDeviceRegistration>>();
        newDevices.put(MAP_KEY_DEVICES, list);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(jsonResource.getFile(), newDevices);
        LOGGER.debug("Saved [{}] device(s) into repository [{}]", list.size(), jsonResource);
    }

    @Override
    public void removeAll() throws Exception {
        val jsonResource = casProperties.getAuthn().getMfa().getU2f().getJson().getLocation();
        val newDevices = new HashMap<String, List<U2FDeviceRegistration>>();
        newDevices.put(MAP_KEY_DEVICES, new ArrayList<>(0));
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(jsonResource.getFile(), newDevices);
        LOGGER.debug("Removed all device(s) from repository [{}]", jsonResource);
    }
}
