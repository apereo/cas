package org.apereo.cas.support.realm;

import module java.base;
import lombok.val;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RealmPasswordVerificationCallbackHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
class RealmPasswordVerificationCallbackHandlerTests {
    @Test
    void verifyToken() {
        val realm = new RealmPasswordVerificationCallbackHandler("password".toCharArray());
        val callback = new WSPasswordCallback("casuser", "password", "type", WSPasswordCallback.USERNAME_TOKEN);
        assertDoesNotThrow(() -> {
            realm.handle(new Callback[]{callback});
            assertEquals(new String(realm.getPassword()), callback.getPassword());
        });
    }
}
