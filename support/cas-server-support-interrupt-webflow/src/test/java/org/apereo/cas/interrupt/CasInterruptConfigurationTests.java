package org.apereo.cas.interrupt;

import org.apereo.cas.config.CasInterruptConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasInterruptConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasInterruptConfiguration.class
}, properties = {
    "cas.interrupt.regex.attribute-name=attr-name",
    "cas.interrupt.regex.attribute-value=attr-value",
    "cas.interrupt.json.location=classpath:/interrupt.json",
    "cas.interrupt.groovy.location=classpath:/interrupt.groovy",
    "cas.interrupt.rest.url=http://localhost:1234"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("CasConfiguration")
public class CasInterruptConfigurationTests {
    @Autowired
    @Qualifier("interruptInquirer")
    private InterruptInquiryExecutionPlan interruptInquirer;

    @Test
    public void verifyOperation() {
        assertEquals(4, interruptInquirer.getInterruptInquirers().size());
    }

}
