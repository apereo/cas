package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.config.CasSupportActionsAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

/**
 * This is {@link AbstractWebflowActionsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ImportAutoConfiguration(CasSupportActionsAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
public abstract class AbstractWebflowActionsTests extends AbstractCentralAuthenticationServiceTests {
}
