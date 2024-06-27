package org.apereo.cas.web.support;

import org.apereo.cas.config.CasMongoDbThrottlingAutoConfiguration;
import org.apereo.cas.config.CasSupportMongoDbAuditAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
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
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasMongoDbThrottlingAutoConfiguration.class,
    CasSupportMongoDbAuditAutoConfiguration.class,
    BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.throttle.core.username-parameter=username",
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
@EnabledIfListeningOnPort(port = 27017)
class MongoDbThrottledSubmissionHandlerInterceptorAdapterTests extends
    BaseThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
    private ThrottledSubmissionHandlerInterceptor throttle;
}
