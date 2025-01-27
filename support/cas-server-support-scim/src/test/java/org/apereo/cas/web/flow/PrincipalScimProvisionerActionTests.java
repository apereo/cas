package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalScimProvisionerActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@TestPropertySource(properties = {
    "cas.scim.target=http://localhost:9666/scim/v2",
    "cas.scim.username=scim-user",
    "cas.scim.password=changeit",
    "cas.scim.oauth-token=mfh834bsd202usn10snf",
    "cas.scim.schema-mappings.externalId=externalId",
    "cas.scim.schema-mappings.resourceType=resourceType",
    "cas.scim.schema-mappings.nickName=nickName",
    "cas.scim.schema-mappings.displayName=displayName",
    "cas.scim.schema-mappings.givenName=givenName",
    "cas.scim.schema-mappings.familyName=familyName",
    "cas.scim.schema-mappings.middleName=middleName",
    "cas.scim.schema-mappings.email=email",
    "cas.scim.schema-mappings.phoneNumber=phoneNumber",
    "cas.scim.schema-mappings.entitlements=entitlements",
    "cas.scim.schema-mappings.roles=roles",
    "cas.scim.schema-mappings.ims=ims",
    "cas.scim.schema-mappings.addresses=addresses",
    "cas.scim.schema-mappings.employeeNumber=employeeNumber"
})
@Tag("SCIM")
@EnabledIfListeningOnPort(port = 9666)
class PrincipalScimProvisionerActionTests extends BaseScimTests {
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
        attributes.put("email", List.of(UUID.randomUUID() + "@google.com", UUID.randomUUID() + "@yahoo.com"));
        attributes.put("phoneNumber", List.of("1234567890", "3477463543"));
        attributes.put("entitlements", List.of(UUID.randomUUID().toString()));
        attributes.put("roles", List.of(UUID.randomUUID().toString()));
        attributes.put("ims", List.of(UUID.randomUUID().toString()));
        attributes.put("addresses", List.of(UUID.randomUUID().toString()));

        val principal = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(), attributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(principal), context);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, principalScimProvisionerAction.execute(context).getId());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, principalScimProvisionerAction.execute(context).getId());
    }
}
