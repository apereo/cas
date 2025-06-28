package org.apereo.cas.aws;

import java.nio.file.Files;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.core.SdkSystemSetting;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingAWSCredentialsProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AmazonWebServices")
class ChainingAWSCredentialsProviderTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Test
    void verifyInstance() throws Throwable {
        val path = Files.createTempFile("props", ".txt").toFile().getCanonicalPath();
        val p = (AwsCredentialsProviderChain) ChainingAWSCredentialsProvider.getInstance("accesskey", "secretKey",
            "profilePath", path);
        val credentials = p.resolveCredentials();
        assertNotNull(credentials);
        assertInstanceOf(AwsBasicCredentials.class, credentials);
        assertNotNull(ChainingAWSCredentialsProvider.getInstance());
    }
}
