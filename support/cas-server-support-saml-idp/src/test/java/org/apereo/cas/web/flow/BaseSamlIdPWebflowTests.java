package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasSamlIdPAutoConfiguration;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseSamlIdPWebflowTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ImportAutoConfiguration({
    CasCoreSamlAutoConfiguration.class,
    CasSamlIdPAutoConfiguration.class
})
@ExtendWith(CasTestExtension.class)
public abstract class BaseSamlIdPWebflowTests extends BaseWebflowConfigurerTests {

    @Autowired
    @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
    protected SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    protected OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier(SamlIdPObjectSigner.DEFAULT_BEAN_NAME)
    protected SamlIdPObjectSigner samlIdPObjectSigner;

    @Autowired
    @Qualifier("samlIdPDistributedSessionStore")
    protected SessionStore samlIdPDistributedSessionStore;

    protected AuthnRequest signAuthnRequest(final HttpServletRequest request, final HttpServletResponse response,
                                            final AuthnRequest authnRequest, final SamlRegisteredService samlRegisteredService,
                                            final MessageContext messageContext) throws Exception {
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            samlRegisteredService, samlRegisteredService.getServiceId()).get();
        return samlIdPObjectSigner.encode(authnRequest, samlRegisteredService,
            adaptor, response, request, SAMLConstants.SAML2_POST_BINDING_URI,
            authnRequest, messageContext);
    }

    protected static AuthnRequest getAuthnRequestFor(final String service) {
        val authnRequest = mock(AuthnRequest.class);
        when(authnRequest.getID()).thenReturn(UUID.randomUUID().toString());
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service);
        when(authnRequest.getIssuer()).thenReturn(issuer);
        return authnRequest;
    }
}


