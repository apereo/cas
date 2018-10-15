package org.apereo.cas.support.saml;

import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
        val registeredService = new SamlRegisteredService();
        registeredService.setId(100);
        registeredService.setName("SAML");
        registeredService.setServiceId("https://sp.testshib.org/shibboleth-sp");
        registeredService.setMetadataLocation("classpath:metadata/testshib-providers.xml");

        val request = new MockHttpServletRequest();
        request.addParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID, registeredService.getServiceId());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        return registeredService;
    }
}
