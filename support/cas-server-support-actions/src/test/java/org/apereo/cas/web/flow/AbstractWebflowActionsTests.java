package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;

import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link AbstractWebflowActionsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Import({CasMultifactorAuthenticationWebflowConfiguration.class, CasSupportActionsConfiguration.class})
@DirtiesContext
@TestPropertySource(properties = "spring.aop.proxy-target-class=true")
public abstract class AbstractWebflowActionsTests extends AbstractCentralAuthenticationServiceTests {
}
