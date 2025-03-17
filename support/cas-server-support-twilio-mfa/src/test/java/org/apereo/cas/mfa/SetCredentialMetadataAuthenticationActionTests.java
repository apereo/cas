package org.apereo.cas.mfa;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.mfa.twilio.CasTwilioMultifactorTokenCredential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.SetCredentialMetadataAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SetCredentialMetadataAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("WebflowActions")
class SetCredentialMetadataAuthenticationActionTests {
    @Test
    void verifyOperation() throws Exception {
        val requestContext = MockRequestContext.create();
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), requestContext);
        val credential = new CasTwilioMultifactorTokenCredential();
        credential.setToken(UUID.randomUUID().toString());
        WebUtils.putCredential(requestContext, credential);
        val action = new SetCredentialMetadataAuthenticationAction();
        action.execute(requestContext);
        assertTrue(credential.getCredentialMetadata().containsProperty(Authentication.class.getName()));
    }
}
