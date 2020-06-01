package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.config.U2FMongoDbConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FMongoDbDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("MongoDb")
@EnabledIfPortOpen(port = 27017)
@SpringBootTest(classes = {
    U2FMongoDbConfiguration.class,
    U2FConfiguration.class,
    CasCoreHttpConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.authn.mfa.u2f.mongo.databaseName=mfa-trusted",
        "cas.authn.mfa.u2f.mongo.host=localhost",
        "cas.authn.mfa.u2f.mongo.port=27017",
        "cas.authn.mfa.u2f.mongo.userId=root",
        "cas.authn.mfa.u2f.mongo.password=secret",
        "cas.authn.mfa.u2f.mongo.authenticationDatabaseName=admin",
        "cas.authn.mfa.u2f.mongo.dropCollection=true"
    })
@Getter
public class U2FMongoDbDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {
    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository deviceRepository;

    @Test
    public void verifyOperation() {
        assertNotNull(deviceRepository);
    }
}
