package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.config.U2FMongoDbConfiguration;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

/**
 * This is {@link U2FMongoDbDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Category(MongoDbCategory.class)
@SpringBootTest(classes = {
    U2FMongoDbConfiguration.class,
    U2FConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class})
@TestPropertySource(properties = {
    "cas.authn.mfa.u2f.mongo.databaseName=mfa-trusted",
    "cas.authn.mfa.u2f.mongo.host=localhost",
    "cas.authn.mfa.u2f.mongo.port=27017",
    "cas.authn.mfa.u2f.mongo.userId=root",
    "cas.authn.mfa.u2f.mongo.password=secret",
    "cas.authn.mfa.u2f.mongo.authenticationDatabaseName=admin",
    "cas.authn.mfa.u2f.mongo.dropCollection=true"
    })
public class U2FMongoDbDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository u2fDeviceRepository;

    @Override
    protected U2FDeviceRepository getDeviceRepository() {
        return this.u2fDeviceRepository;
    }
}
