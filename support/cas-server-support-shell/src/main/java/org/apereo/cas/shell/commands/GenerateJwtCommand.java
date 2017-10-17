package org.apereo.cas.shell.commands;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.DirectDecrypter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This is {@link GenerateJwtCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Service
public class GenerateJwtCommand implements CommandMarker {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateJwtCommand.class);
    private static final int DEFAULT_SIGNING_SECRET_SIZE = 256;
    private static final int DEFAULT_ENCRYPTION_SECRET_SIZE = 48;
    private static final String DEFAULT_SIGNING_ALGORITHM = "HS256";
    private static final String DEFAULT_ENCRYPTION_ALGORITHM = "dir";
    private static final String DEFAULT_ENCRYPTION_METHOD = "A192CBC-HS384";


    /**
     * Generate.
     *
     * @param subject the subject
     */
    public void generate(final String subject) {
        generate(DEFAULT_SIGNING_SECRET_SIZE, DEFAULT_ENCRYPTION_SECRET_SIZE,
                DEFAULT_SIGNING_ALGORITHM, DEFAULT_ENCRYPTION_ALGORITHM,
                DEFAULT_ENCRYPTION_METHOD, subject);
    }

    /**
     * Generate.
     *
     * @param signingSecretSize    the signing secret size
     * @param encryptionSecretSize the encryption secret size
     * @param signingAlgorithm     the signing algorithm
     * @param encryptionAlgorithm  the encryption algorithm
     * @param encryptionMethod     the encryption algorithm
     * @param subject              the subject
     */
    @CliCommand(value = "generate-jwt", help = "Generate a JWT with given size and algorithm for signing and encryption.")
    public void generate(
            @CliOption(key = {"signingSecretSize"},
                    help = "Size of the signing secret",
                    optionContext = "Size of the signing secret",
                    specifiedDefaultValue = "256",
                    unspecifiedDefaultValue = "256") final int signingSecretSize,
            @CliOption(key = {"signingSecretSize"},
                    help = "Size of the encryption secret",
                    optionContext = "Size of the encryption secret",
                    specifiedDefaultValue = "48",
                    unspecifiedDefaultValue = "48") final int encryptionSecretSize,
            @CliOption(key = {"signingAlgorithm"},
                    help = "Algorithm to use for signing",
                    optionContext = "Algorithm to use for signing",
                    specifiedDefaultValue = "HS256",
                    unspecifiedDefaultValue = "HS256") final String signingAlgorithm,
            @CliOption(key = {"encryptionAlgorithm"},
                    help = "Algorithm to use for encryption",
                    optionContext = "Algorithm to use for encryption",
                    specifiedDefaultValue = "DIR",
                    unspecifiedDefaultValue = "DIR") final String encryptionAlgorithm,
            @CliOption(key = {"encryptionMethod"},
                    help = "Method to use for encryption",
                    optionContext = "Method to use for encryption",
                    specifiedDefaultValue = "A256GCM",
                    unspecifiedDefaultValue = "A256GCM") final String encryptionMethod,
            @CliOption(key = {"subject"},
                    help = "Subject to use for the JWT",
                    optionContext = "Subject to use for the JWT",
                    mandatory = true) final String subject) {

        final JwtGenerator<CommonProfile> g = new JwtGenerator<>();

        configureJwtSigning(signingSecretSize, signingAlgorithm, g);
        configureJwtEncryption(encryptionSecretSize, encryptionAlgorithm, encryptionMethod, g);

        final CommonProfile profile = new CommonProfile();
        profile.setId(subject);
        final String token = g.generate(profile);
        LOGGER.info("JWT:\n{}", token);
    }

    private void configureJwtEncryption(final int encryptionSecretSize, final String encryptionAlgorithm,
                                        final String encryptionMethod, final JwtGenerator<CommonProfile> g) {
        if (encryptionSecretSize > 0 || StringUtils.isBlank(encryptionMethod) || StringUtils.isBlank(encryptionAlgorithm)) {
            LOGGER.info("No encryption algorithm specified, so the generated JWT will not be encrypted");
            return;
        }

        final String encryptionSecret = RandomStringUtils.randomAlphanumeric(encryptionSecretSize);
        LOGGER.info("Encryption Secret:\n[{}]", encryptionSecret);

        final String acceptedEncAlgs = Arrays.stream(JWEAlgorithm.class.getDeclaredFields())
                .filter(f -> f.getType().equals(JWEAlgorithm.class))
                .map(Unchecked.function(f -> {
                    f.setAccessible(true);
                    return ((JWEAlgorithm) f.get(null)).getName();
                }))
                .collect(Collectors.joining(","));
        LOGGER.debug("Encryption algorithm: [{}]. Available algorithms are [{}]", encryptionAlgorithm, acceptedEncAlgs);

        final String acceptedEncMethods = Arrays.stream(EncryptionMethod.class.getDeclaredFields())
                .filter(f -> f.getType().equals(EncryptionMethod.class))
                .map(Unchecked.function(f -> {
                    f.setAccessible(true);
                    return ((EncryptionMethod) f.get(null)).getName();
                }))
                .collect(Collectors.joining(","));
        LOGGER.debug("Encryption method: [{}]. Available methods are [{}]", encryptionMethod, acceptedEncMethods);

        final JWEAlgorithm algorithm = JWEAlgorithm.parse(encryptionAlgorithm);
        if (DirectDecrypter.SUPPORTED_ALGORITHMS.contains(algorithm.getName())) {
            if (!DirectDecrypter.SUPPORTED_ENCRYPTION_METHODS.contains(encryptionMethod)) {
                LOGGER.warn("Encrypted method [{}] is not supported for algorithm [{}]. Accepted methods are [{}]",
                        encryptionMethod, encryptionAlgorithm, DirectDecrypter.SUPPORTED_ENCRYPTION_METHODS);
                return;
            }
        }
        if (AESDecrypter.SUPPORTED_ALGORITHMS.contains(algorithm)) {
            if (!AESDecrypter.SUPPORTED_ENCRYPTION_METHODS.contains(encryptionMethod)) {
                LOGGER.warn("Encrypted method [{}] is not supported for algorithm [{}]. Accepted methods are [{}]",
                        encryptionMethod, encryptionAlgorithm, AESDecrypter.SUPPORTED_ENCRYPTION_METHODS);
                return;
            }
        }

        final EncryptionMethod encMethod = EncryptionMethod.parse(encryptionMethod);
        g.setEncryptionConfiguration(new SecretEncryptionConfiguration(encryptionSecret, algorithm, encMethod));
        
    }

    private void configureJwtSigning(final int signingSecretSize, final String signingAlgorithm, final JwtGenerator<CommonProfile> g) {
        final String signingSecret = RandomStringUtils.randomAlphanumeric(signingSecretSize);
        LOGGER.info("Signing Secret:\n[{}]", signingSecret);

        final String acceptedSigningAlgs = Arrays.stream(JWSAlgorithm.class.getDeclaredFields())
                .filter(f -> f.getType().equals(JWSAlgorithm.class))
                .map(Unchecked.function(f -> {
                    f.setAccessible(true);
                    return ((JWSAlgorithm) f.get(null)).getName();
                }))
                .collect(Collectors.joining(","));
        LOGGER.debug("Signing algorithm: [{}]. Available algorithms are [{}]", signingAlgorithm, acceptedSigningAlgs);

        g.setSignatureConfiguration(new SecretSignatureConfiguration(signingSecret, JWSAlgorithm.parse(signingAlgorithm)));
    }
}
