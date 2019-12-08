package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;

import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

/**
 * This is {@link AbstractWebflowActionsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Import(CasSupportActionsConfiguration.class)
@DirtiesContext
public class AbstractWebflowActionsTests extends AbstractCentralAuthenticationServiceTests {
}
