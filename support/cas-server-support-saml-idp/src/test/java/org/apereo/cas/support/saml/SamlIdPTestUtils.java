package org.apereo.cas.support.saml;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.RandomUtils;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * This is {@link SamlIdPTestUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@UtilityClass
public class SamlIdPTestUtils {

    /**
     * Gets saml registered service.
     *
     * @return the saml registered service
     */
    public static SamlRegisteredService getSamlRegisteredService() {
        return getSamlRegisteredService("https://sp.testshib.org/shibboleth-sp");
    }

    /**
     * Gets saml registered service.
     *
     * @param serviceId the service id
     * @return the saml registered service
     */
    public static SamlRegisteredService getSamlRegisteredService(final String serviceId) {
        val registeredService = new SamlRegisteredService();
        registeredService.setId(RandomUtils.nextInt());
        registeredService.setName("SAML");
        registeredService.setServiceId(serviceId);
        registeredService.setMetadataLocation("classpath:metadata/testshib-providers.xml");

        val request = new MockHttpServletRequest();
        request.addParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID, registeredService.getServiceId());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        return registeredService;
    }

    /**
     * Gets authn request.
     *
     * @param openSamlConfigBean    the open saml config bean
     * @param samlRegisteredService the saml registered service
     * @return the authn request
     */
    public static AuthnRequest getAuthnRequest(final OpenSamlConfigBean openSamlConfigBean,
                                               final SamlRegisteredService samlRegisteredService) {
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        var authnRequest = (AuthnRequest) Objects.requireNonNull(builder).buildObject();
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) Objects.requireNonNull(builder).buildObject();
        issuer.setValue(samlRegisteredService.getServiceId());
        authnRequest.setIssuer(issuer);
        return authnRequest;
    }
}
