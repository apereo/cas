package org.apereo.cas.ticket.registry;

import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public abstract class BaseSpringRunnableTicketRegistryTests extends BaseTicketRegistryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    public BaseSpringRunnableTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }
}
