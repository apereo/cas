package org.apereo.cas.uma.web.controllers.authz;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaAuthorizationNeedInfoResponseTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
class UmaAuthorizationNeedInfoResponseTests {

    @Test
    void verifyOperation() {
        val info = new UmaAuthorizationNeedInfoResponse();
        info.setRedirectUser(true);
        info.setRequiredClaims(List.of("claim"));
        info.setRequiredScopes(List.of("scope"));
        info.setTicket("ticket");
        assertNotNull(info.toJson());
    }
}
