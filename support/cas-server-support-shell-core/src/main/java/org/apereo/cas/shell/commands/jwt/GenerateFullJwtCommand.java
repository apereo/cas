package org.apereo.cas.shell.commands.jwt;

import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This is {@link GenerateFullJwtCommand}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@ShellCommandGroup("JWT")
@ShellComponent
@Slf4j
public class GenerateFullJwtCommand {
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
    @ShellMethod(key = "generate-full-jwt", value = "Generate JWT and sign it using a given keystore")
    public String generateKey(
        @ShellOption(value = {"jwks", "--jwks"}, defaultValue = StringUtils.EMPTY, help = "Path to the JWKS file used to sign the token")
        final String jwks,
        @ShellOption(value = {"iss", "--iss"}, defaultValue = "https://localhost:8443/cas/oidc", help = "Issuer")
        final String iss,
        @ShellOption(value = {"claims", "--claims"}, defaultValue = "{}", help = "JWT claims as JSON")
        final String claims,
        @ShellOption(value = {"aud", "--aud"}, defaultValue = "CAS", help = "Audience")
        final String aud,
        @ShellOption(value = {"exp", "--exp"}, defaultValue = "300", help = "Expiration in seconds")
        final String exp,
        @ShellOption(value = {"sub", "--sub"}, help = "Subject")
        final String sub) throws Exception {

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
