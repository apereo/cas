
package org.apereo.cas;

import org.apereo.cas.ticket.registry.DynamoDbTicketRegistryFacilitatorTests;
import org.apereo.cas.ticket.registry.DynamoDbTicketRegistryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    DynamoDbTicketRegistryTests.class,
    DynamoDbTicketRegistryFacilitatorTests.class
})
@Suite
public class AllTestsSuite {
}
