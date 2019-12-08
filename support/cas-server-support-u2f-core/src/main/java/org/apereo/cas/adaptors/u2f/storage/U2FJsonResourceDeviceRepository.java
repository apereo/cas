package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.ResourceUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link U2FJsonResourceDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class U2FJsonResourceDeviceRepository extends BaseResourceU2FDeviceRepository {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

    private final Resource jsonResource;

    @SneakyThrows
    public U2FJsonResourceDeviceRepository(final LoadingCache<String, String> requestStorage,
                                           final Resource jsonResource,
                                           final long expirationTime, final TimeUnit expirationTimeUnit) {
        super(requestStorage, expirationTime, expirationTimeUnit);
        this.jsonResource = jsonResource;
        if (!ResourceUtils.doesResourceExist(this.jsonResource)) {
            if (this.jsonResource.getFile().createNewFile()) {
                LOGGER.debug("Created JSON resource [{}] for U2F device registrations", jsonResource);
            }
        }
    }

    @Override
    public Map<String, List<U2FDeviceRegistration>> readDevicesFromResource() throws Exception {
        if (!ResourceUtils.doesResourceExist(this.jsonResource)) {
            LOGGER.debug("JSON resource [{}] does not exist or is empty", jsonResource);
            return new HashMap<>(0);
        }
        return MAPPER.readValue(jsonResource.getInputStream(),
            new TypeReference<>() {
            });
    }

    @Override
    public void writeDevicesBackToResource(final List<U2FDeviceRegistration> list) throws Exception {
        val newDevices = new HashMap<String, List<U2FDeviceRegistration>>();
        newDevices.put(MAP_KEY_DEVICES, list);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(jsonResource.getFile(), newDevices);
        LOGGER.debug("Saved [{}] device(s) into repository [{}]", list.size(), jsonResource);
    }

    @Override
    public void removeAll() throws Exception {
        val newDevices = new HashMap<String, List<U2FDeviceRegistration>>();
        newDevices.put(MAP_KEY_DEVICES, new ArrayList<>(0));
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(jsonResource.getFile(), newDevices);
        LOGGER.debug("Removed all device(s) from repository [{}]", jsonResource);
    }
}
