package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.ConditionalIgnoreRule;

import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class BaseSpringRunnableTicketRegistryTests extends BaseTicketRegistryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    public BaseSpringRunnableTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }
}
