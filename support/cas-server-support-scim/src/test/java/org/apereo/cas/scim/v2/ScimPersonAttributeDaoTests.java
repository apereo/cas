package org.apereo.cas.scim.v2;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.BaseScimTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScimPersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.attribute-repository.scim[0].target=http://localhost:9666/scim/v2",
    "cas.authn.attribute-repository.scim[0].username=scim-user",
    "cas.authn.attribute-repository.scim[0].password=changeit",
    "cas.authn.attribute-repository.scim[0].oauth-token=mfh834bsd202usn10snf",
    
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
class ScimPersonAttributeDaoTests extends BaseScimTests {
    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    private PersonAttributeDao attributeRepository;
    
    @Test
    void verifyAttributes() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val attributes = new HashMap<String, List<Object>>();
        attributes.put("resourceType", List.of("User"));
        attributes.put("nickName", List.of(UUID.randomUUID().toString()));
        attributes.put("displayName", List.of(UUID.randomUUID().toString()));
        attributes.put("givenName", List.of(UUID.randomUUID().toString()));
        attributes.put("familyName", List.of(UUID.randomUUID().toString()));
        attributes.put("middleName", List.of(UUID.randomUUID().toString()));
        attributes.put("email", List.of(UUID.randomUUID() + "@google.com"));
        attributes.put("phoneNumber", List.of("1234567890"));
        attributes.put("entitlements", List.of(UUID.randomUUID().toString()));
        attributes.put("roles", List.of(UUID.randomUUID().toString()));
        attributes.put("ims", List.of(UUID.randomUUID().toString()));
        attributes.put("addresses", List.of(UUID.randomUUID().toString()));
        attributes.put("employeeNumber", List.of(1234656));

        val principal = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(), attributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(principal), context);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, principalScimProvisionerAction.execute(context).getId());
        val people = attributeRepository.getPeople(Map.of("username", List.of(principal.getId())),
            PersonAttributeDaoFilter.alwaysChoose(), Set.of());
        val person = people.iterator().next();
        assertNotNull(person);
    }
}
