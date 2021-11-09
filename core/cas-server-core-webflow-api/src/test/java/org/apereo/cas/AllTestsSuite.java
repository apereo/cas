package org.apereo.cas;

import org.apereo.cas.web.flow.CasWebflowLoginContextProviderTests;
import org.apereo.cas.web.flow.actions.ConsumerExecutionActionTests;
import org.apereo.cas.web.flow.actions.StaticEventExecutionActionTests;
import org.apereo.cas.web.flow.configurer.DefaultCasWebflowExecutionPlanTests;
import org.apereo.cas.web.flow.configurer.DynamicFlowModelBuilderTests;
import org.apereo.cas.web.flow.decorator.GroovyLoginWebflowDecoratorTests;
import org.apereo.cas.web.flow.decorator.RestfulLoginWebflowDecoratorTests;
import org.apereo.cas.web.flow.executor.ClientFlowExecutionRepositoryTests;
import org.apereo.cas.web.flow.executor.EncryptedTranscoderTests;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfoTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    GroovyLoginWebflowDecoratorTests.class,
    RestfulLoginWebflowDecoratorTests.class,
    ClientFlowExecutionRepositoryTests.class,
    DefaultRegisteredServiceUserInterfaceInfoTests.class,
    DynamicFlowModelBuilderTests.class,
    ConsumerExecutionActionTests.class,
    CasWebflowLoginContextProviderTests.class,
    DefaultCasWebflowExecutionPlanTests.class,
    StaticEventExecutionActionTests.class,
    EncryptedTranscoderTests.class
})
@Suite
public class AllTestsSuite {
}
