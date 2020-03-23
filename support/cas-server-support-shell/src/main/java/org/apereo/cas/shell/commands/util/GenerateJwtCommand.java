package org.apereo.cas.shell.commands.util;

import org.apereo.cas.util.RandomUtils;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.DirectDecrypter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This is {@link GenerateJwtCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ShellCommandGroup("Utilities")
@ShellComponent
@Slf4j
public class GenerateJwtCommand {

    private static final int SEP_LENGTH = 8;

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
    public static void generate(final String subject) {
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
    @ShellMethod(key = "generate-jwt", value = "Generate a JWT with given size and algorithm for signing and encryption.")
    public static void generate(
        @ShellOption(value = {"signingSecretSize", "--signingSecretSize"},
            help = "Size of the signing secret",
            defaultValue = StringUtils.EMPTY + DEFAULT_SIGNING_SECRET_SIZE) final int signingSecretSize,
        @ShellOption(value = {"encryptionSecretSize", "--encryptionSecretSize"},
            help = "Size of the encryption secret",
            defaultValue = StringUtils.EMPTY + DEFAULT_ENCRYPTION_SECRET_SIZE) final int encryptionSecretSize,
        @ShellOption(value = {"signingAlgorithm", "--signingAlgorithm"},
            help = "Algorithm to use for signing",
            defaultValue = DEFAULT_SIGNING_ALGORITHM) final String signingAlgorithm,
        @ShellOption(value = {"encryptionAlgorithm", "--encryptionAlgorithm"},
            help = "Algorithm to use for encryption",
            defaultValue = DEFAULT_ENCRYPTION_ALGORITHM) final String encryptionAlgorithm,
        @ShellOption(value = {"encryptionMethod", "--encryptionMethod"},
            help = "Method to use for encryption",
            defaultValue = DEFAULT_ENCRYPTION_METHOD) final String encryptionMethod,
        @ShellOption(value = {"subject", "--subject"},
            help = "Subject to use for the JWT") final String subject) {

        val g = new JwtGenerator<CommonProfile>();

        configureJwtSigning(signingSecretSize, signingAlgorithm, g);
        configureJwtEncryption(encryptionSecretSize, encryptionAlgorithm, encryptionMethod, g);

        val profile = new CommonProfile();
        profile.setId(subject);

        val repeat = "=".repeat(SEP_LENGTH);
        LOGGER.debug(repeat);
        LOGGER.info("\nGenerating JWT for subject [{}] with signing key size [{}], signing algorithm [{}], "
                + "encryption key size [{}], encryption method [{}] and encryption algorithm [{}]\n",
            subject, signingSecretSize, signingAlgorithm, encryptionSecretSize, encryptionMethod, encryptionAlgorithm);
        LOGGER.debug(repeat);

        val token = g.generate(profile);
        LOGGER.info("==== JWT ====\n[{}]", token);
    }

    private static void configureJwtEncryption(final int encryptionSecretSize, final String encryptionAlgorithm,
                                               final String encryptionMethod, final JwtGenerator<CommonProfile> g) {
        if (encryptionSecretSize <= 0 || StringUtils.isBlank(encryptionMethod) || StringUtils.isBlank(encryptionAlgorithm)) {
            LOGGER.info("No encryption algorithm or size specified, so the generated JWT will not be encrypted");
            return;
        }

        val encryptionSecret = RandomUtils.randomAlphanumeric(encryptionSecretSize);
        LOGGER.info("==== Encryption Secret ====\n[{}]\n", encryptionSecret);

        val acceptedEncAlgs = Arrays.stream(JWEAlgorithm.class.getDeclaredFields())
            .filter(f -> f.getType().equals(JWEAlgorithm.class))
            .map(Unchecked.function(f -> {
                f.setAccessible(true);
                return ((JWEAlgorithm) f.get(null)).getName();
            }))
            .collect(Collectors.joining(","));
        LOGGER.debug("Encryption algorithm: [{}]. Available algorithms are [{}]", encryptionAlgorithm, acceptedEncAlgs);

        val acceptedEncMethods = Arrays.stream(EncryptionMethod.class.getDeclaredFields())
            .filter(f -> f.getType().equals(EncryptionMethod.class))
            .map(Unchecked.function(f -> {
                f.setAccessible(true);
                return ((EncryptionMethod) f.get(null)).getName();
            }))
            .collect(Collectors.joining(","));
        LOGGER.debug("Encryption method: [{}]. Available methods are [{}]", encryptionMethod, acceptedEncMethods);

        val algorithm = JWEAlgorithm.parse(encryptionAlgorithm);
        val encryptionMethodAlg = EncryptionMethod.parse(encryptionMethod);

        if (DirectDecrypter.SUPPORTED_ALGORITHMS.contains(algorithm)) {
            if (!DirectDecrypter.SUPPORTED_ENCRYPTION_METHODS.contains(encryptionMethodAlg)) {
                LOGGER.warn("Encrypted method [{}] is not supported for algorithm [{}]. Accepted methods are [{}]",
                    encryptionMethod, encryptionAlgorithm, DirectDecrypter.SUPPORTED_ENCRYPTION_METHODS);
                return;
            }
        }
        if (AESDecrypter.SUPPORTED_ALGORITHMS.contains(algorithm)) {
            if (!AESDecrypter.SUPPORTED_ENCRYPTION_METHODS.contains(encryptionMethodAlg)) {
                LOGGER.warn("Encrypted method [{}] is not supported for algorithm [{}]. Accepted methods are [{}]",
                    encryptionMethod, encryptionAlgorithm, AESDecrypter.SUPPORTED_ENCRYPTION_METHODS);
                return;
            }
        }

        g.setEncryptionConfiguration(new SecretEncryptionConfiguration(encryptionSecret, algorithm, encryptionMethodAlg));

    }

    private static void configureJwtSigning(final int signingSecretSize, final String signingAlgorithm, final JwtGenerator<CommonProfile> g) {
        if (signingSecretSize <= 0 || StringUtils.isBlank(signingAlgorithm)) {
            LOGGER.info("No signing algorithm or size specified, so the generated JWT will not be encrypted");
            return;
        }

        val signingSecret = RandomUtils.randomAlphanumeric(signingSecretSize);
        LOGGER.info("==== Signing Secret ====\n{}\n", signingSecret);

        val acceptedSigningAlgs = Arrays.stream(JWSAlgorithm.class.getDeclaredFields())
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
