package org.apereo.cas.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.wss4j.dom.WSConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * This is {@link SecurityTokenServiceAuthenticationPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SecurityTokenServiceAuthenticationPostProcessor implements AuthenticationPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityTokenServiceAuthenticationPostProcessor.class);

    private final ServicesManager servicesManager;
    private final IdentityProviderConfigurationService identityProviderConfigurationService;

    public SecurityTokenServiceAuthenticationPostProcessor(final ServicesManager servicesManager,
                                                           final IdentityProviderConfigurationService identityProviderConfigurationService) {
        this.servicesManager = servicesManager;
        this.identityProviderConfigurationService = identityProviderConfigurationService;
    }

    @Override
    public void process(final AuthenticationTransaction transaction, final AuthenticationBuilder builder) {
        final Service service = transaction.getService();
        if (service != null && this.servicesManager != null) {
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.warn("Service [{}] is not allowed to use SSO.", registeredService);
                throw new UnauthorizedSsoServiceException();
            }
            final WSFederationRegisteredService rp = WSFederationRegisteredService.class.cast(registeredService);
            final Bus cxfBus = BusFactory.getDefaultBus();
            final IdentityProviderSTSClient sts = new IdentityProviderSTSClient(cxfBus);
            sts.setAddressingNamespace("http://www.w3.org/2005/08/addressing");
            if (StringUtils.isNotBlank(rp.getTokenType())) {
                sts.setTokenType(rp.getTokenType());
            } else {
                sts.setTokenType(WSConstants.WSS_SAML2_TOKEN_TYPE);
            }
            sts.setKeyType(WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512_BEARER);
            sts.setWsdlLocation(rp.getWsdlLocation());

            final String namespace = StringUtils.defaultIfBlank(rp.getNamespace(), WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512);
            sts.setServiceQName(new QName(namespace, rp.getWsdlService()));
            sts.setEndpointQName(new QName(namespace, rp.getWsdlEndpoint()));

            sts.getProperties().putAll(new HashMap<>());
            if (rp.isUse200502Namespace()) {
                sts.setNamespace(WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_02_TRUST);
            }

            if (rp.getLifetime() > 0) {
                sts.setEnableLifetime(true);
                sts.setTtl(Long.valueOf(rp.getLifetime()).intValue());
            }

//            transaction.getCredentials()
//                    .stream()
//                    .filter(UsernamePasswordCredential.class::isInstance)
//                    .map(UsernamePasswordCredential.class::cast)
//                    .forEach(c -> {
//                        sts.getProperties().put(SecurityConstants.USERNAME, c.getUsername());
//                        sts.getProperties().put(SecurityConstants.PASSWORD, c.getPassword());
//
//                        // urn:fediz:idp
//                        final SecurityToken token = sts.requestSecurityToken(rp.getAppliesTo());
//                    });
        }


    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
