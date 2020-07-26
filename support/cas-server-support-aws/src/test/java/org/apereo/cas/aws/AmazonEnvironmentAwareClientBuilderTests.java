package org.apereo.cas.aws;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.core.SdkSystemSetting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonEnvironmentAwareClientBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Tag("AmazonWebServices")
public class AmazonEnvironmentAwareClientBuilderTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
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
        assertNotNull(builder.getSetting("secretAccessKey"));
        assertNotNull(builder.getSetting("secretAccessKey", String.class));
        assertNotNull(builder.getSetting("something", "defaultValue"));
    }
}
