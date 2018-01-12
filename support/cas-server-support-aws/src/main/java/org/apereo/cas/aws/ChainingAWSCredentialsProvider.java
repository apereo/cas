package org.apereo.cas.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link ChainingAWSCredentialsProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ChainingAWSCredentialsProvider implements AWSCredentialsProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChainingAWSCredentialsProvider.class);

    private List<AWSCredentialsProvider> chain = new ArrayList<>();

    public ChainingAWSCredentialsProvider(final AWSCredentialsProvider... chain) {
        this.chain = Stream.of(chain).collect(Collectors.toList());
    }

    public ChainingAWSCredentialsProvider(final List<AWSCredentialsProvider> chain) {
        this.chain = chain;
    }

    /**
     * Add provider.
     *
     * @param p the provider
     */
    public void addProvider(final AWSCredentialsProvider p) {
        this.chain.add(p);
    }

    @Override
    public AWSCredentials getCredentials() {
        for (final AWSCredentialsProvider p : this.chain) {
            AWSCredentials c;
            try {
                c = p.getCredentials();
            } catch (final Throwable e) {
                LOGGER.trace(e.getMessage(), e);
                c = null;
            }
            if (c != null) {
                return c;
            }
        }
        throw new IllegalArgumentException("No AWS credentials could be determined from the chain: " + this.chain);
    }

    @Override
    public void refresh() {
        for (final AWSCredentialsProvider p : this.chain) {
            try {
                p.refresh();
            } catch (final Throwable e) {
                LOGGER.trace(e.getMessage(), e);
            }
        }
    }

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
        final List<AWSCredentialsProvider> chain = new ArrayList<>();
        chain.add(new InstanceProfileCredentialsProvider(false));

        if (credentialPropertiesFile != null) {
            try {
                final File f = credentialPropertiesFile.getFile();
                chain.add(new PropertiesFileCredentialsProvider(f.getCanonicalPath()));
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (StringUtils.isNotBlank(profilePath) && StringUtils.isNotBlank(profileName)) {
            chain.add(new ProfileCredentialsProvider(profilePath, profileName));
        }
        chain.add(new SystemPropertiesCredentialsProvider());
        chain.add(new EnvironmentVariableCredentialsProvider());
        chain.add(new ClasspathPropertiesFileCredentialsProvider("awscredentials.properties"));
        if (StringUtils.isNotBlank(credentialAccessKey) && StringUtils.isNotBlank(credentialSecretKey)) {
            final BasicAWSCredentials credentials = new BasicAWSCredentials(credentialAccessKey, credentialSecretKey);
            chain.add(new AWSStaticCredentialsProvider(credentials));
        }
        return new ChainingAWSCredentialsProvider(chain);
    }
}
