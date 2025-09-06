package org.apereo.cas.token.authentication;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.token.TokenAuthenticationProperties;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import java.util.ArrayList;
import java.util.Map;

/**
 * This is {@link OAuth20AccessTokenAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class OAuth20AccessTokenAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final ConfigurableApplicationContext applicationContext;

    public OAuth20AccessTokenAuthenticationHandler(
        final PrincipalFactory principalFactory,
        final ConfigurableApplicationContext applicationContext,
        final TokenAuthenticationProperties properties) {
        super(StringUtils.EMPTY, principalFactory, properties.getOrder() - 1);
        this.applicationContext = applicationContext;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(
        final Credential credential, final Service service) throws PreventedException {
        try {
            val configurationContext = applicationContext.getBean(OAuth20ConfigurationContext.BEAN_NAME, OAuth20ConfigurationContext.class);
            val tokenCredential = (BasicIdentifiableCredential) credential;
            val accessTokenId = OAuth20JwtAccessTokenEncoder.toDecodableCipher(configurationContext.getAccessTokenJwtBuilder()).decode(tokenCredential.getId());
            val decodedToken = configurationContext.getTicketRegistry().getTicket(accessTokenId, OAuth20AccessToken.class);
            Assert.isTrue(decodedToken != null && !decodedToken.isExpired(), "Access token is invalid or expired");
            val claims = configurationContext.getUserProfileDataCreator().createFrom(decodedToken);
            val attributes = CollectionUtils.toMultiValuedMap((Map) claims.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES));
            val principalId = decodedToken.getAuthentication().getPrincipal().getId();
            val principal = principalFactory.createPrincipal(principalId, attributes);
            tokenCredential.setId(principal.getId());
            return createHandlerResult(tokenCredential, principal, new ArrayList<>());
        } catch (final Throwable e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof BasicIdentifiableCredential;
    }
}

