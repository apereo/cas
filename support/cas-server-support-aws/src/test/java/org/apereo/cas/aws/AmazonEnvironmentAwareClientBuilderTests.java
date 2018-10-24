package org.apereo.cas.aws;

import com.amazonaws.client.builder.AwsClientBuilder;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.env.Environment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonEnvironmentAwareClientBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class AmazonEnvironmentAwareClientBuilderTests {
    static {
        System.setProperty("aws.accessKeyId", "AKIAIPPIGGUNIO74C63Z");
        System.setProperty("aws.secretKey", "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");
    }

    @Autowired
    private Environment environment;

    @Test
    public void verifyAction() {
        val builder = new AmazonEnvironmentAwareClientBuilder("aws", environment);
        val mock = mock(AwsClientBuilder.class);
        when(mock.build()).thenReturn(new Object());
        val client = builder.build(mock, Object.class);
        assertNotNull(client);
        assertNotNull(builder.getSetting("secretKey"));
        assertNotNull(builder.getSetting("secretKey", String.class));
        assertNotNull(builder.getSetting("something", "defaultValue"));
    }
}
