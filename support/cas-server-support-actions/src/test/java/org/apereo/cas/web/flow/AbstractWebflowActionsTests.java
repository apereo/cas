package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.config.CasSupportActionsConfiguration;

import org.springframework.context.annotation.Import;

/**
 * This is {@link AbstractWebflowActionsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Import(CasSupportActionsConfiguration.class)
public abstract class AbstractWebflowActionsTests extends AbstractCentralAuthenticationServiceTests {
}
