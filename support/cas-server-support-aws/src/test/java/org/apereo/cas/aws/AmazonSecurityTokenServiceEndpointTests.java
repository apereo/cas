package org.apereo.cas.aws;

import org.apereo.cas.config.AmazonCoreConfiguration;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonSecurityTokenServiceEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = {
    "cas.amazon-sts.endpoint=http://127.0.0.1:4566",
    "cas.amazon-sts.credential-access-key=test",
    "cas.amazon-sts.credential-secret-key=test",

    "management.endpoint.awsSts.enabled=true"
})
@Tag("AmazonWebServices")
@Import(AmazonCoreConfiguration.class)
public class AmazonSecurityTokenServiceEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("awsSecurityTokenServiceEndpoint")
    private AmazonSecurityTokenServiceEndpoint awsSecurityTokenServiceEndpoint;

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();

        val body = new LinkedMultiValueMap<String, String>();
        body.put("username", List.of("casuser"));
        body.put("password", List.of("Mellon"));

        val credentials = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", body, request);
        assertNotNull(credentials);
    }
}
