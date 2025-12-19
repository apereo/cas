package org.apereo.cas.qr.authentication;

import module java.base;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.springframework.core.io.Resource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

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

    private final Resource jsonResource;

    public JsonResourceQRAuthenticationDeviceRepository(final Resource jsonResource) {
        this.jsonResource = jsonResource;
        if (ResourceUtils.isFile(jsonResource) && !ResourceUtils.doesResourceExist(jsonResource)) {
            FunctionUtils.doUnchecked(_ -> {
                val res = jsonResource.getFile().createNewFile();
                if (res) {
                    LOGGER.debug("Created JSON resource @ [{}]", jsonResource);
                }
            });
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

    private void writeToJsonResource() {
        FunctionUtils.doUnchecked(_ -> MAPPER.writerWithDefaultPrettyPrinter().writeValue(jsonResource.getFile(), devices));
    }

    private void readFromJsonResource() {
        if (ResourceUtils.doesResourceExist(jsonResource)) {
            FunctionUtils.doUnchecked(_ -> {
                try (val reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
                    val personList = new TypeReference<Map<String, String>>() {
                    };
                    val results = MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
                    devices.clear();
                    devices.putAll(results);
                }
            });
        }
    }

}
