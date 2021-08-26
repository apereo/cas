package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitConfigurer;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.wss4j.common.WSS4JConstants;

import javax.net.ssl.HostnameVerifier;
import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * This is {@link SecurityTokenServiceClientBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class SecurityTokenServiceClientBuilder {
    private final WsFederationProperties wsFederationProperties;

    private final String prefix;

    private final HostnameVerifier hostnameVerifier;

    private final CasSSLContext sslContext;

    /**
     * Build client for security token requests.
     *
     * @param service the rp
     * @return the security token service client
     */
    public SecurityTokenServiceClient buildClientForSecurityTokenRequests(final WSFederationRegisteredService service) {
        val cxfBus = BusFactory.getDefaultBus();
        val sts = new SecurityTokenServiceClient(cxfBus);
        sts.setAddressingNamespace(StringUtils.defaultIfBlank(service.getAddressingNamespace(),
            WSFederationConstants.HTTP_WWW_W3_ORG_2005_08_ADDRESSING));
        sts.setTokenType(StringUtils.defaultIfBlank(service.getTokenType(), WSS4JConstants.WSS_SAML2_TOKEN_TYPE));
        sts.setKeyType(WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512_BEARER);
        sts.setWsdlLocation(prepareWsdlLocation(service));
        if (StringUtils.isNotBlank(service.getPolicyNamespace())) {
            sts.setWspNamespace(service.getPolicyNamespace());
        }
        val namespace = StringUtils.defaultIfBlank(service.getNamespace(),
            WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512);
        sts.setServiceQName(new QName(namespace, StringUtils.defaultIfBlank(service.getWsdlService(),
            WSFederationConstants.SECURITY_TOKEN_SERVICE)));
        sts.setEndpointQName(new QName(namespace, service.getWsdlEndpoint()));
        sts.setEnableAppliesTo(StringUtils.isNotBlank(service.getAppliesTo()));
        sts.setKeyType(WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512_BEARER);
        sts.getProperties().putAll(new HashMap<>(0));
        sts.setTokenType(StringUtils.defaultIfBlank(service.getTokenType(), WSS4JConstants.WSS_SAML2_TOKEN_TYPE));
        if (StringUtils.isNotBlank(service.getPolicyNamespace())) {
            sts.setWspNamespace(service.getPolicyNamespace());
        }
        val tlsClientParams = getTlsClientParameters();
        sts.setTlsClientParameters(tlsClientParams);
        val configurer = new CasHTTPConduitConfigurer(tlsClientParams);
        cxfBus.setExtension(configurer, HTTPConduitConfigurer.class);
        return sts;
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
        val sts = buildClientForSecurityTokenRequests(service);
        sts.setOnBehalfOf(securityToken.getToken());
        return sts;
    }

    @RequiredArgsConstructor
    private static class CasHTTPConduitConfigurer implements HTTPConduitConfigurer {
        private final TLSClientParameters tlsClientParameters;

        @Override
        public void configure(final String name, final String addr, final HTTPConduit httpConduit) {
            httpConduit.setTlsClientParameters(tlsClientParameters);
        }
    }

    private TLSClientParameters getTlsClientParameters() {
        val tlsClientParams = new TLSClientParameters();
        tlsClientParams.setHostnameVerifier(hostnameVerifier);
        tlsClientParams.setSslContext(sslContext.getSslContext());
        tlsClientParams.setSecureRandom(RandomUtils.getNativeInstance());
        tlsClientParams.setKeyManagers(sslContext.getKeyManagers());
        tlsClientParams.setTrustManagers(sslContext.getTrustManagers());
        tlsClientParams.setSSLSocketFactory(sslContext.getSslContext().getSocketFactory());
        return tlsClientParams;
    }

    private String prepareWsdlLocation(final WSFederationRegisteredService service) {
        if (StringUtils.isNotBlank(service.getWsdlLocation())) {
            return service.getWsdlLocation();
        }
        val wsdl = String.format(WSFederationConstants.ENDPOINT_STS_REALM_WSDL, wsFederationProperties.getIdp().getRealmName());
        return this.prefix.concat(wsdl);
    }
}
