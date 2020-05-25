package org.apereo.cas.web.support;

import org.apereo.cas.config.CasMongoDbThrottlingConfiguration;
import org.apereo.cas.config.CasSupportMongoDbAuditConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is  {@link MongoDbThrottledSubmissionHandlerInterceptorAdapterTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Tag("MongoDb")
@SpringBootTest(classes = {
    CasMongoDbThrottlingConfiguration.class,
    CasSupportMongoDbAuditConfiguration.class,
    BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.throttle.usernameParameter=username",
        "cas.authn.throttle.failure.range-seconds=5",
        "cas.audit.mongo.database-name=throttle",
        "cas.audit.mongo.host=localhost",
        "cas.audit.mongo.port=27017",
        "cas.audit.mongo.collection=MongoDbCasThrottleRepository",
        "cas.audit.mongo.drop-collection=true",
        "cas.audit.mongo.user-id=root",
        "cas.audit.mongo.password=secret",
        "cas.audit.mongo.authentication-database-name=admin",
        "cas.audit.mongo.asynchronous=false"
})
@Getter
@EnabledIfPortOpen(port = 27017)
public class MongoDbThrottledSubmissionHandlerInterceptorAdapterTests extends
    BaseThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier("authenticationThrottle")
    private ThrottledSubmissionHandlerInterceptor throttle;
}
