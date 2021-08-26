package org.apereo.cas.support.realm;

import lombok.val;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import javax.security.auth.callback.Callback;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RealmPasswordVerificationCallbackHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
public class RealmPasswordVerificationCallbackHandlerTests {
    @Test
    public void verifyToken() {
        val realm = new RealmPasswordVerificationCallbackHandler("password");
        val callback = new WSPasswordCallback("casuser", "password", "type", WSPasswordCallback.USERNAME_TOKEN);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                realm.handle(new Callback[]{callback});
                assertEquals(realm.getPassword(), callback.getPassword());
            }
        });
    }
}
