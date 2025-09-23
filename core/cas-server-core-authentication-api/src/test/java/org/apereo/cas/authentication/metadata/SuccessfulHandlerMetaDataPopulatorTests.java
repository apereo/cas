package org.apereo.cas.authentication.metadata;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SuccessfulHandlerMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationHandler")
class SuccessfulHandlerMetaDataPopulatorTests {
    @Test
    void verifyOperation() {
        val input = new SuccessfulHandlerMetaDataPopulator();
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getWebApplicationService(),
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("cas"));
        val builder = new DefaultAuthenticationBuilder();
        input.populateAttributes(builder, transaction);
        assertFalse(builder.hasAttribute(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
        assertTrue(builder.getAttributes().get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS).isEmpty());
    }

}
