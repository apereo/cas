package org.apereo.cas.webflow;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * This is {@link CasWebflowServerSessionContextConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = "cas.webflow.session.storage=true")
@Tag("Webflow")
@Getter
public class CasWebflowServerSessionContextConfigurationTests extends BaseCasWebflowSessionContextConfigurationTests {

    @Autowired
    @Qualifier("loginFlowExecutor")
    private FlowExecutor flowExecutor;
}
