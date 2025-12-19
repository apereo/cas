package org.apereo.cas.webflow;

import module java.base;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * This is {@link CasWebflowClientSessionContextConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("Webflow")
@Getter
class CasWebflowClientSessionContextConfigurationTests extends BaseCasWebflowSessionContextConfigurationTests {

    @Autowired
    @Qualifier("loginFlowExecutor")
    private FlowExecutor flowExecutor;
}
