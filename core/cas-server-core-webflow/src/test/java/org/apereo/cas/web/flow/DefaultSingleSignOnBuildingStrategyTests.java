package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultSingleSignOnBuildingStrategyTests}.
 *
 * @author Brian Kerr
 * @since 7.2.0
 */
@SpringBootTest(classes = BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
@Tag("Authentication")
class DefaultSingleSignOnBuildingStrategyTests {

    @Autowired
    @Qualifier(SingleSignOnBuildingStrategy.BEAN_NAME)
    private DefaultSingleSignOnBuildingStrategy singleSignOnBuildingStrategy;

    @Test
    void verifyRemoveTicketGrantingTicketAndTicketIsNotFound() {
        assertDoesNotThrow(() -> singleSignOnBuildingStrategy.removeTicketGrantingTicket("TGT-1"));
    }
}
