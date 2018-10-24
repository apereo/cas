package org.apereo.cas.gauth.credential;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * This is {@link RestGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.authn.mfa.gauth.rest.endpointUrl=http://example.com"
})
@Category(RestfulApiCategory.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Getter
public class RestGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final Map<String, OneTimeTokenCredentialRepository> repositoryMap = new HashMap<>();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @Override
    public OneTimeTokenCredentialRepository getRegistry(final String testName) {
        return repositoryMap.computeIfAbsent(testName,
            name -> new RestGoogleAuthenticatorTokenCredentialRepository(getGoogle(), new RestTemplate(),
                casProperties.getAuthn().getMfa().getGauth(),
                CipherExecutor.noOpOfStringToString()));
    }

    @Test
    @Override
    public void verifyGet() throws Exception {
        val repository = (RestGoogleAuthenticatorTokenCredentialRepository) getRegistry("verifyGet");
        assertNotNull("Repository is null", repository);

        val mockServer = MockRestServiceServer.createServer(repository.getRestTemplate());
        mockServer.expect(requestTo("http://example.com"))
            .andExpect(method(HttpMethod.GET)).andRespond(withNoContent());
        mockServer.expect(requestTo("http://example.com"))
            .andExpect(method(HttpMethod.POST)).andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("http://example.com"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(MAPPER.writeValueAsString(getAccount("verifyGet", CASUSER)), MediaType.APPLICATION_JSON));

        super.verifyGet();
        mockServer.verify();
    }

    @Test
    @Override
    public void verifyGetWithDecodedSecret() throws Exception {
        val repository = (RestGoogleAuthenticatorTokenCredentialRepository) getRegistry("verifyGetWithDecodedSecret");
        assertNotNull("Repository is null", repository);

        val acct = getAccount("verifyGetWithDecodedSecret", CASUSER).clone();
        acct.setSecretKey(PLAIN_SECRET);
        val mockServer = MockRestServiceServer.createServer(repository.getRestTemplate());
        mockServer.expect(requestTo("http://example.com"))
            .andExpect(method(HttpMethod.POST)).andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("http://example.com"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(MAPPER.writeValueAsString(acct), MediaType.APPLICATION_JSON));

        super.verifyGetWithDecodedSecret();
        mockServer.verify();
    }

    @Test
    @Override
    public void verifySaveAndUpdate() throws Exception {
        val repository = (RestGoogleAuthenticatorTokenCredentialRepository) getRegistry("verifySaveAndUpdate");
        assertNotNull("Repository is null", repository);
        val acct = getAccount("verifySaveAndUpdate", CASUSER).clone();

        val mockServer = MockRestServiceServer.createServer(repository.getRestTemplate());
        mockServer.expect(requestTo("http://example.com"))
            .andExpect(method(HttpMethod.POST)).andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("http://example.com"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(MAPPER.writeValueAsString(acct), MediaType.APPLICATION_JSON));

        acct.setSecretKey("newSecret");
        acct.setValidationCode(999666);

        mockServer.expect(requestTo("http://example.com"))
            .andExpect(method(HttpMethod.POST)).andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("http://example.com"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(MAPPER.writeValueAsString(acct), MediaType.APPLICATION_JSON));

        super.verifySaveAndUpdate();
        mockServer.verify();
    }
}
