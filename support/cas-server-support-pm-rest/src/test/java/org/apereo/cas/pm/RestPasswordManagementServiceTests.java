package org.apereo.cas.pm;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.PasswordManagementConfiguration;
import org.apereo.cas.config.RestPasswordManagementConfiguration;

import lombok.val;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * This is {@link RestPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RestPasswordManagementConfiguration.class,
    PasswordManagementConfiguration.class,
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.pm.rest.endpointUrlChange=http://localhost:9090/change",
    "cas.authn.pm.rest.endpointUrlSecurityQuestions=http://localhost:9090/questions",
    "cas.authn.pm.rest.endpointUrlEmail=http://localhost:9090/email"
    })
@Category(RestfulApiCategory.class)
public class RestPasswordManagementServiceTests extends AbstractPasswordManagementTests {

    @Autowired
    @Qualifier("passwordManagementRestTemplate")
    private RestTemplate restTemplate;

    @Test
    @Override
    public void verifyUserEmailCanBeFound() {
        val server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://localhost:9090/email"))
            .andExpect(method(HttpMethod.GET)).andRespond(withSuccess(CASUSER+"@example.org", MediaType.TEXT_PLAIN));

        assertNotNull(server);
        super.verifyUserEmailCanBeFound();
        server.verify();
    }

    @Test
    @Override
    public void verifyUserEmailCanNotBeFound() {
        val server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://localhost:9090/email")).andExpect(content().string(CASUSER+"notfound"))
            .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertNotNull(server);
        super.verifyUserEmailCanBeFound();
        server.verify();
    }

    @Test
    public void verifyUserQuestionsCanBeFound() {
        val server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://localhost:9090/questions")).andExpect(content().string(CASUSER))
            .andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertNotNull(server);
        super.verifyUserEmailCanBeFound();
        server.verify();
    }

    @Test
    public void verifyUserPasswordChange() {
        val bean = new PasswordChangeBean();
        bean.setConfirmedPassword("newPassword");
        bean.setPassword("newPassword");
        assertTrue("Password did not change", passwordChangeService.change(new UsernamePasswordCredential(CASUSER, "password"), bean));
    }

    @Test
    public void verifyPasswordValidationService() {
        val bean = new PasswordChangeBean();
        bean.setConfirmedPassword("Test@1234");
        bean.setPassword("Test@1234");
        assertTrue("Password should be valid", passwordValidationService.isValid(new UsernamePasswordCredential(CASUSER, "password"), bean));
        assertFalse("Password should not be valid", passwordValidationService.isValid(new UsernamePasswordCredential(CASUSER, "Test@1234"), bean));
    }
}
