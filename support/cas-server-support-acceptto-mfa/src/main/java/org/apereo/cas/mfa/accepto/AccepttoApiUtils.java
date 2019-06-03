package org.apereo.cas.mfa.accepto;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.model.support.mfa.AccepttoMultifactorProperties;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoWebflowUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.AesKey;
import org.springframework.webflow.execution.RequestContext;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
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
     * @param authentication       the authentication
     * @param acceptto             the acceptto
     * @param requestContext       the request context
     * @param apiResponsePublicKey the api response public key
     * @return the map
     */
    public static Map authenticate(final Authentication authentication,
                                   final AccepttoMultifactorProperties acceptto,
                                   final RequestContext requestContext,
                                   final PublicKey apiResponsePublicKey) {
        val url = acceptto.getRegistrationApiUrl();
        val email = getUserEmailAttribute(authentication, acceptto);
        val sessionId = UUID.randomUUID().toString();

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        LOGGER.debug("Principal email address determined from attribute [{}] is [{}]", acceptto.getEmailAttribute(), email);
        val parameters = CollectionUtils.<String, Object>wrap(
            "application_uid", acceptto.getApplicationId(),
            "type", "Login",
            "ip_address", ClientInfoHolder.getClientInfo().getClientIpAddress(),
            "remote_ip_address", request.getRemoteAddr(),
            "message", acceptto.getMessage(),
            "session_id", sessionId,
            "timeout", acceptto.getTimeout(),
            "email", email);

        CookieUtils.getCookieFromRequest("jwt", request)
            .ifPresent(cookie -> parameters.put("jwt", cookie.getValue()));

        AccepttoWebflowUtils.getEGuardianUserId(requestContext).ifPresent(value -> {
            parameters.put("eguardian_user_id", value);
        });
        val currentCredential = WebUtils.getCredential(requestContext);
        if (currentCredential instanceof AccepttoEmailCredential) {
            parameters.put("auth_type", 1);
        }

        LOGGER.debug("Authentication API parameters are assembled as [{}]", parameters);
        HttpResponse response = null;
        try {
            val authzPayload = buildAuthorizationHeaderPayloadForAuthentication(acceptto);
            val headers = CollectionUtils.<String, Object>wrap("Authorization", "Bearer " + authzPayload);
            response = HttpUtils.executePost(url, parameters, headers);
            if (response != null) {
                val status = response.getStatusLine().getStatusCode();
                LOGGER.debug("Authentication response status code is [{}]", status);
                val results = MAPPER.readValue(response.getEntity().getContent(), Map.class);
                LOGGER.trace("Received API response as [{}]", results);
                if (!results.containsKey("content")) {
                    throw new IllegalArgumentException("Unable to locate content in API response");
                }
                val content = results.get("content").toString();
                LOGGER.trace("Loading public key from [{}] to validate response", acceptto.getRegistrationApiPublicKey().getLocation());

                LOGGER.trace("Validating response signature for [{}]", content);
                val decoded = EncodingUtils.verifyJwsSignature(apiResponsePublicKey, content);
                if (decoded != null) {
                    val decodedResult = new String(decoded, StandardCharsets.UTF_8);
                    LOGGER.debug("Received final API response as [{}]", decodedResult);
                    return MAPPER.readValue(decodedResult, Map.class);
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return new HashMap<>();
    }

    private static String buildAuthorizationHeaderPayloadForAuthentication(final AccepttoMultifactorProperties acceptto) {
        val claims = new JwtClaims();
        claims.setClaim("uid", acceptto.getOrganizationId());
        claims.setExpirationTimeMinutesInTheFuture(1);
        val payload = claims.toJson();
        LOGGER.trace("Authorization payload is [{}]", payload);
        val signingKey = new AesKey(acceptto.getOrganizationSecret().getBytes(StandardCharsets.UTF_8));
        LOGGER.trace("Signing authorization payload...");
        val signedBytes = EncodingUtils.signJwsHMACSha256(signingKey, payload.getBytes(StandardCharsets.UTF_8));
        val authzPayload = new String(signedBytes, StandardCharsets.UTF_8);
        LOGGER.trace("Signed authorization payload is [{}]", authzPayload);
        return authzPayload;
    }

    /**
     * Is user device paired?.
     *
     * @param authentication the authentication
     * @param acceptto       the acceptto
     * @return the boolean
     */
    public static boolean isUserDevicePaired(final Authentication authentication, final AccepttoMultifactorProperties acceptto) {
        val results = isUserValid(authentication, acceptto);
        if (results != null && results.containsKey("device_paired")) {
            return BooleanUtils.toBoolean(results.get("device_paired").toString());
        }
        return false;
    }

    /**
     * Generate qr code hash.
     *
     * @param authentication  the authentication
     * @param acceptto        the acceptto
     * @param invitationToken the invitation token
     * @return the string
     * @throws Exception the exception
     */
    public static String generateQRCodeHash(final Authentication authentication, final AccepttoMultifactorProperties acceptto,
                                            final String invitationToken) throws Exception {
        val email = getUserEmailAttribute(authentication, acceptto);
        val hash = CollectionUtils.wrap("invitation_token", invitationToken, "email_address", email);
        val result = MAPPER.writeValueAsString(hash);
        return EncodingUtils.encodeBase64(result);
    }

    /**
     * Decode invitation token to string.
     *
     * @param token the token
     * @return the string
     * @throws Exception the exception
     */
    public static String decodeInvitationToken(final String token) throws Exception {
        val decoded = EncodingUtils.decodeBase64(token);
        val results = MAPPER.readValue(decoded, Map.class);
        return results.get("invitation_token").toString();
    }
}
