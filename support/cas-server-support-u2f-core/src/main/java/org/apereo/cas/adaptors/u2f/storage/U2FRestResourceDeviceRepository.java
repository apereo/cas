package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.configuration.model.support.mfa.U2FMultifactorProperties;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpStatus;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link U2FRestResourceDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class U2FRestResourceDeviceRepository extends BaseResourceU2FDeviceRepository {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

    private final U2FMultifactorProperties.Rest restProperties;

    public U2FRestResourceDeviceRepository(final LoadingCache<String, String> requestStorage,
                                           final long expirationTime, final TimeUnit expirationTimeUnit,
                                           final U2FMultifactorProperties.Rest restProperties) {
        super(requestStorage, expirationTime, expirationTimeUnit);
        this.restProperties = restProperties;
    }

    @Override
    public Map<String, List<U2FDeviceRegistration>> readDevicesFromResource() {
        HttpResponse response = null;
        try {
            response = HttpUtils.executeGet(restProperties.getUrl(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword());
            if (Objects.requireNonNull(response).getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                return MAPPER.readValue(response.getEntity().getContent(),
                    new TypeReference<>() {
                    });
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return new HashMap<>(0);
    }

    @Override
    public void writeDevicesBackToResource(final List<U2FDeviceRegistration> list) {
        HttpResponse response = null;
        try (val writer = new StringWriter()) {
            val newDevices = new HashMap<String, List<U2FDeviceRegistration>>();
            newDevices.put(MAP_KEY_DEVICES, list);
            MAPPER.writer(new MinimalPrettyPrinter()).writeValue(writer, newDevices);
            response = HttpUtils.executePost(restProperties.getUrl(),
                restProperties.getBasicAuthUsername(),
                restProperties.getBasicAuthPassword(),
                writer.toString());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void removeAll() {
        HttpResponse response = null;
        try {
            response = HttpUtils.executeDelete(restProperties.getUrl(),
                restProperties.getBasicAuthUsername(),
                restProperties.getBasicAuthPassword());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
    }
}
