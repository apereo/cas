package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.configuration.model.support.mfa.u2f.U2FRestfulMultifactorAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.Serializable;
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
public class U2FRestResourceDeviceRepository extends BaseResourceU2FDeviceRepository {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final U2FRestfulMultifactorAuthenticationProperties restProperties;

    public U2FRestResourceDeviceRepository(final LoadingCache<String, String> requestStorage,
                                           final long expirationTime,
                                           final TimeUnit expirationTimeUnit,
                                           final U2FRestfulMultifactorAuthenticationProperties restProperties,
                                           final CipherExecutor<Serializable, String> cipherExecutor) {
        super(requestStorage, expirationTime, expirationTimeUnit, cipherExecutor);
        this.restProperties = restProperties;
    }

    @Override
    @SneakyThrows
    public Map<String, List<U2FDeviceRegistration>> readDevicesFromResource() {
        HttpResponse response = null;
        try {
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(restProperties.getUrl())
                .build();

            response = HttpUtils.execute(exec);
            if (Objects.requireNonNull(response).getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                return MAPPER.readValue(response.getEntity().getContent(),
                    new TypeReference<>() {
                    });
            }
        } finally {
            HttpUtils.close(response);
        }
        return new HashMap<>(0);
    }

    @Override
    @SneakyThrows
    public void writeDevicesBackToResource(final List<U2FDeviceRegistration> list) {
        HttpResponse response = null;
        try (val writer = new StringWriter()) {
            val newDevices = new HashMap<String, List<U2FDeviceRegistration>>();
            newDevices.put(MAP_KEY_DEVICES, list);
            MAPPER.writer(new MinimalPrettyPrinter()).writeValue(writer, newDevices);

            val headers = CollectionUtils.<String, Object>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(restProperties.getHeaders());
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(restProperties.getUrl())
                .headers(headers)
                .entity(writer.toString())
                .build();

            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void deleteRegisteredDevice(final U2FDeviceRegistration registration) {
        HttpResponse response = null;
        try {
            val url = StringUtils.appendIfMissing(restProperties.getUrl(), "/") + registration.getId();
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(url)
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void removeAll() {
        HttpResponse response = null;
        try {
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(restProperties.getUrl())
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }
}
