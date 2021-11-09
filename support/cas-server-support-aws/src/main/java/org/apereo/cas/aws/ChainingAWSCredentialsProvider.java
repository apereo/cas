package org.apereo.cas.aws;

import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.profiles.ProfileFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This is {@link ChainingAWSCredentialsProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class ChainingAWSCredentialsProvider {
    private final List<AwsCredentialsProvider> chain;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static AwsCredentialsProvider getInstance() {
        return getInstance(null, null, null, null);
    }

    /**
     * Gets instance.
     *
     * @param credentialAccessKey the credential access key
     * @param credentialSecretKey the credential secret key
     * @return the instance
     */
    public static AwsCredentialsProvider getInstance(final String credentialAccessKey, final String credentialSecretKey) {
        return getInstance(credentialAccessKey, credentialSecretKey, null, null);
    }


    /**
     * Gets instance.
     *
     * @param credentialAccessKey the credential access key
     * @param credentialSecretKey the credential secret key
     * @param profilePath         the profile path
     * @param profileName         the profile name
     * @return the instance
     */
    public static AwsCredentialsProvider getInstance(final String credentialAccessKey, final String credentialSecretKey,
                                                     final String profilePath, final String profileName) {

        LOGGER.debug("Attempting to locate AWS credentials...");
        val chain = new ArrayList<AwsCredentialsProvider>();
        addProviderToChain(nothing -> {
            chain.add(WebIdentityTokenFileCredentialsProvider.create());
            return null;
        });

        chain.add(InstanceProfileCredentialsProvider.create());

        if (StringUtils.isNotBlank(profilePath) && StringUtils.isNotBlank(profileName)) {
            addProviderToChain(nothing -> {
                chain.add(ProfileCredentialsProvider.builder()
                    .profileName(profileName)
                    .profileFile(ProfileFile.builder().content(Path.of(profilePath)).build())
                    .build());
                return null;
            });
        }
        addProviderToChain(nothing -> {
            chain.add(SystemPropertyCredentialsProvider.create());
            return null;
        });

        addProviderToChain(nothing -> {
            chain.add(EnvironmentVariableCredentialsProvider.create());
            return null;
        });

        if (StringUtils.isNotBlank(credentialAccessKey) && StringUtils.isNotBlank(credentialSecretKey)) {
            addProviderToChain(nothing -> {
                val resolver = SpringExpressionLanguageValueResolver.getInstance();
                val credentials = AwsBasicCredentials.create(resolver.resolve(credentialAccessKey), resolver.resolve(credentialSecretKey));
                chain.add(StaticCredentialsProvider.create(credentials));
                return null;
            });
        }

        addProviderToChain(nothing -> {
            chain.add(ContainerCredentialsProvider.builder().build());
            return null;
        });

        addProviderToChain(nothing -> {
            chain.add(InstanceProfileCredentialsProvider.builder().build());
            return null;
        });

        LOGGER.debug("AWS chained credential providers are configured as [{}]", chain);
        return AwsCredentialsProviderChain.builder().credentialsProviders(chain).build();
    }

    private static void addProviderToChain(final Function<Void, Void> func) {
        try {
            func.apply(null);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }
}
