package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalScimV2ProvisionerActionWithScimServerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@TestPropertySource(properties = {
    "cas.scim.target=http://localhost:9666/scim/v2",
    "cas.scim.username=scim-user",
    "cas.scim.password=changeit",
    "cas.scim.oauth-token=mfh834bsd202usn10snf"
})
@Tag("SCIM")
@EnabledIfListeningOnPort(port = 9666)
class PrincipalScimV2ProvisionerActionWithScimServerTests extends BaseScimProvisionerActionTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val attributes = new HashMap<String, List<Object>>();
        attributes.put("externalId", List.of(UUID.randomUUID().toString()));
        attributes.put("resourceType", List.of("User"));
        attributes.put("nickName", List.of(UUID.randomUUID().toString()));
        attributes.put("displayName", List.of(UUID.randomUUID().toString()));
        attributes.put("givenName", List.of(UUID.randomUUID().toString()));
        attributes.put("familyName", List.of(UUID.randomUUID().toString()));
        attributes.put("middleName", List.of(UUID.randomUUID().toString()));
        attributes.put("email", List.of(UUID.randomUUID() + "@google.com"));
        attributes.put("phoneNumber", List.of("1234567890"));
        val principal = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(), attributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(principal), context);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, principalScimProvisionerAction.execute(context).getId());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, principalScimProvisionerAction.execute(context).getId());
    }
}
