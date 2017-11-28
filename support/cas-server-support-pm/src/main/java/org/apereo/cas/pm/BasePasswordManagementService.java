package org.apereo.cas.pm;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.inspektr.audit.annotation.Audit;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This is {@link BasePasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class BasePasswordManagementService implements PasswordManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasePasswordManagementService.class);

    /**
     * Password management settings.
     */
    protected final PasswordManagementProperties properties;

    private final CipherExecutor<Serializable, String> cipherExecutor;
    private final String issuer;
    
    public BasePasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final String issuer,
                                         final PasswordManagementProperties properties) {
        this.cipherExecutor = cipherExecutor;
        this.issuer = issuer;
        this.properties = properties;
    }

    @Override
    public String parseToken(final String token) {
        try {
            final String json = this.cipherExecutor.decode(token);
            final JwtClaims claims = JwtClaims.parse(json);

            if (!claims.getIssuer().equals(issuer)) {
                LOGGER.error("Token issuer does not match CAS");
                return null;
            }
            if (claims.getAudience().isEmpty() || !claims.getAudience().get(0).equals(issuer)) {
                LOGGER.error("Token audience does not match CAS");
                return null;
            }
            if (StringUtils.isBlank(claims.getSubject())) {
                LOGGER.error("Token has no subject identifier");
                return null;
            }

            final ClientInfo holder = ClientInfoHolder.getClientInfo();
            if (!claims.getStringClaimValue("origin").equals(holder.getServerIpAddress())) {
                LOGGER.error("Token origin does not match CAS");
                return null;
            }
            if (!claims.getStringClaimValue("client").equals(holder.getClientIpAddress())) {
                LOGGER.error("Token client does not match CAS");
                return null;
            }

            if (claims.getExpirationTime().isBefore(NumericDate.now())) {
                LOGGER.error("Token has expired.");
                return null;
            }

            return claims.getSubject();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String createToken(final String to) {
        try {
            final String token = UUID.randomUUID().toString();
            final JwtClaims claims = new JwtClaims();
            claims.setJwtId(token);
            claims.setIssuer(issuer);
            claims.setAudience(issuer);
            claims.setExpirationTimeMinutesInTheFuture(properties.getReset().getExpirationMinutes());
            claims.setIssuedAtToNow();

            final ClientInfo holder = ClientInfoHolder.getClientInfo();
            claims.setStringClaim("origin", holder.getServerIpAddress());
            claims.setStringClaim("client", holder.getClientIpAddress());

            claims.setSubject(to);
            final String json = claims.toJson();
            return this.cipherExecutor.encode(json);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Audit(action = "CHANGE_PASSWORD",
            actionResolverName = "CHANGE_PASSWORD_ACTION_RESOLVER",
            resourceResolverName = "CHANGE_PASSWORD_RESOURCE_RESOLVER")
    @Override
    public boolean change(final Credential c, final PasswordChangeBean bean) throws InvalidPasswordException {
        return changeInternal(c, bean);
    }

    /**
     * Change password internally, by the impl.
     *
     * @param c    the credential
     * @param bean the bean
     * @return the boolean
     * @throws InvalidPasswordException if new password fails downstream validation
     */
    public boolean changeInternal(final Credential c, final PasswordChangeBean bean) throws InvalidPasswordException {
        return false;
    }

    /**
     * Orders security questions consistently.
     *
     * @param questionMap A map of question/answer key/value pairs
     * @return A list of questions in a consistent order
     */
    public static List<String> canonicalizeSecurityQuestions(final Map<String, String> questionMap) {
        final List<String> keys = new ArrayList<>(questionMap.keySet());
        keys.sort(String.CASE_INSENSITIVE_ORDER);
        return keys;
    }
}
