package org.apereo.cas.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingAWSCredentialsProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ChainingAWSCredentialsProviderTests {
    static {
        System.setProperty("aws.accessKeyId", "AKIAIPPIGGUNIO74C63Z");
        System.setProperty("aws.secretKey", "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");
    }

    @Test
    public void verifyInstance() {
        val p = (ChainingAWSCredentialsProvider) ChainingAWSCredentialsProvider.getInstance("accesskey", "secretKey",
            new FileSystemResource("credentials.properties"), "profilePath", "profileName");
        assertFalse(p.getChain().isEmpty());
        val credentials = p.getCredentials();
        assertNotNull(credentials);
        assertTrue(credentials instanceof BasicAWSCredentials);
    }
}
