package org.apereo.cas.adaptors.authy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;

import com.authy.api.Error;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;

/**
 * This is {@link AuthyClientInstanceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class
})
public class AuthyClientInstanceTests {
    @Test
    public void verifyAction() {
        try {
            val client = new AuthyClientInstance("apikey", "https://api.authy.com",
                "mail", "phone", "1");
            val user = client.getOrCreateUser(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("mail", "casuser@example.org", "phone", "123-456-6789")));
            assertNotNull(user);
            assertTrue(user.getId() <= 0);
            assertTrue(HttpStatus.valueOf(user.getStatus()).isError());

            val error = new Error();
            error.setCountryCode("1");
            error.setMessage("Error");
            error.setUrl("http://app.example.org");
            val msg = AuthyClientInstance.getErrorMessage(error);
            assertNotNull(msg);

        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
