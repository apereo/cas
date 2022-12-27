package org.apereo.cas.config;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.protobuf.ByteString;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Properties;

/**
 * This is {@link GoogleCloudSecretsManagerCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@Getter
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "gcp-secretsmanager")
@AutoConfiguration
public class GoogleCloudSecretsManagerCloudConfigBootstrapConfiguration implements PropertySourceLocator {
    public static final String CAS_CONFIGURATION_PREFIX = "";

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val props = new Properties();
        try {
           props.put(1, 1);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        LOGGER.debug("Located [{}] secret(s)", props.size());
        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }

    public static void main(String[] args) throws Exception {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            // Build the parent name from the project.
            ProjectName projectName = ProjectName.of("misagh-362104");

            // Create the parent secret.
//            Secret secret =
//                Secret.newBuilder()
//                    .setReplication(
//                        Replication.newBuilder()
//                            .setAutomatic(Replication.Automatic.newBuilder().build())
//                            .build())
//                    .build();
//
//            Secret createdSecret = client.createSecret(projectName, secretId, secret);
//
//            // Add a secret version.
//            SecretPayload payload =
//                SecretPayload.newBuilder().setData(ByteString.copyFromUtf8("hello world!")).build();
//            SecretVersion addedVersion = client.addSecretVersion(createdSecret.getName(), payload);

            // Access the secret version.
//            AccessSecretVersionResponse response = client.accessSecretVersion(addedVersion.getName());
            client.listSecrets(projectName).iterateAll().forEach(secret -> {
                System.out.println(secret.toByteString().toString());
            });

            // Print the secret payload.
            //
            // WARNING: Do not print the secret in a production environment - this
            // snippet is showing how to access the secret material.
//            String data = response.getPayload().getData().toStringUtf8();
//            System.out.printf("Plaintext: %s\n", data);
        }
    }
}
