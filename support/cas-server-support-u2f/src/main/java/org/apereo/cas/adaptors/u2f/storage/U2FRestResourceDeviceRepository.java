package org.apereo.cas.adaptors.u2f.storage;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.http.HttpResponse;
import org.apereo.cas.configuration.model.support.mfa.U2FMultifactorProperties;
import org.apereo.cas.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link U2FRestResourceDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class U2FRestResourceDeviceRepository extends BaseResourceU2FDeviceRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(U2FRestResourceDeviceRepository.class);

    private final U2FMultifactorProperties.Rest restProperties;
    private final ObjectMapper mapper;

    public U2FRestResourceDeviceRepository(final LoadingCache<String, String> requestStorage,
                                           final long expirationTime, final TimeUnit expirationTimeUnit,
                                           final U2FMultifactorProperties.Rest restProperties) {
        super(requestStorage, expirationTime, expirationTimeUnit);
        this.restProperties = restProperties;
        mapper = new ObjectMapper()
                .findAndRegisterModules()
                .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    @Override
    protected Map<String, List<U2FDeviceRegistration>> readDevicesFromResource() {
        try {
            final HttpResponse response = HttpUtils.executeGet(restProperties.getUrl(), 
                    restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword());
            if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                return mapper.readValue(response.getEntity().getContent(),
                        new TypeReference<Map<String, List<U2FDeviceRegistration>>>() {
                        });
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>(0);
    }

    @Override
    protected void writeDevicesBackToResource(final List<U2FDeviceRegistration> list) {
        try (StringWriter writer = new StringWriter()) {
            final Map<String, List<U2FDeviceRegistration>> newDevices = new HashMap<>();
            newDevices.put(MAP_KEY_SERVICES, list);
            mapper.writer(new MinimalPrettyPrinter()).writeValue(writer, newDevices);
            HttpUtils.executePost(restProperties.getUrl(),
                    restProperties.getBasicAuthUsername(),
                    restProperties.getBasicAuthPassword(),
                    writer.toString());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
