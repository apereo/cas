package org.apereo.cas.tokens;

import com.nimbusds.jwt.JWTClaimsSet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.rest.factory.TicketStatusResourcePreprocessor;
import org.apereo.cas.token.TokenConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link JWTTicketStatusResourcePreprocessor}.
 *
 * @author Francesco Chicchiriccò
 * @since 5.2.2
 */
public class JWTTicketStatusResourcePreprocessor implements TicketStatusResourcePreprocessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTTicketStatusResourcePreprocessor.class);

    private final CipherExecutor<String, String> tokenCipherExecutor;

    public JWTTicketStatusResourcePreprocessor(final CipherExecutor<String, String> tokenCipherExecutor) {
        this.tokenCipherExecutor = tokenCipherExecutor;
    }

    @Override
    public String preprocess(final String id, final HttpServletRequest request) {
        String tokenParam = request.getParameter(TokenConstants.PARAMETER_NAME_TOKEN);
        if (StringUtils.isBlank(tokenParam)) {
            tokenParam = request.getHeader(TokenConstants.PARAMETER_NAME_TOKEN);
        }
        if (StringUtils.isBlank(tokenParam) || !BooleanUtils.toBoolean(tokenParam)) {
            LOGGER.debug("The request indicates that ticket-granting ticket should not be extracted from JWT");
            return id;
        }

        final JWTClaimsSet claimsSet;
        try {
            final String jwtJson = tokenCipherExecutor.decode(id);
            claimsSet = JWTClaimsSet.parse(jwtJson);
        } catch (final Exception e) {
            LOGGER.error("Could not extract JWT from {}", id, e);
            return id;
        }

        return claimsSet.getJWTID();
    }

}
