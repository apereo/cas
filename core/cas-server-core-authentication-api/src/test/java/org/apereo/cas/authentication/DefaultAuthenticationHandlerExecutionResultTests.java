package org.apereo.cas.authentication;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAuthenticationHandlerExecutionResultTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationHandler")
public class DefaultAuthenticationHandlerExecutionResultTests {

    @Test
    public void verifyOperation() {
        val otp = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val res = new DefaultAuthenticationHandlerExecutionResult(new SimpleTestUsernamePasswordAuthenticationHandler(),
            new BasicCredentialMetaData(otp), CollectionUtils.wrapList(new DefaultMessageDescriptor("code1")));
        assertFalse(res.getWarnings().isEmpty());
        res.clearWarnings();
        assertTrue(res.getWarnings().isEmpty());
    }

}
