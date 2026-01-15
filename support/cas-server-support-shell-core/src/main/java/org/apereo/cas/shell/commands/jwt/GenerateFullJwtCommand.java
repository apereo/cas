package org.apereo.cas.shell.commands.jwt;

import module java.base;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.shell.commands.CasShellCommand;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import org.hjson.JsonValue;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link GenerateFullJwtCommand}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class GenerateFullJwtCommand implements CasShellCommand {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    /**
     * Generate key string.
     *
     * @param jwks   the jwks
     * @param iss    the iss
     * @param claims the claims
     * @param aud    the aud
     * @param exp    the exp
     * @param sub    the sub
     * @return the string
     * @throws Exception the exception
     */
    @Command(group = "JWT", name = "generate-full-jwt", description = "Generate JWT and sign it using a given keystore")
    public String generateKey(
        @Option(
            longName = "jwks",
            defaultValue = StringUtils.EMPTY,
            description = "Path to the JWKS file used to sign the token"
        )
        final String jwks,

        @Option(
            longName = "iss",
            defaultValue = "https://localhost:8443/cas/oidc",
            description = "Issuer"
        )
        final String iss,

        @Option(
            longName = "claims",
            defaultValue = "{}",
            description = "JWT claims as JSON"
        )
        final String claims,

        @Option(
            longName = "aud",
            defaultValue = "CAS",
            description = "Audience"
        )
        final String aud,

        @Option(
            longName = "exp",
            defaultValue = "300",
            description = "Expiration in seconds"
        )
        final String exp,

        @Option(
            longName = "sub",
            description = "Subject"
        )
        final String sub
    ) throws Exception {

        val jwtClaims = new JwtClaims();
        jwtClaims.setJwtId(RandomUtils.randomAlphanumeric(8));
        jwtClaims.setIssuer(iss);
        jwtClaims.setAudience(aud);

        if (NumberUtils.isParsable(exp)) {
            val expireInSeconds = Long.parseLong(exp);
            if (expireInSeconds > 0) {
                val expirationDate = NumericDate.now();
                expirationDate.addSeconds(expireInSeconds);
                jwtClaims.setExpirationTime(expirationDate);
            }
        } else {
            val expirationDate = NumericDate.fromMilliseconds(Beans.newDuration(exp).toSeconds());
            jwtClaims.setExpirationTime(expirationDate);
        }
        jwtClaims.setIssuedAtToNow();
        jwtClaims.setNotBeforeMinutesInThePast(1);
        jwtClaims.setSubject(sub);

        val otherClaims = MAPPER.readValue(JsonValue.readHjson(claims).toString(), Map.class);
        otherClaims.forEach((key, value) -> jwtClaims.setClaim(key.toString(), value));

        if (StringUtils.isNotBlank(jwks)) {
            val keystore = new JsonWebKeySet(FileUtils.readFileToString(new File(jwks), StandardCharsets.UTF_8));
            val jsonWebKey = (PublicJsonWebKey) keystore.getJsonWebKeys()
                .stream()
                .filter(key -> Strings.CI.equals(key.getUse(), "signing"))
                .findFirst()
                .orElseGet(() -> keystore.getJsonWebKeys().getFirst());
            val jwt = EncodingUtils.signJwsRSASha512(jsonWebKey.getPrivateKey(),
                jwtClaims.toJson().getBytes(StandardCharsets.UTF_8), Map.of());
            val token = new String(jwt, StandardCharsets.UTF_8);
            //CHECKSTYLE:OFF
            LOGGER.info("Producing signed JWT:\n{}\n", token);
            //CHECKSTYLE:ON
            return token;
        }
        val jwt = new PlainJWT(JWTClaimsSet.parse(jwtClaims.getClaimsMap()));
        val token = jwt.serialize();
        //CHECKSTYLE:OFF
        LOGGER.info("Producing plain JWT:\n{}\n", token);
        //CHECKSTYLE:ON
        return token;
    }
}
