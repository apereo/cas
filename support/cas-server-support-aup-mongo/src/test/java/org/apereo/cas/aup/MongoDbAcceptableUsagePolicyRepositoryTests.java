package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyMongoDbConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MongoDb")
@Import(CasAcceptableUsagePolicyMongoDbConfiguration.class)
@EnabledIfPortOpen(port = 27017)
@TestPropertySource(properties = {
    "cas.acceptable-usage-policy.mongo.host=localhost",
    "cas.acceptable-usage-policy.mongo.port=27017",
    "cas.acceptable-usage-policy.mongo.dropCollection=true",
    "cas.acceptable-usage-policy.mongo.collection=acceptable-usage-policy",
    "cas.acceptable-usage-policy.mongo.user-id=root",
    "cas.acceptable-usage-policy.mongo.password=secret",
    "cas.acceptable-usage-policy.mongo.databaseName=acceptableUsagePolicy",
    "cas.acceptable-usage-policy.mongo.authenticationDatabaseName=admin",
    "cas.acceptable-usage-policy.aupAttributeName=accepted"
})
@Getter
public class MongoDbAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {

    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Test
    public void verifyOperation() {
        assertNotNull(acceptableUsagePolicyRepository);
        verifyRepositoryAction("casuser",
            CollectionUtils.wrap("accepted", List.of("false"), "email", List.of("CASuser@example.org")));
    }
}
