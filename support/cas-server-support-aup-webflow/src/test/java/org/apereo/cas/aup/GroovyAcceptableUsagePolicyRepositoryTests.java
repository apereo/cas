package org.apereo.cas.aup;

import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link GroovyAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Tag("Groovy")
@TestPropertySource(properties = "cas.acceptableUsagePolicy.groovy.location=classpath:/AcceptableUsagePolicy.groovy")
public class GroovyAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {

    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Test
    public void verifyRepositoryActionWithAdvancedConfig() {
        verifyRepositoryAction("casuser", CollectionUtils.wrap("aupAccepted", "false"));
    }
}
