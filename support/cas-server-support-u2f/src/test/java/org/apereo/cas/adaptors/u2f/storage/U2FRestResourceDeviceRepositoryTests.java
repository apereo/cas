package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.u2f.data.DeviceRegistration;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.time.LocalDate;
import java.time.ZoneId;
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

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository deviceRepository;

    @BeforeAll
    public static void beforeClass() throws Exception {
        val devices = new HashMap<String, List<U2FDeviceRegistration>>();
        val reg = new DeviceRegistration("123456", "bjsdghj3b", "njsdkhjdfjh45", 1, false);
        val device1 = new U2FDeviceRegistration(2000, "casuser", reg.toJsonWithAttestationCert(), LocalDate.now(ZoneId.systemDefault()));
        val device2 = new U2FDeviceRegistration(1000, "casuser", reg.toJsonWithAttestationCert(), LocalDate.now(ZoneId.systemDefault()));
        devices.put(BaseResourceU2FDeviceRepository.MAP_KEY_DEVICES, CollectionUtils.wrapList(device1, device2));
        val data = MAPPER.writeValueAsString(devices);
        WEB_SERVER = new MockWebServer(9196, data);
        WEB_SERVER.start();
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
    protected void registerDevices(final U2FDeviceRepository deviceRepository) {
    }
}
