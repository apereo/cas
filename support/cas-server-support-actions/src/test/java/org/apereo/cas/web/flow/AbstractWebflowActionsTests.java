package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.config.CasSupportActionsAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

/**
 * This is {@link AbstractWebflowActionsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ImportAutoConfiguration(CasSupportActionsAutoConfiguration.class)
public abstract class AbstractWebflowActionsTests extends AbstractCentralAuthenticationServiceTests {
}
