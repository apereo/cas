package org.apereo.cas.adaptors.u2f.storage;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

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
public class U2FJsonResourceDeviceRepository extends BaseResourceU2FDeviceRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(U2FJsonResourceDeviceRepository.class);

    private final ObjectMapper mapper;

    private final Resource jsonResource;

    public U2FJsonResourceDeviceRepository(final LoadingCache<String, String> requestStorage,
                                           final Resource jsonResource,
                                           final long expirationTime, final TimeUnit expirationTimeUnit) {
        super(requestStorage, expirationTime, expirationTimeUnit);
        try {
            this.jsonResource = jsonResource;

            mapper = new ObjectMapper()
                    .findAndRegisterModules()
                    .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
            if (!this.jsonResource.exists()) {
                if (this.jsonResource.getFile().createNewFile()) {
                    LOGGER.debug("Created JSON resource [{}] for U2F device registrations", jsonResource);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, List<U2FDeviceRegistration>> readDevicesFromResource() throws Exception {
        if (!this.jsonResource.getFile().exists() || this.jsonResource.getFile().length() <= 0) {
            LOGGER.debug("JSON resource [{}] does not exist or is empty", jsonResource);
            return new HashMap<>(0);
        }
        return mapper.readValue(jsonResource.getInputStream(),
                new TypeReference<Map<String, List<U2FDeviceRegistration>>>() {
                });
    }

    @Override
    public void writeDevicesBackToResource(final List<U2FDeviceRegistration> list) throws Exception {
        final Map<String, List<U2FDeviceRegistration>> newDevices = new HashMap<>();
        newDevices.put(MAP_KEY_SERVICES, list);
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonResource.getFile(), newDevices);
        LOGGER.debug("Saved [{}] device(s) into repository [{}]", list.size(), jsonResource);
    }
}
