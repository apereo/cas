package org.apereo.cas.web.report;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import org.apereo.cas.web.v1.LegacyValidateController;
import org.apereo.cas.web.v2.ServiceValidateController;
import org.apereo.cas.web.v3.V3ServiceValidateController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.JacksonJsonView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link CasProtocolValidationEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Endpoint(id = "casValidate", defaultAccess = Access.NONE)
public class CasProtocolValidationEndpoint extends BaseCasRestActuatorEndpoint {
    private final ServiceValidateConfigurationContext configurationContext;

    public CasProtocolValidationEndpoint(final CasConfigurationProperties casProperties,
                                         final ServiceValidateConfigurationContext configurationContext) {
        super(casProperties, configurationContext.getApplicationContext());
        this.configurationContext = configurationContext;
    }

    /**
     * Validate.
     *
     * @param request  the request
     * @param response the response
     * @throws Throwable the throwable
     */
    @PostMapping(value = "/validate",
        produces = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
        consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @Operation(summary = "Produce a ticket validation response based on CAS Protocol v1",
        parameters = {
            @Parameter(name = "username", required = true, description = "The username"),
            @Parameter(name = "password", required = false, description = "The password"),
            @Parameter(name = "service", required = true, description = "The service")
        })
    public ModelAndView validate(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        return renderValidationView(request, response, LegacyValidateController.class);
    }

    /**
     * Service validate.
     *
     * @param request  the request
     * @param response the response
     * @throws Throwable the throwable
     */
    @PostMapping(value = "/serviceValidate",
        produces = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
        consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @Operation(summary = "Produce a ticket validation response based on CAS Protocol v2",
        parameters = {
            @Parameter(name = "username", required = true, description = "The username"),
            @Parameter(name = "password", required = false, description = "The password"),
            @Parameter(name = "service", required = true, description = "The service")
        })
    public ModelAndView serviceValidate(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        return renderValidationView(request, response, ServiceValidateController.class);
    }

    /**
     * P3 service validate.
     *
     * @param request  the request
     * @param response the response
     * @throws Throwable the throwable
     */
    @PostMapping(value = "/p3/serviceValidate",
        produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
        consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @Operation(summary = "Produce a ticket validation response based on CAS Protocol v3",
        parameters = {
            @Parameter(name = "username", required = true, description = "The username"),
            @Parameter(name = "password", required = false, description = "The password"),
            @Parameter(name = "service", required = true, description = "The service")
        })
    public ModelAndView p3ServiceValidate(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        return renderValidationView(request, response, V3ServiceValidateController.class);
    }

    protected ModelAndView renderValidationView(final HttpServletRequest request,
                                                final HttpServletResponse response,
                                                final Class viewClass) throws Throwable {
        val selectedService = (WebApplicationService) configurationContext.getServiceFactory()
            .createService(request, WebApplicationService.class);
        Assert.notNull(selectedService, "Service is missing and must be specified");

        val registeredService = configurationContext.getServicesManager().findServiceBy(selectedService);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(selectedService, registeredService);

        val modelAndView = configurationContext.getValidationViewFactory()
            .getModelAndView(request, true, selectedService, viewClass);

        val authentication = buildAuthentication(request, selectedService);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(configurationContext.getApplicationContext())
            .service(selectedService)
            .principal(authentication.getPrincipal())
            .build();

        val attributesToRelease = registeredService.getAttributeReleasePolicy().getAttributes(context);
        val builder = DefaultAuthenticationBuilder.of(
            configurationContext.getApplicationContext(),
            authentication.getPrincipal(),
            configurationContext.getPrincipalFactory(),
            attributesToRelease,
            selectedService,
            registeredService,
            authentication);

        val finalAuthentication = builder.build();
        val assertion = DefaultAssertionBuilder.builder()
            .primaryAuthentication(finalAuthentication)
            .service(selectedService)
            .authentications(CollectionUtils.wrap(finalAuthentication))
            .registeredService(registeredService)
            .build()
            .assemble();

        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, assertion);
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, selectedService);
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_REGISTERED_SERVICE, registeredService);

        val requestWrapper = new ContentCachingRequestWrapper(request, 0);
        val responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            Objects.requireNonNull(modelAndView.getView()).render(modelAndView.getModel(), requestWrapper, responseWrapper);
        } finally {
            val responseArray = responseWrapper.getContentAsByteArray();
            val output = new String(responseArray, responseWrapper.getCharacterEncoding());
            modelAndView.addObject("response", output);
            modelAndView.setView(new JacksonJsonView());
            modelAndView.setStatus(HttpStatus.OK);
        }
        return modelAndView;
    }

    protected Authentication buildAuthentication(final HttpServletRequest request,
                                                 final WebApplicationService selectedService) throws Throwable {
        val password = request.getParameter("password");
        val username = FunctionUtils.throwIfBlank(request.getParameter("username"));
        if (StringUtils.isNotBlank(password)) {
            val credential = new UsernamePasswordCredential(username, password);
            val result = configurationContext.getAuthenticationSystemSupport()
                .finalizeAuthenticationTransaction(selectedService, credential);
            return result.getAuthentication();
        }
        val principal = configurationContext.getPrincipalResolver()
            .resolve(new BasicIdentifiableCredential(username),
                Optional.of(configurationContext.getPrincipalFactory().createPrincipal(username)),
                Optional.empty(), Optional.of(selectedService));
        return DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
    }
}
