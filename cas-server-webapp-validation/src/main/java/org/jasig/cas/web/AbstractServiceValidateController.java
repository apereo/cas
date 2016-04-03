package org.jasig.cas.web;

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.CasViewConstants;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationContextValidator;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationResult;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultAuthenticationSystemSupport;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategyUtils;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.AbstractTicketValidationException;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationContextTicketValidationException;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.util.Pair;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ValidationResponseType;
import org.jasig.cas.validation.ValidationSpecification;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Process the /validate , /serviceValidate , and /proxyValidate URL requests.
 * <p>
 * Obtain the Service Ticket and Service information and present them to the CAS
 * validation services. Receive back an Assertion containing the user Principal
 * and (possibly) a chain of Proxy Principals. Store the Assertion in the Model
 * and chain to a View to generate the appropriate response (CAS 1, CAS 2 XML,
 * SAML, ...).
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@RefreshScope
@Component("serviceValidateController")
public abstract class AbstractServiceValidateController extends AbstractDelegateController {
    /** View if Service Ticket Validation Fails. */
    public static final String DEFAULT_SERVICE_FAILURE_VIEW_NAME = "cas2ServiceFailureView";

    /** View if Service Ticket Validation Succeeds. */
    public static final String DEFAULT_SERVICE_SUCCESS_VIEW_NAME = "cas2ServiceSuccessView";

    /** JSON View if Service Ticket Validation Succeeds and if service requires JSON. */
    public static final String DEFAULT_SERVICE_VIEW_NAME_JSON = "cas3ServiceJsonView";

    @Autowired
    private ApplicationContext context;

    
    private ValidationSpecification validationSpecification;

    
    @Autowired(required=false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    /** Implementation of Service Manager. */
    
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    /** The CORE which we will delegate all requests to. */
    
    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    /** The proxy handler we want to use with the controller. */
    
    private ProxyHandler proxyHandler;

    /** The view to redirect to on a successful validation. */
    
    private String successView = DEFAULT_SERVICE_SUCCESS_VIEW_NAME;

    /** The view to redirect to on a validation failure. */
    
    private String failureView = DEFAULT_SERVICE_FAILURE_VIEW_NAME;

    /** Extracts parameters from Request object. */
    
    @Autowired
    @Qualifier("defaultArgumentExtractor")
    private ArgumentExtractor argumentExtractor;

    
    @Autowired
    @Qualifier("defaultMultifactorTriggerSelectionStrategy")
    private MultifactorTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    @Value("${cas.mfa.authn.ctx.attribute:authnContextClass}")
    private String authenticationContextAttribute;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private AuthenticationContextValidator authenticationContextValidator;

    /**
     * Instantiates a new Service validate controller.
     */
    public AbstractServiceValidateController() {}

    /**
     * Overrideable method to determine which credentials to use to grant a
     * proxy granting ticket. Default is to use the pgtUrl.
     *
     * @param service the webapp service requesting proxy
     * @param request the HttpServletRequest object.
     * @return the credentials or null if there was an error or no credentials
     * provided.
     */
    protected Credential getServiceCredentialsFromRequest(final WebApplicationService service, final HttpServletRequest request) {
        final String pgtUrl = request.getParameter(CasProtocolConstants.PARAMETER_PROXY_CALLBACK_URL);
        if (StringUtils.hasText(pgtUrl)) {
            try {
                final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
                verifyRegisteredServiceProperties(registeredService, service);
                return new HttpBasedServiceCredential(new URL(pgtUrl), registeredService);
            } catch (final Exception e) {
                logger.error("Error constructing {}", CasProtocolConstants.PARAMETER_PROXY_CALLBACK_URL, e);
            }
        }

        return null;
    }


    /**
     * Validate authentication context pair.
     *
     * @param assertion the assertion
     * @param request   the request
     * @return the pair
     */
    protected Pair<Boolean, Optional<MultifactorAuthenticationProvider>> validateAuthenticationContext(
            final Assertion assertion, final HttpServletRequest request) {
        // find the RegisteredService for this Assertion
        logger.debug("Locating the primary authentication associated with this service request {}", assertion.getService());
        final RegisteredService service = this.servicesManager.findServiceBy(assertion.getService());
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(assertion.getService(), service);

        // resolve MFA auth context for this request
        final Map<String, MultifactorAuthenticationProvider> providers = context.getBeansOfType(MultifactorAuthenticationProvider.class);
        final Authentication authentication = assertion.getPrimaryAuthentication();
        final Optional<String> requestedContext = multifactorTriggerSelectionStrategy.resolve(providers.values(), request,
                service, authentication.getPrincipal());

        // no MFA auth context found
        if (!requestedContext.isPresent()) {
            logger.debug("No particular authentication context is required for this request");
            return new Pair<>(true, Optional.empty());
        }

        // validate the requested strategy
        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> result =
                this.authenticationContextValidator.validate(authentication, requestedContext.get(), service);
        return result;
    }

    /**
     * Inits the binder with the required fields. {@code renew} is required.
     *
     * @param request the request
     * @param binder the binder
     */
    protected void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) {
        binder.setRequiredFields(CasProtocolConstants.PARAMETER_RENEW);
    }

    /**
     * Handle proxy granting ticket delivery.
     *
     * @param serviceTicketId the service ticket id
     * @param credential the service credential
     * @return the ticket granting ticket
     */
    private TicketGrantingTicket handleProxyGrantingTicketDelivery(final String serviceTicketId, final Credential credential) 
        throws AuthenticationException, AbstractTicketException {
        final ServiceTicket serviceTicket = this.centralAuthenticationService.getTicket(serviceTicketId, ServiceTicket.class);
        final AuthenticationResult authenticationResult =
                this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(serviceTicket.getService(),
                        credential);
        final TicketGrantingTicket proxyGrantingTicketId = this.centralAuthenticationService.createProxyGrantingTicket(serviceTicketId,
                authenticationResult);
        logger.debug("Generated proxy-granting ticket [{}] off of service ticket [{}] and credential [{}]",
                proxyGrantingTicketId.getId(), serviceTicketId, credential);

        return proxyGrantingTicketId;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final WebApplicationService service = this.argumentExtractor.extractService(request);
        final String serviceTicketId = service != null ? service.getArtifactId() : null;

        if (service == null || serviceTicketId == null) {
            logger.debug("Could not identify service and/or service ticket for service: [{}]", service);
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST,
                    CasProtocolConstants.ERROR_CODE_INVALID_REQUEST, null, request, service);
        }

        try {
            return handleTicketValidation(request, service, serviceTicketId);
        } catch (final AbstractTicketValidationException e) {
            final String code = e.getCode();
            return generateErrorView(code, code,
                    new Object[] {serviceTicketId, e.getOriginalService().getId(), service.getId()}, request, service);
        } catch (final AbstractTicketException e) {
            return generateErrorView(e.getCode(), e.getCode(),
                new Object[] {serviceTicketId}, request, service);
        } catch (final UnauthorizedProxyingException e) {
            return generateErrorView(e.getMessage(), e.getMessage(), new Object[] {service.getId()}, request, service);
        } catch (final UnauthorizedServiceException e) {
            return generateErrorView(e.getMessage(), e.getMessage(), null, request, service);
        }
    }

    /**
     * Handle ticket validation model and view.
     *
     * @param request         the request
     * @param service         the service
     * @param serviceTicketId the service ticket id
     * @return the model and view
     */
    protected ModelAndView handleTicketValidation(final HttpServletRequest request, final WebApplicationService service, 
                                                  final String serviceTicketId) {
        TicketGrantingTicket proxyGrantingTicketId = null;
        final Credential serviceCredential = getServiceCredentialsFromRequest(service, request);
        if (serviceCredential != null) {
            try {
                proxyGrantingTicketId = handleProxyGrantingTicketDelivery(serviceTicketId, serviceCredential);
            } catch (final AuthenticationException e) {
                logger.warn("Failed to authenticate service credential {}", serviceCredential);
                return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                        CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                        new Object[] {serviceCredential.getId()}, request, service);
            } catch (final InvalidTicketException e) {
                logger.error("Failed to create proxy granting ticket due to an invalid ticket for {}", serviceCredential, e);
                return generateErrorView(e.getCode(), e.getCode(),
                        new Object[]{serviceTicketId}, request, service);
            } catch (final AbstractTicketException e) {
                logger.error("Failed to create proxy granting ticket for {}", serviceCredential, e);
                return generateErrorView(e.getCode(), e.getCode(),
                        new Object[]{serviceCredential.getId()}, request, service);
            }
        }

        final Assertion assertion = this.centralAuthenticationService.validateServiceTicket(serviceTicketId, service);
        if (!validateAssertion(request, serviceTicketId, assertion)) {
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_TICKET,
                    CasProtocolConstants.ERROR_CODE_INVALID_TICKET,
                    new Object[] {serviceTicketId}, request, service);
        }

        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> ctxResult = validateAuthenticationContext(assertion, request);
        if (!ctxResult.getFirst()) {
            throw new UnsatisfiedAuthenticationContextTicketValidationException(assertion.getService());
        }

        String proxyIou = null;
        if (serviceCredential != null && this.proxyHandler.canHandle(serviceCredential)) {
            proxyIou = handleProxyIouDelivery(serviceCredential, proxyGrantingTicketId);
            if (StringUtils.isEmpty(proxyIou)) {
                return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                        CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                        new Object[]{serviceCredential.getId()}, request, service);
            }
        } else {
            logger.debug("No service credentials specified, and/or the proxy handler [{}] cannot handle credentials",
                    this.proxyHandler);
        }

        onSuccessfulValidation(serviceTicketId, assertion);
        logger.debug("Successfully validated service ticket {} for service [{}]", serviceTicketId, service.getId());
        return generateSuccessView(assertion, proxyIou, service, request, 
                ctxResult.getSecond(), proxyGrantingTicketId);
    }

    private String handleProxyIouDelivery(final Credential serviceCredential, 
                                          final TicketGrantingTicket proxyGrantingTicketId) {
        return this.proxyHandler.handle(serviceCredential, proxyGrantingTicketId);
    }
    
    /**
     * Validate assertion.
     *
     * @param request the request
     * @param serviceTicketId the service ticket id
     * @param assertion the assertion
     * @return the boolean
     */
    private boolean validateAssertion(final HttpServletRequest request, final String serviceTicketId, final Assertion assertion) {

        final ServletRequestDataBinder binder = new ServletRequestDataBinder(validationSpecification, "validationSpecification");
        initBinder(request, binder);
        binder.bind(request);

        if (!validationSpecification.isSatisfiedBy(assertion, request)) {
            logger.warn("Service ticket [{}] does not satisfy validation specification.", serviceTicketId);
            return false;
        }
        return true;
    }

    /**
     * Triggered on successful validation events. Extensions are to
     * use this as hook to plug in behvior.
     *
     * @param serviceTicketId the service ticket id
     * @param assertion the assertion
     */
    protected void onSuccessfulValidation(final String serviceTicketId, final Assertion assertion) {
        // template method with nothing to do.
    }

    /**
     * Generate error view, set to {@link #setFailureView(String)}.
     *
     * @param code the code
     * @param description the description
     * @param args the args
     * @param request the request
     * @return the model and view
     */
    private ModelAndView generateErrorView(final String code, final String description,
                                           final Object[] args,
                                           final HttpServletRequest request,
                                           final WebApplicationService service) {

        final ModelAndView modelAndView = getModelAndView(request, false, service);
        final String convertedDescription = this.context.getMessage(description, args,
            description, request.getLocale());
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_CODE, code);
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_DESCRIPTION, convertedDescription);

        return modelAndView;
    }

    private ModelAndView getModelAndView(final HttpServletRequest request, 
                                         final boolean isSuccess, final WebApplicationService service) {

        ValidationResponseType type = service != null ? service.getFormat() : ValidationResponseType.XML;
        final String format = request.getParameter(CasProtocolConstants.PARAMETER_FORMAT);
        if (!StringUtils.isEmpty(format)) {
            try {
                type = ValidationResponseType.valueOf(format.toUpperCase());
            } catch (final Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        
        if (type == ValidationResponseType.JSON) {
            return new ModelAndView(DEFAULT_SERVICE_VIEW_NAME_JSON);
        }
        
        return new ModelAndView(isSuccess ? this.successView : this.failureView);
    }

    /**
     * Generate the success view. The result will contain the assertion and the proxy iou.
     *
     * @param assertion           the assertion
     * @param proxyIou            the proxy iou
     * @param service             the validated service
     * @param contextProvider     the context provider
     * @param proxyGrantingTicket the proxy granting ticket
     * @return the model and view, pointed to the view name set by
     */
    private ModelAndView generateSuccessView(final Assertion assertion, final String proxyIou,
                                             final WebApplicationService service,
                                             final HttpServletRequest request,
                                             final Optional<MultifactorAuthenticationProvider> contextProvider,
                                             final TicketGrantingTicket proxyGrantingTicket) {

        final ModelAndView modelAndView = getModelAndView(request, true, service);

        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, assertion);
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, service);
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU, proxyIou);
        if (proxyGrantingTicket != null) {
            modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, proxyGrantingTicket.getId());
        }

        if (contextProvider.isPresent()) {
            modelAndView.addObject(this.authenticationContextAttribute, contextProvider);
        }
        final Map<String, ?> augmentedModelObjects = augmentSuccessViewModelObjects(assertion);
        if (augmentedModelObjects != null) {
            modelAndView.addAllObjects(augmentedModelObjects);
        }
        return modelAndView;
    }

    /**
     * Augment success view model objects. Provides
     * a way for extension of this controller to dynamically
     * populate the model object with attributes
     * that describe a custom nature of the validation protocol.
     *
     * @param assertion the assertion
     * @return map of objects each keyed to a name
     */
    protected Map<String, ?> augmentSuccessViewModelObjects(final Assertion assertion) {
        return Collections.emptyMap();  
    }


    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        return true;
    }

    /**
     * @param centralAuthenticationService The centralAuthenticationService to
     * set.
     */
    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    
    public void setArgumentExtractor(final ArgumentExtractor argumentExtractor) {
        this.argumentExtractor = argumentExtractor;
    }

    public void setMultifactorTriggerSelectionStrategy(final MultifactorTriggerSelectionStrategy strategy) {
        this.multifactorTriggerSelectionStrategy = strategy;
    }

    /**
     * @param validationSpecificationClass The authenticationSpecificationClass
     * to set.
     */
    public void setValidationSpecification(final ValidationSpecification validationSpecificationClass) {
        this.validationSpecification = validationSpecificationClass;
    }

    /**
     * @param failureView The failureView to set.
     */
    public void setFailureView(final String failureView) {
        this.failureView = failureView;
    }

    /**
     * Return the failureView.
     * @return the failureView
     */
    public String getFailureView() {
        return this.failureView;
    }

    /**
     * @param successView The successView to set.
     */
    public void setSuccessView(final String successView) {
        this.successView = successView;
    }

    /**
     * Return the successView.
     * @return the successView
     */
    public String getSuccessView() {
        return this.successView;
    }
    /**
     * @param proxyHandler The proxyHandler to set.
     */
    public void setProxyHandler(final ProxyHandler proxyHandler) {
        this.proxyHandler = proxyHandler;
    }

    /**
     * Sets the services manager.
     *
     * @param servicesManager the new services manager
     */
    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     * Ensure that the service is found and enabled in the service registry.
     * @param registeredService the located entry in the registry
     * @param service authenticating service
     * @throws UnauthorizedServiceException if service is determined to be unauthorized
     */
    private void verifyRegisteredServiceProperties(final RegisteredService registeredService, final Service service) {
        if (registeredService == null) {
            final String msg = String.format("Service [%s] is not found in service registry.", service.getId());
            logger.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            final String msg = String.format("ServiceManagement: Unauthorized Service Access. "
                    + "Service [%s] is not enabled in service registry.", service.getId());
            
            logger.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
    }

    public void setAuthenticationSystemSupport(final AuthenticationSystemSupport authenticationSystemSupport) {
        this.authenticationSystemSupport = authenticationSystemSupport;
    }

    public void setApplicationContext(final ApplicationContext context) {
        this.context = context;
    }
}
