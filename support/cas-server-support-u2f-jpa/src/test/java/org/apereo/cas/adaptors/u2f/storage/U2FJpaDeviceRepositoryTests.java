package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.config.U2FJpaConfiguration;
import org.apereo.cas.util.crypto.CertUtils;

import com.yubico.u2f.data.DeviceRegistration;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FJpaDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    U2FJpaConfiguration.class,
    U2FConfiguration.class,
    CasHibernateJpaConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = "cas.jdbc.show-sql=false"
)
@EnableTransactionManagement
@EnableAspectJAutoProxy
@Tag("JDBC")
@Getter
public class U2FJpaDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {
    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository deviceRepository;

    @Autowired
    @Qualifier("u2fDeviceRepositoryCleanerScheduler")
    private Runnable cleanerScheduler;

    @Test
    public void verifyOperation() {
        assertNotNull(deviceRepository);
    }

    @Test
    public void verifyCleaner() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                cleanerScheduler.run();
            }
        });
    }

    @Test
    public void verifyRegistrationAndAuthentication() throws Exception {
        val id = UUID.randomUUID().toString();
        val cert = CertUtils.readCertificate(new ClassPathResource("cert.crt"));
        val r1 = new DeviceRegistration("keyhandle11", "publickey1", cert, 1);
        val record1 = U2FDeviceRegistration.builder()
            .record(deviceRepository.getCipherExecutor().encode(r1.toJsonWithAttestationCert()))
            .username(id)
            .build();
        deviceRepository.registerDevice(record1);
        deviceRepository.verifyRegisteredDevice(record1);
        assertFalse(deviceRepository.getRegisteredDevices(id).isEmpty());
    }

    @Test
    public void verifyUpdateRegistration() throws Exception {
        val id = UUID.randomUUID().toString();
        val cert = CertUtils.readCertificate(new ClassPathResource("cert.crt"));
        val r1 = new DeviceRegistration("keyhandle11", "publickey1", cert, 1);
        var record1 = U2FDeviceRegistration.builder()
            .record(deviceRepository.getCipherExecutor().encode(r1.toJsonWithAttestationCert()))
            .username(id)
            .build();
        record1 = deviceRepository.registerDevice(record1);
        record1.setCreatedDate(LocalDate.now(Clock.systemUTC()).plusDays(5));
        deviceRepository.registerDevice(record1);
        assertEquals(1, deviceRepository.getRegisteredDevices(id).size());
    }

}
