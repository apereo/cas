package org.apereo.cas.token;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;

/**
 * This is {@link JwtTokenCipherSigningPublicKeyEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Endpoint(id = "jwtTicketSigningPublicKey", enableByDefault = false)
public class JwtTokenCipherSigningPublicKeyEndpoint extends BaseCasActuatorEndpoint {
    private final CipherExecutor tokenCipherExecutor;

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    public JwtTokenCipherSigningPublicKeyEndpoint(final CasConfigurationProperties casProperties,
                                                  final CipherExecutor tokenCipherExecutor,
                                                  final ServicesManager servicesManager,
                                                  final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        super(casProperties);
        this.tokenCipherExecutor = tokenCipherExecutor;
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    /**
     * Fetch public key.
     *
     * @param service the service
     * @return the string
     * @throws Exception the exception
     */
    @ReadOperation(produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Get public key for signing operations", parameters = {
        @Parameter(name = "service")
    })
    public String fetchPublicKey(@Nullable final String service) throws Exception {
        var signingKey = tokenCipherExecutor.getSigningKey();

        if (StringUtils.isNotBlank(service)) {
            val registeredService = servicesManager.findServiceBy(webApplicationServiceFactory.createService(service));
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
            val serviceCipher = new RegisteredServiceJwtTicketCipherExecutor();
            if (serviceCipher.supports(registeredService)) {
                val cipher = serviceCipher.getTokenTicketCipherExecutorForService(registeredService);
                if (cipher.isEnabled()) {
                    signingKey = cipher.getSigningKey();
                }
            }
        }

        if (signingKey instanceof RSAPrivateCrtKey) {
            val rsaSigningKey = (RSAPrivateCrtKey) signingKey;
            val factory = KeyFactory.getInstance("RSA");
            val publicKey = factory.generatePublic(new RSAPublicKeySpec(rsaSigningKey.getModulus(), rsaSigningKey.getPublicExponent()));
            return EncodingUtils.encodeBase64(publicKey.getEncoded());
        }
        return null;
    }
}
