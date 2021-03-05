package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.support.WebUtils;

import com.unboundid.scim.data.Meta;
import com.unboundid.scim.data.Name;
import com.unboundid.scim.data.UserResource;
import com.unboundid.scim.marshal.json.JsonMarshaller;
import com.unboundid.scim.schema.CoreSchema;
import com.unboundid.scim.sdk.Resources;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalScimV1ProvisionerActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.scim.target=http://localhost:8215",
    "cas.scim.version=1",
    "cas.scim.username=casuser",
    "cas.scim.password=Mellon"
})
@Tag("WebflowActions")
@SuppressWarnings("JavaUtilDate")
public class PrincipalScimV1ProvisionerActionTests extends BaseScimProvisionerActionTests {
    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        val user = new UserResource(CoreSchema.USER_DESCRIPTOR);
        user.setActive(true);
        user.setDisplayName("CASUser");
        user.setId("casuser");
        val name = new Name("formatted", "family",
            "middle", "givenMame", "prefix", "prefix2");
        name.setGivenName("casuser");
        user.setName(name);
        val meta = new Meta(new Date(), new Date(), new URI("http://localhost:8215"), "1");
        meta.setCreated(new Date());
        user.setMeta(meta);

        val resources = new Resources(CollectionUtils.wrapList(user));
        val stream = new ByteArrayOutputStream();
        resources.marshal(new JsonMarshaller(), stream);
        val data = stream.toString();
        try (val webServer = new MockWebServer(8215,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, principalScimProvisionerAction.execute(context).getId());
        }
    }

    @Test
    public void verifyFailsAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
            request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, principalScimProvisionerAction.execute(context).getId());
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, principalScimProvisionerAction.execute(context).getId());
    }
}
