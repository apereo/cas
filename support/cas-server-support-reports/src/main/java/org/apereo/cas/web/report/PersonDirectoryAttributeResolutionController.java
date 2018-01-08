package org.apereo.cas.web.report;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.CopyPrintWriter;
import org.apereo.cas.util.io.CopyServletOutputStream;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link PersonDirectoryAttributeResolutionController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PersonDirectoryAttributeResolutionController extends BaseCasMvcEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonDirectoryAttributeResolutionController.class);

    private final ServicesManager servicesManager;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final PrincipalResolver personDirectoryPrincipalResolver;
    private final ServiceFactory<WebApplicationService> serviceFactory;
    private final PrincipalFactory principalFactory;

    private final View cas3ServiceSuccessView;
    private final View cas3ServiceJsonView;
    private final View cas2ServiceSuccessView;
    private final View cas1ServiceSuccessView;

    public PersonDirectoryAttributeResolutionController(final CasConfigurationProperties casProperties,
                                                        final ServicesManager servicesManager,
                                                        final AuthenticationSystemSupport authenticationSystemSupport,
                                                        final PrincipalResolver personDirectoryPrincipalResolver,
                                                        final ServiceFactory<WebApplicationService> serviceFactory,
                                                        final PrincipalFactory principalFactory,
                                                        final View cas3ServiceSuccessView,
                                                        final View cas3ServiceJsonView,
                                                        final View cas2ServiceSuccessView,
                                                        final View cas1ServiceSuccessView) {
        super("attrresolution", "/attrresolution", casProperties.getMonitor().getEndpoints().getAttributeResolution(), casProperties);
        this.servicesManager = servicesManager;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.personDirectoryPrincipalResolver = personDirectoryPrincipalResolver;
        this.serviceFactory = serviceFactory;
        this.principalFactory = principalFactory;
        this.cas3ServiceSuccessView = cas3ServiceSuccessView;
        this.cas3ServiceJsonView = cas3ServiceJsonView;
        this.cas2ServiceSuccessView = cas2ServiceSuccessView;
        this.cas1ServiceSuccessView = cas1ServiceSuccessView;
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                 final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        final Map model = new LinkedHashMap();
       
        return new ModelAndView("monitoring/attrresolution", model);
    }

    /**
     * Resolve principal attributes map.
     *
     * @param uid      the uid
     * @param request  the request
     * @param response the response
     * @return the map
     */
    @PostMapping(value = "/resolveattrs")
    @ResponseBody
    public Map<String, Object> resolvePrincipalAttributes(@RequestParam final String uid,
                                                          final HttpServletRequest request,
                                                          final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        final Principal p = personDirectoryPrincipalResolver.resolve(new BasicIdentifiableCredential(uid));
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("uid", p.getId());
        map.put("attributes", p.getAttributes());
        return map;
    }

    /**
     * Release principal attributes map.
     *
     * @param username the username
     * @param password the password
     * @param service  the service
     * @param request  the request
     * @param response the response
     * @return the map
     * @throws Exception the exception
     */
    @PostMapping(value = "/releaseattrs")
    @ResponseBody
    public Map<String, Object> releasePrincipalAttributes(@RequestParam final String username,
                                                          @RequestParam final String password,
                                                          @RequestParam final String service,
                                                          final HttpServletRequest request,
                                                          final HttpServletResponse response) throws Exception {
        ensureEndpointAccessIsAuthorized(request, response);

        final Map<String, Object> resValidation = new HashMap<>();
        final Service selectedService = this.serviceFactory.createService(service);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(selectedService);

        final UsernamePasswordCredential credential = new UsernamePasswordCredential(username, password);
        final AuthenticationResult result = this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(selectedService, credential);
        final Authentication authentication = result.getAuthentication();

        final Principal principal = authentication.getPrincipal();
        final Map<String, Object> attributesToRelease = registeredService.getAttributeReleasePolicy()
                .getAttributes(principal, selectedService, registeredService);
        final String principalId = registeredService.getUsernameAttributeProvider().resolveUsername(principal, selectedService, registeredService);
        final Principal modifiedPrincipal = this.principalFactory.createPrincipal(principalId, attributesToRelease);
        final AuthenticationBuilder builder = DefaultAuthenticationBuilder.newInstance(authentication);
        builder.setPrincipal(modifiedPrincipal);
        final Authentication finalAuthentication = builder.build();
        final Assertion assertion = new DefaultAssertionBuilder(finalAuthentication)
                .with(selectedService)
                .with(CollectionUtils.wrap(finalAuthentication))
                .build();

        final Map<String, Object> model = new LinkedHashMap<>();
        model.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, assertion);
        model.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, selectedService);

        resValidation.put("registeredService", registeredService);

        String copy = renderViewAndGetResult(this.cas1ServiceSuccessView, model, request, response).getKey().getCopy();
        resValidation.put("cas1Response", StringEscapeUtils.escapeXml11(copy));

        if (casProperties.getView().getCas2().isV3ForwardCompatible()) {
            copy = renderViewAndGetResult(this.cas3ServiceSuccessView, model, request, response).getKey().getCopy();
        } else {
            copy = renderViewAndGetResult(this.cas2ServiceSuccessView, model, request, response).getKey().getCopy();
        }
        resValidation.put("cas2Response", StringEscapeUtils.escapeXml11(copy));

        copy = renderViewAndGetResult(this.cas3ServiceSuccessView, model, request, response).getKey().getCopy();
        resValidation.put("cas3XmlResponse", StringEscapeUtils.escapeXml11(copy));

        copy = renderViewAndGetResult(this.cas3ServiceJsonView, model, request, response).getValue().getStringCopy();
        resValidation.put("cas3JsonResponse", copy);

        response.reset();

        return resValidation;
    }

    private Pair<CopyPrintWriter, CopyServletOutputStream> renderViewAndGetResult(final View view,
                                                                                  final Map<String, ?> model,
                                                                                  final HttpServletRequest request,
                                                                                  final HttpServletResponse response) throws Exception {
        final CopyPrintWriter writer = new CopyPrintWriter();
        final CopyServletOutputStream stream = new CopyServletOutputStream(response.getOutputStream());
        final HttpServletResponse wrapper = new HttpServletResponseWrapper(response) {
            @Override
            public PrintWriter getWriter() {
                return writer;
            }

            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        view.render(model, request, wrapper);
        return Pair.of(writer, stream);
    }
}
