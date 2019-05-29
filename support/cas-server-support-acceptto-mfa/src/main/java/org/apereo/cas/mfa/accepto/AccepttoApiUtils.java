package org.apereo.cas.mfa.accepto;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.model.support.mfa.AccepttoMultifactorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.apereo.cas.web.support.CookieUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.AesKey;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is {@link AccepttoApiUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@UtilityClass
@Slf4j
public class AccepttoApiUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    /**
     * Gets user email attribute.
     *
     * @param authentication the authentication
     * @param acceptto       the acceptto
     * @return the user email attribute
     */
    public String getUserEmailAttribute(final Authentication authentication, final AccepttoMultifactorProperties acceptto) {
        val attributes = authentication.getPrincipal().getAttributes();
        LOGGER.debug("Current principal attributes are [{}]", attributes);

        return CollectionUtils.firstElement(attributes.get(acceptto.getEmailAttribute()))
            .map(Object::toString)
            .orElseThrow(null);
    }

    /**
     * Is user valid?.
     *
     * @param authentication the authentication
     * @param acceptto       the acceptto
     * @return the map
     */
    public static Map isUserValid(final Authentication authentication, final AccepttoMultifactorProperties acceptto) {
        val url = StringUtils.appendIfMissing(acceptto.getApiUrl(), "/") + "is_user_valid";
        val email = getUserEmailAttribute(authentication, acceptto);

        if (StringUtils.isBlank(email)) {
            LOGGER.error("Unable to determine email address under attribute [{}]", acceptto.getEmailAttribute());
            return new HashMap<>();
        }

        LOGGER.debug("Principal email address determined from attribute [{}] is [{}]", acceptto.getEmailAttribute(), email);
        val parameters = CollectionUtils.<String, Object>wrap(
            "uid", acceptto.getApplicationId(),
            "secret", acceptto.getSecret(),
            "email", email);

        HttpResponse response = null;
        try {
            response = HttpUtils.executePost(url, parameters, new HashMap<>(0));
            if (response != null) {
                val status = response.getStatusLine().getStatusCode();
                LOGGER.debug("Response status code is [{}]", status);

                if (status == HttpStatus.SC_OK) {
                    val results = MAPPER.readValue(response.getEntity().getContent(), Map.class);
                    LOGGER.debug("Received API response as [{}]", results);
                    return results;
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return new HashMap<>();
    }

    /**
     * Authenticate.
     *
     * @param authentication the authentication
     * @param acceptto       the acceptto
     * @param request        the request
     * @return the map
     */
    public static Map authenticate(final Authentication authentication,
                                   final AccepttoMultifactorProperties acceptto,
                                   final HttpServletRequest request) {
        val url = acceptto.getRegistrationApiUrl();
        val email = getUserEmailAttribute(authentication, acceptto);
        val sessionId = UUID.randomUUID().toString();

        LOGGER.debug("Principal email address determined from attribute [{}] is [{}]", acceptto.getEmailAttribute(), email);
        val parameters = CollectionUtils.<String, Object>wrap(
            "application_uid", acceptto.getApplicationId(),
            "type", "Login",
            "message", acceptto.getMessage(),
            "session_id", sessionId,
            "timeout", acceptto.getTimeout(),
            "email", email);
        CookieUtils.getCookieFromRequest("jwt", request)
            .ifPresent(cookie -> parameters.put("jwt", cookie.getValue()));

        val claims = new JwtClaims();
        claims.setClaim("uid", acceptto.getOrganizationId());
        claims.setExpirationTimeMinutesInTheFuture(1);
        val payload = claims.toJson();
        LOGGER.trace("Authorization payload is [{}}", payload);
        val signingKey = new AesKey(acceptto.getOrganizationSecret().getBytes(StandardCharsets.UTF_8));
        val authzPayload = new String(EncodingUtils.signJwsHMACSha256(signingKey, payload.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        LOGGER.trace("Signed authorization payload is [{}}", authzPayload);

        HttpResponse response = null;
        try {
            val headers = CollectionUtils.<String, Object>wrap("Authorization", "Bearer " + authzPayload);
            response = HttpUtils.executePost(url, parameters, headers);
            if (response != null) {
                val status = response.getStatusLine().getStatusCode();
                LOGGER.debug("Authentication response status code is [{}]", status);
                val results = MAPPER.readValue(response.getEntity().getContent(), Map.class);
                val content = results.get("content").toString();

                val factory = new PublicKeyFactoryBean();
                factory.setResource(acceptto.getRegistrationApiPublicKey().getLocation());
                factory.setSingleton(false);
                factory.setAlgorithm("RSA");
                val publicKey = factory.getObject();

                val decoded = EncodingUtils.verifyJwsSignature(publicKey, content);
                if (decoded != null) {
                    val decodedResult = new String(decoded, StandardCharsets.UTF_8);
                    if (status == HttpStatus.SC_OK) {
                        LOGGER.debug("Received API response as [{}]", decodedResult);
                        return MAPPER.readValue(decodedResult, Map.class);
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return new HashMap<>();
    }
}
