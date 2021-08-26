package org.apereo.cas.qr.authentication;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * This is {@link JsonResourceQRAuthenticationDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class JsonResourceQRAuthenticationDeviceRepository implements QRAuthenticationDeviceRepository {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final ConcurrentMap<String, String> devices = new ConcurrentHashMap<>();

    private final transient Resource jsonResource;

    @SneakyThrows
    public JsonResourceQRAuthenticationDeviceRepository(final Resource jsonResource) {
        this.jsonResource = jsonResource;
        if (ResourceUtils.isFile(jsonResource) && !ResourceUtils.doesResourceExist(jsonResource)) {
            val res = jsonResource.getFile().createNewFile();
            if (res) {
                LOGGER.debug("Created JSON resource @ [{}]", jsonResource);
            }
        }
        readFromJsonResource();
    }

    @Override
    public boolean isAuthorizedDeviceFor(final String deviceId, final String subject) {
        readFromJsonResource();
        return devices.containsKey(deviceId) && devices.get(deviceId).equals(subject);
    }

    @Override
    public void authorizeDeviceFor(final String deviceId, final String subject) {
        devices.put(deviceId, subject);
        writeToJsonResource();
    }

    @Override
    public void removeDevice(final String device) {
        devices.remove(device);
        writeToJsonResource();
    }

    @Override
    public void removeAll() {
        devices.clear();
        writeToJsonResource();
    }

    @Override
    public List<String> getAuthorizedDevicesFor(final String subject) {
        readFromJsonResource();
        return devices.entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(subject))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @SneakyThrows
    private void writeToJsonResource() {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(jsonResource.getFile(), devices);
    }

    @SneakyThrows
    private void readFromJsonResource() {
        if (ResourceUtils.doesResourceExist(jsonResource)) {
            try (val reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
                val personList = new TypeReference<Map<String, String>>() {
                };
                val results = MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
                devices.clear();
                devices.putAll(results);
            }
        }
    }

}
