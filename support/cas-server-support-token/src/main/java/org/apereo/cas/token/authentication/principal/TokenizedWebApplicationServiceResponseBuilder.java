package org.apereo.cas.token.authentication.principal;

import com.google.common.base.Throwables;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.TokenConstants;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 * This is {@link TokenizedWebApplicationServiceResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TokenizedWebApplicationServiceResponseBuilder extends WebApplicationServiceResponseBuilder {
    private static final long serialVersionUID = -2863268279032438778L;

    @Autowired
    private CasConfigurationProperties casProperties;

    private final ServicesManager servicesManager;

    private final CipherExecutor<String, String> tokenCipherExecutor;

    public TokenizedWebApplicationServiceResponseBuilder(final ServicesManager servicesManager,
                                                         final CipherExecutor tokenCipherExecutor) {
        this.servicesManager = servicesManager;
        this.tokenCipherExecutor = tokenCipherExecutor;
    }

    @Override
    protected WebApplicationService buildInternal(final WebApplicationService service,
                                                  final Map<String, String> parameters) {
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        final Map.Entry<String, RegisteredServiceProperty> property = registeredService.getProperties()
                .entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(TokenConstants.PROPERTY_NAME_TOKEN_AS_RESPONSE)
                        && BooleanUtils.toBoolean(entry.getValue().getValue()))
                .distinct()
                .findFirst()
                .orElse(null);

        if (property == null) {
            return super.buildInternal(service, parameters);
        }

        final String jwt = generateToken(service, parameters);
        final TokenWebApplicationService jwtService =
                new TokenWebApplicationService(service.getId(), service.getOriginalUrl(), service.getArtifactId());
        jwtService.setFormat(service.getFormat());
        jwtService.setLoggedOutAlready(service.isLoggedOutAlready());
        parameters.put(CasProtocolConstants.PARAMETER_TICKET, jwt);
        return jwtService;
    }

    /**
     * Generate token string.
     *
     * @param service    the service
     * @param parameters the parameters
     * @return the jwt
     */
    protected String generateToken(final Service service, final Map<String, String> parameters) {
        try {
            final String ticketId = parameters.get(CasProtocolConstants.PARAMETER_TICKET);
            final Cas30ServiceTicketValidator validator = new Cas30ServiceTicketValidator(casProperties.getServer().getPrefix());
            final Assertion assertion = validator.validate(ticketId, service.getId());
            final JWTClaimsSet.Builder claims =
                    new JWTClaimsSet.Builder()
                            .audience(service.getId())
                            .issuer(casProperties.getServer().getPrefix())
                            .jwtID(ticketId)
                            .expirationTime(new Date())
                            .issueTime(assertion.getAuthenticationDate())
                            .subject(assertion.getPrincipal().getName());
            assertion.getAttributes().forEach((k, v) -> claims.claim(k, v));
            assertion.getPrincipal().getAttributes().forEach((k, v) -> claims.claim(k, v));
            final JWTClaimsSet claimsSet = claims.build();
            final JSONObject object = claimsSet.toJSONObject();
            return tokenCipherExecutor.encode(object.toJSONString());
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Token/JWT web application service.
     */
    public static class TokenWebApplicationService extends AbstractWebApplicationService {

        private static final long serialVersionUID = -8844121291312069964L;

        public TokenWebApplicationService(final String id, final String originalUrl, final String artifactId) {
            super(id, originalUrl, artifactId);
        }
    }
}
