package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.u2f.data.DeviceRegistration;
import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

/**
 * This is {@link U2FRestResourceDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    U2FConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@TestPropertySource(properties = "cas.authn.mfa.u2f.rest.url=http://localhost:9196")
public class U2FRestResourceDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        .findAndRegisterModules();

    private static MockWebServer WEB_SERVER;

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository u2fDeviceRepository;

    @BeforeAll
    public static void beforeClass() throws Exception {
        val devices = new HashMap<String, List<U2FDeviceRegistration>>();
        val reg = new DeviceRegistration("123456", "bjsdghj3b", "njsdkhjdfjh45", 1, false);
        val device1 = new U2FDeviceRegistration(2000, "casuser", reg.toJson(), LocalDate.now());
        val device2 = new U2FDeviceRegistration(1000, "casuser", reg.toJson(), LocalDate.now());
        devices.put(BaseResourceU2FDeviceRepository.MAP_KEY_DEVICES, CollectionUtils.wrapList(device1, device2));
        val data = MAPPER.writeValueAsString(devices);
        WEB_SERVER = new MockWebServer(9196, data);
        WEB_SERVER.start();
    }

    @AfterAll
    public static void afterClass() {
        WEB_SERVER.close();
    }

    @Override
    protected U2FDeviceRepository getDeviceRepository() {
        return this.u2fDeviceRepository;
    }

    @Override
    protected void registerDevices(final U2FDeviceRepository deviceRepository) {
    }
}
