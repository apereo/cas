package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FRestResourceDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
@SpringBootTest(classes = {
    U2FConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = "cas.authn.mfa.u2f.rest.url=http://localhost:9196")
@Getter
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FRestResourceDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        .findAndRegisterModules();

    private static MockWebServer WEB_SERVER;
    
    private static List<U2FDeviceRegistration> DEVICES = new ArrayList<>();

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository deviceRepository;

    @BeforeAll
    public static void beforeClass() {
        WEB_SERVER = new MockWebServer(9196);
        WEB_SERVER.start();
    }

    @Override
    @BeforeEach
    @Synchronized
    public void setUp() {
        DEVICES.clear();
        configureResponse();
    }

    @AfterAll
    public static void afterClass() {
        WEB_SERVER.close();
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
        configureResponse();
        return devices;
    }

    @SneakyThrows
    @Synchronized
    private static void configureResponse() {
        val results = new HashMap<String, List<U2FDeviceRegistration>>();
        results.put(BaseResourceU2FDeviceRepository.MAP_KEY_DEVICES, DEVICES);
        WEB_SERVER.responseBody(MAPPER.writeValueAsString(results));
    }

    @Override
    @SneakyThrows
    @Synchronized
    protected void deleteDevice(final U2FDeviceRepository deviceRepository, final U2FDeviceRegistration device) {
        DEVICES.removeIf(d -> d.getRecord().equals(device.getRecord()));
        configureResponse();
        super.deleteDevice(deviceRepository, device);
    }
}
