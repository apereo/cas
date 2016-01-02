package org.jasig.cas.support.saml.web.idp.profile;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.support.saml.SamlIdPConstants;
import org.jasig.cas.support.saml.SamlIdPUtils;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link SLOPostProfileHandlerController}, responsible for
 * handling requests for SAML2 SLO.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Controller("sloPostProfileHandlerController")
public class SLOPostProfileHandlerController extends AbstractSamlProfileHandlerController {

    @Value("${cas.samlidp.logout.request.force.signed:true}")
    private boolean forceSignedLogoutRequests = true;

    /**
     * Instantiates a new Slo post profile handler controller.
     */
    public SLOPostProfileHandlerController() {
    }

    /**
     * Handle SLO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @RequestMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SLO_PROFILE_POST, method = RequestMethod.POST)
    protected void handleSaml2ProfileSLOPostRequest(final HttpServletResponse response,
                                                    final HttpServletRequest request) throws Exception {
        handleSloPostProfileRequest(response, request, new HTTPPostDecoder());
    }

    /**
     * Handle profile request.
     *
     * @param response the response
     * @param request  the request
     * @param decoder  the decoder
     * @throws Exception the exception
     */
    protected void handleSloPostProfileRequest(final HttpServletResponse response,
                                               final HttpServletRequest request,
                                               final BaseHttpServletRequestXMLMessageDecoder decoder) throws Exception {
        final LogoutRequest logoutRequest = decodeRequest(request, decoder, LogoutRequest.class);
        if (this.forceSignedLogoutRequests && !logoutRequest.isSigned()) {
            throw new SAMLException("Logout request is not signed but should be.");
        } else if (logoutRequest.isSigned()) {
            final ChainingMetadataResolver resolver = getMetadataResolverForAllSamlServices(logoutRequest);
            this.samlObjectSigner.verifySamlProfileRequestIfNeeded(logoutRequest, resolver);
        }
        SamlIdPUtils.logSamlObject(this.configBean, logoutRequest);
        response.sendRedirect(this.casServerPrefix.concat("/logout"));
    }

    /**
     * Gets chaining metadata resolver for all saml services.
     *
     * @param request the request
     * @return the chaining metadata resolver for all saml services
     * @throws ResolverException the resolver exception
     */
    protected ChainingMetadataResolver getMetadataResolverForAllSamlServices(final RequestAbstractType request) throws Exception {
        final Predicate p = Predicates.instanceOf(SamlRegisteredService.class);
        final Collection<RegisteredService> registeredServices = this.servicesManager.findServiceBy(p);
        final List<MetadataResolver> resolvers = new ArrayList<>();
        final ChainingMetadataResolver resolver = new ChainingMetadataResolver();

        for (final RegisteredService registeredService : registeredServices) {
            final SamlRegisteredService samlRegisteredService = SamlRegisteredService.class.cast(registeredService);
            final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
                    SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver,
                            samlRegisteredService, request);
            resolvers.add(adaptor.getMetadataResolver());
        }
        resolver.setResolvers(resolvers);
        resolver.setId(getClass().getName());
        resolver.initialize();
        return resolver;
    }
}
