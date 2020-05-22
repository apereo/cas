package org.apereo.cas.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

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
public class ChainingAWSCredentialsProvider implements AWSCredentialsProvider {
    private final List<AWSCredentialsProvider> chain;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static AWSCredentialsProvider getInstance() {
        return getInstance(null, null, null, null, null);
    }

    /**
     * Gets instance.
     *
     * @param credentialAccessKey the credential access key
     * @param credentialSecretKey the credential secret key
     * @return the instance
     */
    public static AWSCredentialsProvider getInstance(final String credentialAccessKey, final String credentialSecretKey) {
        return getInstance(credentialAccessKey, credentialSecretKey, null, null, null);
    }

    /**
     * Gets instance.
     *
     * @param credentialAccessKey      the credential access key
     * @param credentialSecretKey      the credential secret key
     * @param credentialPropertiesFile the credential properties file
     * @return the instance
     */
    public static AWSCredentialsProvider getInstance(final String credentialAccessKey, final String credentialSecretKey,
                                                     final Resource credentialPropertiesFile) {
        return getInstance(credentialAccessKey, credentialSecretKey, credentialPropertiesFile, null, null);
    }

    /**
     * Gets instance.
     *
     * @param credentialAccessKey      the credential access key
     * @param credentialSecretKey      the credential secret key
     * @param credentialPropertiesFile the credential properties file
     * @param profilePath              the profile path
     * @param profileName              the profile name
     * @return the instance
     */
    public static AWSCredentialsProvider getInstance(final String credentialAccessKey, final String credentialSecretKey,
                                                     final Resource credentialPropertiesFile,
                                                     final String profilePath, final String profileName) {

        LOGGER.debug("Attempting to locate AWS credentials...");

        val chain = new ArrayList<AWSCredentialsProvider>();
        chain.add(new InstanceProfileCredentialsProvider(false));

        if (credentialPropertiesFile != null) {
            try {
                val f = credentialPropertiesFile.getFile();
                chain.add(new PropertiesFileCredentialsProvider(f.getCanonicalPath()));
            } catch (final Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error(e.getMessage(), e);
                } else {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        if (StringUtils.isNotBlank(profilePath) && StringUtils.isNotBlank(profileName)) {
            addProviderToChain(nothing -> {
                chain.add(new ProfileCredentialsProvider(profilePath, profileName));
                return null;
            });
        }
        addProviderToChain(nothing -> {
            chain.add(new SystemPropertiesCredentialsProvider());
            return null;
        });

        addProviderToChain(nothing -> {
            chain.add(new EnvironmentVariableCredentialsProvider());
            return null;
        });

        addProviderToChain(nothing -> {
            chain.add(new EnvironmentVariableCredentialsProvider());
            return null;
        });

        addProviderToChain(nothing -> {
            chain.add(new ClasspathPropertiesFileCredentialsProvider("awscredentials.properties"));
            return null;
        });

        if (StringUtils.isNotBlank(credentialAccessKey) && StringUtils.isNotBlank(credentialSecretKey)) {
            addProviderToChain(nothing -> {
                val credentials = new BasicAWSCredentials(credentialAccessKey, credentialSecretKey);
                chain.add(new AWSStaticCredentialsProvider(credentials));
                return null;
            });
        }

        addProviderToChain(nothing -> {
            chain.add(new EC2ContainerCredentialsProviderWrapper());
            return null;
        });

        LOGGER.debug("AWS chained credential providers are configured as [{}]", chain);
        return new ChainingAWSCredentialsProvider(chain);
    }

    private static void addProviderToChain(final Function<Void, Void> func) {
        try {
            func.apply(null);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }

    private static AWSCredentials getCredentialsFromProvider(final AWSCredentialsProvider p) {
        try {
            LOGGER.debug("Calling credential provider [{}] to fetch credentials...", p.getClass().getSimpleName());
            return p.getCredentials();
        } catch (final Throwable e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public AWSCredentials getCredentials() {
        LOGGER.debug("Attempting to locate AWS credentials from the chain...");
        for (val p : this.chain) {
            val c = getCredentialsFromProvider(p);
            if (c != null) {
                LOGGER.debug("Fetched credentials from [{}] provider successfully.", p.getClass().getSimpleName());
                return c;
            }
        }
        LOGGER.warn("No AWS credentials could be determined from the chain. Using anonymous credentials...");
        return new AnonymousAWSCredentials();
    }

    @Override
    public void refresh() {
        for (val p : this.chain) {
            try {
                p.refresh();
            } catch (final Throwable e) {
                LOGGER.trace(e.getMessage(), e);
            }
        }
    }


}
