package org.apereo.cas.shell.commands.jwt;

import module java.base;
import org.apereo.cas.shell.commands.CasShellCommand;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.impl.AESCryptoProvider;
import com.nimbusds.jose.crypto.impl.DirectCryptoProvider;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

/**
 * This is {@link GenerateJwtCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GenerateJwtCommand implements CasShellCommand {

    private static final int SEP_LENGTH = 8;

    private static final int DEFAULT_SIGNING_SECRET_SIZE = 256;

    private static final int DEFAULT_ENCRYPTION_SECRET_SIZE = 48;

    private static final String DEFAULT_SIGNING_ALGORITHM = "HS256";

    private static final String DEFAULT_ENCRYPTION_ALGORITHM = "dir";

    private static final String DEFAULT_ENCRYPTION_METHOD = "A192CBC-HS384";

    /**
     * Generate.
     *
     * @param signingSecretSize    the signing secret size
     * @param encryptionSecretSize the encryption secret size
     * @param signingAlgorithm     the signing algorithm
     * @param encryptionAlgorithm  the encryption algorithm
     * @param encryptionMethod     the encryption algorithm
     * @param subject              the subject, which is required
     * @throws IllegalArgumentException when no subject is given
     */
    @Command(group = "JWT", name = "generate-jwt", description = "Generate a JWT with given size and algorithm for signing and encryption.")
    public void generate(
        @Option(
            longName = "signingSecretSize",
            description = "Size of the signing secret",
            defaultValue = StringUtils.EMPTY + DEFAULT_SIGNING_SECRET_SIZE
        )
        final int signingSecretSize,

        @Option(
            longName = "encryptionSecretSize",
            description = "Size of the encryption secret",
            defaultValue = StringUtils.EMPTY + DEFAULT_ENCRYPTION_SECRET_SIZE
        )
        final int encryptionSecretSize,

        @Option(
            longName = "signingAlgorithm",
            description = "Algorithm to use for signing",
            defaultValue = DEFAULT_SIGNING_ALGORITHM
        )
        final String signingAlgorithm,

        @Option(
            longName = "encryptionAlgorithm",
            description = "Algorithm to use for encryption",
            defaultValue = DEFAULT_ENCRYPTION_ALGORITHM
        )
        final String encryptionAlgorithm,

        @Option(
            longName = "encryptionMethod",
            description = "Method to use for encryption",
            defaultValue = DEFAULT_ENCRYPTION_METHOD
        )
        final String encryptionMethod,

        @Option(
            longName = "subject",
            description = "Subject to use for the JWT",
            required = true
        )
        final String subject
    ) {
        if (StringUtils.isBlank(subject)) {
            throw new IllegalArgumentException("A subject is required to generate a JWT");
        }
        val repeat = "=".repeat(SEP_LENGTH);
        LOGGER.debug(repeat);
        LOGGER.info("""
                Generating JWT for subject [{}] with signing key size [{}], signing algorithm [{}],
                encryption key size [{}], encryption method [{}] and encryption algorithm [{}]
                """, subject, signingSecretSize, signingAlgorithm,
            encryptionSecretSize, encryptionMethod, encryptionAlgorithm);
        LOGGER.debug(repeat);

        val claims = new JWTClaimsSet.Builder().subject(subject).issueTime(new Date()).build();
        val jwt = signJwt(signingSecretSize, signingAlgorithm, claims);
        val token = encryptJwt(encryptionSecretSize, encryptionAlgorithm, encryptionMethod, jwt).orElseGet(jwt::serialize);
        LOGGER.info("==== JWT ====\n[{}]", token);
    }

    /**
     * Encrypt the token with a random secret of the given size: a signed token travels as the payload of a JWE whose
     * content type is {@code JWT}, an unsigned one as an encrypted JWT.
     *
     * @param encryptionSecretSize the encryption secret size
     * @param encryptionAlgorithm  the encryption algorithm
     * @param encryptionMethod     the encryption method
     * @param jwt                  the token
     * @return the encrypted token, or empty when the token stays unencrypted
     */
    private static Optional<String> encryptJwt(final int encryptionSecretSize, final String encryptionAlgorithm,
                                               final String encryptionMethod, final JWT jwt) {
        if (encryptionSecretSize <= 0 || StringUtils.isBlank(encryptionMethod) || StringUtils.isBlank(encryptionAlgorithm)) {
            LOGGER.info("No encryption algorithm or size specified, so the generated JWT will not be encrypted");
            return Optional.empty();
        }

        val encryptionSecret = RandomUtils.randomAlphanumeric(encryptionSecretSize);
        LOGGER.info("==== Encryption Secret ====\n[{}]\n", encryptionSecret);

        val acceptedEncAlgs = Arrays.stream(JWEAlgorithm.class.getDeclaredFields())
            .filter(f -> f.getType().equals(JWEAlgorithm.class))
            .map(Unchecked.function(f -> {
                f.setAccessible(true);
                return ((Algorithm) f.get(null)).getName();
            }))
            .collect(Collectors.joining(","));
        LOGGER.debug("Encryption algorithm: [{}]. Available algorithms are [{}]", encryptionAlgorithm, acceptedEncAlgs);

        val acceptedEncMethods = Arrays.stream(EncryptionMethod.class.getDeclaredFields())
            .filter(f -> f.getType().equals(EncryptionMethod.class))
            .map(Unchecked.function(f -> {
                f.setAccessible(true);
                return ((Algorithm) f.get(null)).getName();
            }))
            .collect(Collectors.joining(","));
        LOGGER.debug("Encryption method: [{}]. Available methods are [{}]", encryptionMethod, acceptedEncMethods);

        val algorithm = JWEAlgorithm.parse(encryptionAlgorithm);
        val encryptionMethodAlg = EncryptionMethod.parse(encryptionMethod);

        if (DirectCryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)
            && !DirectCryptoProvider.SUPPORTED_ENCRYPTION_METHODS.contains(encryptionMethodAlg)) {
            LOGGER.warn("Encrypted method [{}] is not supported for algorithm [{}]. Accepted methods are [{}]",
                encryptionMethod, encryptionAlgorithm, DirectCryptoProvider.SUPPORTED_ENCRYPTION_METHODS);
            return Optional.empty();
        }
        if (AESCryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)
            && !AESCryptoProvider.SUPPORTED_ENCRYPTION_METHODS.contains(encryptionMethodAlg)) {
            LOGGER.warn("Encrypted method [{}] is not supported for algorithm [{}]. Accepted methods are [{}]",
                encryptionMethod, encryptionAlgorithm, AESCryptoProvider.SUPPORTED_ENCRYPTION_METHODS);
            return Optional.empty();
        }
        return Optional.of(FunctionUtils.doUnchecked(() -> {
            val secret = encryptionSecret.getBytes(StandardCharsets.UTF_8);
            val encrypter = DirectCryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm) ? new DirectEncrypter(secret) : new AESEncrypter(secret);
            if (jwt instanceof final SignedJWT signedJwt) {
                val jwe = new JWEObject(new JWEHeader.Builder(algorithm, encryptionMethodAlg).contentType("JWT").build(), new Payload(signedJwt));
                jwe.encrypt(encrypter);
                return jwe.serialize();
            }
            val encryptedJwt = new EncryptedJWT(new JWEHeader(algorithm, encryptionMethodAlg), jwt.getJWTClaimsSet());
            encryptedJwt.encrypt(encrypter);
            return encryptedJwt.serialize();
        }));
    }

    /**
     * Sign the claims with a random secret of the given size.
     *
     * @param signingSecretSize the signing secret size
     * @param signingAlgorithm  the signing algorithm
     * @param claims            the claims
     * @return the signed token, or an unsigned one when no signing algorithm or size is given
     */
    private static JWT signJwt(final int signingSecretSize, final String signingAlgorithm, final JWTClaimsSet claims) {
        if (signingSecretSize <= 0 || StringUtils.isBlank(signingAlgorithm)) {
            LOGGER.info("No signing algorithm or size specified, so the generated JWT will not be signed");
            return new PlainJWT(claims);
        }

        val signingSecret = RandomUtils.randomAlphanumeric(signingSecretSize);
        LOGGER.info("==== Signing Secret ====\n[{}]\n", signingSecret);

        val acceptedSigningAlgs = Arrays.stream(JWSAlgorithm.class.getDeclaredFields())
            .filter(f -> f.getType().equals(JWSAlgorithm.class))
            .map(Unchecked.function(f -> {
                f.setAccessible(true);
                return ((Algorithm) f.get(null)).getName();
            }))
            .collect(Collectors.joining(","));
        LOGGER.debug("Signing algorithm: [{}]. Available algorithms are [{}]", signingAlgorithm, acceptedSigningAlgs);

        return FunctionUtils.doUnchecked(() -> {
            val signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.parse(signingAlgorithm)), claims);
            signedJwt.sign(new MACSigner(signingSecret.getBytes(StandardCharsets.UTF_8)));
            return signedJwt;
        });
    }
}
