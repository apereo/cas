package org.apereo.cas.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.wss4j.dom.WSConstants;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * This is {@link SecurityTokenServiceClientBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SecurityTokenServiceClientBuilder {
    private final WsFederationProperties wsFederationProperties;
    private final String prefix;

    public SecurityTokenServiceClientBuilder(final WsFederationProperties wsFederationProperties, final String prefix) {
        this.wsFederationProperties = wsFederationProperties;
        this.prefix = prefix;
    }

    /**
     * Build client for security token requests.
     *
     * @param service the rp
     * @return the security token service client
     */
    public SecurityTokenServiceClient buildClientForSecurityTokenRequests(final WSFederationRegisteredService service) {
        final Bus cxfBus = BusFactory.getDefaultBus();
        final SecurityTokenServiceClient sts = new SecurityTokenServiceClient(cxfBus);
        sts.setAddressingNamespace(StringUtils.defaultIfBlank(service.getAddressingNamespace(), WSFederationConstants.HTTP_WWW_W3_ORG_2005_08_ADDRESSING));
        sts.setTokenType(StringUtils.defaultIfBlank(service.getTokenType(), WSConstants.WSS_SAML2_TOKEN_TYPE));
        sts.setKeyType(WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512_BEARER);
        sts.setWsdlLocation(prepareWsdlLocation(service));
        if (StringUtils.isNotBlank(service.getPolicyNamespace())) {
            sts.setWspNamespace(service.getPolicyNamespace());
        }
        final String namespace = StringUtils.defaultIfBlank(service.getNamespace(), WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512);
        sts.setServiceQName(new QName(namespace, StringUtils.defaultIfBlank(service.getWsdlService(), WSFederationConstants.SECURITY_TOKEN_SERVICE)));
        sts.setEndpointQName(new QName(namespace, service.getWsdlEndpoint()));
        sts.getProperties().putAll(new HashMap<>());
        return sts;
    }

    private String prepareWsdlLocation(final WSFederationRegisteredService service) {
        if (StringUtils.isNotBlank(service.getWsdlLocation())) {
            return service.getWsdlLocation();
        }
        final String wsdl = String.format(WSFederationConstants.ENDPOINT_STS_REALM_WSDL, wsFederationProperties.getIdp().getRealmName());
        final String location = this.prefix.concat(wsdl);
        return location;
    }

    /**
     * Build client for relying party token responses.
     *
     * @param securityToken the security token
     * @param service       the service
     * @return the security token service client
     */
    public SecurityTokenServiceClient buildClientForRelyingPartyTokenResponses(final SecurityToken securityToken,
                                                                               final WSFederationRegisteredService service) {
        final Bus cxfBus = BusFactory.getDefaultBus();
        final SecurityTokenServiceClient sts = new SecurityTokenServiceClient(cxfBus);
        sts.setAddressingNamespace(StringUtils.defaultIfBlank(service.getAddressingNamespace(), WSFederationConstants.HTTP_WWW_W3_ORG_2005_08_ADDRESSING));
        sts.setWsdlLocation(prepareWsdlLocation(service));
        final String namespace = StringUtils.defaultIfBlank(service.getNamespace(), WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512);
        sts.setServiceQName(new QName(namespace, service.getWsdlService()));
        sts.setEndpointQName(new QName(namespace, service.getWsdlEndpoint()));
        sts.setEnableAppliesTo(StringUtils.isNotBlank(service.getAppliesTo()));
        sts.setOnBehalfOf(securityToken.getToken());
        sts.setKeyType(WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512_BEARER);
        sts.setTokenType(StringUtils.defaultIfBlank(service.getTokenType(), WSConstants.WSS_SAML2_TOKEN_TYPE));

        if (StringUtils.isNotBlank(service.getPolicyNamespace())) {
            sts.setWspNamespace(service.getPolicyNamespace());
        }

        return sts;
    }
}
