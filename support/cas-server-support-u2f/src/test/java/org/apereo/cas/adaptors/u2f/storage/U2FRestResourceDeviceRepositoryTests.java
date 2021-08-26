package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FRestResourceDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @author Hal Deadman
 * @since 5.3.0
 */
@Tag("RestfulApiAuthentication")
@SpringBootTest(classes = {
    U2FRestResourceDeviceRepositoryTests.RestfulServiceRegistryTestConfiguration.class,
    U2FConfiguration.class,
    AopAutoConfiguration.class
}, properties = {
    "server.port=9190",
    "cas.authn.mfa.u2f.rest.url=http://localhost:9190"
}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Getter
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAutoConfiguration
public class U2FRestResourceDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {

    private static List<U2FDeviceRegistration> DEVICES = new ArrayList<>();

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository deviceRepository;

    @Override
    @BeforeEach
    @Synchronized
    public void setUp() {
        DEVICES.clear();
    }

    @Test
    public void verifyOperation() {
        assertNotNull(deviceRepository);
    }

    @Override
    @SneakyThrows
    @Synchronized
    protected List<U2FDeviceRegistration> prepareDevices(final U2FDeviceRepository deviceRepository) {
        val devices = super.prepareDevices(deviceRepository);
        DEVICES.addAll(devices);
        return devices;
    }

    @TestConfiguration
    @Lazy(false)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public static class RestfulServiceRegistryTestConfiguration {

        @Autowired
        public void configureJackson(final ObjectMapper objectMapper) {
            objectMapper.findAndRegisterModules()
                .activateDefaultTyping(BasicPolymorphicTypeValidator.builder().build(),
                    ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        }

        @RestController("deviceRepositoryController")
        @RequestMapping("/")
        public static class DeviceRepositoryController {

            @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
            public void removeAll() {
                DEVICES.clear();
            }

            @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
            public void writeDevicesBackToResource(final ArrayList<U2FDeviceRegistration> devices) {
                DEVICES.addAll(devices);
            }

            @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
            public void deleteRegisteredDevice(@PathVariable(name = "id") final String id) {
                DEVICES.removeIf(d -> String.valueOf(d.getId()).equals(id));
            }

            @SneakyThrows
            @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
            public Map<String, List<U2FDeviceRegistration>> readDevicesFromResource() {
                val results = new HashMap<String, List<U2FDeviceRegistration>>();
                results.put(BaseResourceU2FDeviceRepository.MAP_KEY_DEVICES, DEVICES);
                return results;
            }
        }
    }
}
