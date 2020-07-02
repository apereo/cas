package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyCouchbaseConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchbaseAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Import({
    CasAcceptableUsagePolicyCouchbaseConfiguration.class,
    BaseAcceptableUsagePolicyRepositoryTests.SharedTestConfiguration.class
})
@TestPropertySource(
    properties = {
        "cas.acceptable-usage-policy.couchbase.bucket=testbucket",
        "cas.acceptable-usage-policy.couchbase.cluster-username=admin",
        "cas.acceptable-usage-policy.couchbase.cluster-password=password"
    })
@Tag("Couchbase")
@EnabledIfPortOpen(port = 8091)
@Getter
public class CouchbaseAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {
    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Test
    public void verifyOperation() {
        assertNotNull(acceptableUsagePolicyRepository);
        val id = UUID.randomUUID().toString();
        verifyRepositoryAction(id,
            CollectionUtils.wrap(
                "accepted", List.of("false"),
                "email", List.of("CASuser@example.org")));
    }
}
