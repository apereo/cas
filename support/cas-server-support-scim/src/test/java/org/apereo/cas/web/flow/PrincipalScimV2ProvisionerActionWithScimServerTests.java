package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalScimV2ProvisionerActionWithScimServerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@TestPropertySource(properties = {
    "cas.scim.target=http://localhost:9666/scim/v2",
    "cas.scim.version=2",
    "cas.scim.username=scim-user",
    "cas.scim.password=changeit"
})
@Tag("SCIM")
@EnabledIfPortOpen(port = 9666)
public class PrincipalScimV2ProvisionerActionWithScimServerTests extends BaseScimProvisionerActionTests {
    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, principalScimProvisionerAction.execute(context).getId());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, principalScimProvisionerAction.execute(context).getId());
    }
}
