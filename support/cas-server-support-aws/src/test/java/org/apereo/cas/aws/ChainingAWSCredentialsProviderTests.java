package org.apereo.cas.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link ChainingAWSCredentialsProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ChainingAWSCredentialsProviderTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

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
