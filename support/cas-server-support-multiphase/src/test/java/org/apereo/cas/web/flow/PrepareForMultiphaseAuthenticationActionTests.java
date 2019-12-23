import org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrepareForMultiphaseAuthenticationActionTests}.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
public class PrepareForMultiphaseAuthenticationActionTests extends BaseMultiphaseAuthenticationActiontests {
    @Autowired
    @Qualifier("initializeLoginAction")
    private Action initializeLoginAction;

    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(MultiphaseAuthenticationWebflowConfigurer.TRANSITION_ID_MULTIPHASE_GET_USERID, initializeLoginAction.execute(context).getId());
        WebUtils.putMultiphaseAuthenticationUsername(context, "casuser");
        assertEquals("success", initializeLoginAction.execute(context).getId());
    }
}
