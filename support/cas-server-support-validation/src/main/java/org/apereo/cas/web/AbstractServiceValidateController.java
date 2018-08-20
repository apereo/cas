package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HttpBasedServiceCredential;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedProxyingException;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.AbstractTicketValidationException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationContextTicketValidationException;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.validation.UnauthorizedServiceTicketValidationException;
import org.apereo.cas.validation.ValidationResponseType;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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
@Slf4j
@Setter
@AllArgsConstructor
public abstract class AbstractServiceValidateController extends AbstractDelegateController {

    private Set<CasProtocolValidationSpecification> validationSpecifications = new LinkedHashSet<>();

    private final ServiceTicketValidationAuthorizersExecutionPlan validationAuthorizers;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ServicesManager servicesManager;
    private final CentralAuthenticationService centralAuthenticationService;

    private ProxyHandler proxyHandler;
    private final View successView;
    private final View failureView;

    private final ArgumentExtractor argumentExtractor;
    private final MultifactorTriggerSelectionStrategy multifactorTriggerSelectionStrategy;
    private final AuthenticationContextValidator authenticationContextValidator;
    private final View jsonView;
    private final String authnContextAttribute;

    private boolean renewEnabled = true;

    /**
     * Ensure that the service is found and enabled in the service registry.
     *
     * @param registeredService the located entry in the registry
     * @param service           authenticating service
     * @throws UnauthorizedServiceException if service is determined to be unauthorized
     */
    private static void verifyRegisteredServiceProperties(final RegisteredService registeredService, final Service service) {
        if (registeredService == null) {
            val msg = String.format("Service [%s] is not found in service registry.", service.getId());
            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            val msg = String.format("ServiceManagement: Unauthorized Service Access. " + "Service [%s] is not enabled in service registry.", service.getId());
            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
    }

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
        val pgtUrl = request.getParameter(CasProtocolConstants.PARAMETER_PROXY_CALLBACK_URL);
        if (StringUtils.isNotBlank(pgtUrl)) {
            try {
                val registeredService = this.servicesManager.findServiceBy(service);
                verifyRegisteredServiceProperties(registeredService, service);
                return new HttpBasedServiceCredential(new URL(pgtUrl), registeredService);
            } catch (final Exception e) {
                LOGGER.error("Error constructing [{}]", CasProtocolConstants.PARAMETER_PROXY_CALLBACK_URL, e);
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
    protected Pair<Boolean, Optional<MultifactorAuthenticationProvider>> validateAuthenticationContext(final Assertion assertion, final HttpServletRequest request) {
        LOGGER.debug("Locating the primary authentication associated with this service request [{}]", assertion.getService());
        val service = this.servicesManager.findServiceBy(assertion.getService());
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(assertion.getService(), service);
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        val authentication = assertion.getPrimaryAuthentication();
        val requestedContext = this.multifactorTriggerSelectionStrategy.resolve(providers.values(), request, service, authentication);

        if (!requestedContext.isPresent()) {
            LOGGER.debug("No particular authentication context is required for this request");
            return Pair.of(Boolean.TRUE, Optional.empty());
        }
        return this.authenticationContextValidator.validate(authentication, requestedContext.get(), service);
    }

    /**
     * Initialize the binder with the required fields.
     *
     * @param request the request
     * @param binder  the binder
     */
    protected void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) {
        if (this.renewEnabled) {
            binder.setRequiredFields(CasProtocolConstants.PARAMETER_RENEW);
        }
    }

    /**
     * Handle proxy granting ticket delivery.
     *
     * @param serviceTicketId the service ticket id
     * @param credential      the service credential
     * @return the ticket granting ticket
     * @throws AuthenticationException the authentication exception
     * @throws AbstractTicketException the abstract ticket exception
     */
    public TicketGrantingTicket handleProxyGrantingTicketDelivery(final String serviceTicketId, final Credential credential)
        throws AuthenticationException, AbstractTicketException {
        val serviceTicket = this.centralAuthenticationService.getTicket(serviceTicketId, ServiceTicket.class);
        val authenticationResult = this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(serviceTicket.getService(), credential);
        val proxyGrantingTicketId = this.centralAuthenticationService.createProxyGrantingTicket(serviceTicketId, authenticationResult);
        LOGGER.debug("Generated proxy-granting ticket [{}] off of service ticket [{}] and credential [{}]", proxyGrantingTicketId.getId(), serviceTicketId, credential);
        return proxyGrantingTicketId;
    }

    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val service = this.argumentExtractor.extractService(request);
        val serviceTicketId = service != null ? service.getArtifactId() : null;
        if (service == null || StringUtils.isBlank(serviceTicketId)) {
            LOGGER.debug("Could not identify service and/or service ticket for service: [{}]", service);
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST, null, request, service);
        }
        try {
            prepareForTicketValidation(request, service, serviceTicketId);
            return handleTicketValidation(request, service, serviceTicketId);
        } catch (final AbstractTicketValidationException e) {
            val code = e.getCode();
            return generateErrorView(code, new Object[]{serviceTicketId, e.getService().getId(), service.getId()}, request, service);
        } catch (final AbstractTicketException e) {
            return generateErrorView(e.getCode(), new Object[]{serviceTicketId}, request, service);
        } catch (final UnauthorizedProxyingException e) {
            return generateErrorView(CasProtocolConstants.ERROR_CODE_UNAUTHORIZED_SERVICE_PROXY, new Object[]{service.getId()}, request, service);
        } catch (final UnauthorizedServiceException | PrincipalException e) {
            return generateErrorView(CasProtocolConstants.ERROR_CODE_UNAUTHORIZED_SERVICE, null, request, service);
        }
    }

    /**
     * Prepare for ticket validation.
     *
     * @param request         the request
     * @param service         the service
     * @param serviceTicketId the service ticket id
     */
    protected void prepareForTicketValidation(final HttpServletRequest request, final WebApplicationService service, final String serviceTicketId) {
    }

    /**
     * Handle ticket validation model and view.
     *
     * @param request         the request
     * @param service         the service
     * @param serviceTicketId the service ticket id
     * @return the model and view
     */

    protected ModelAndView handleTicketValidation(final HttpServletRequest request, final WebApplicationService service, final String serviceTicketId) {
        var proxyGrantingTicketId = (TicketGrantingTicket) null;
        val serviceCredential = getServiceCredentialsFromRequest(service, request);
        if (serviceCredential != null) {
            try {
                proxyGrantingTicketId = handleProxyGrantingTicketDelivery(serviceTicketId, serviceCredential);
            } catch (final AuthenticationException e) {
                LOGGER.warn("Failed to authenticate service credential [{}]", serviceCredential);
                return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK, new Object[]{serviceCredential.getId()}, request, service);
            } catch (final InvalidTicketException e) {
                LOGGER.error("Failed to create proxy granting ticket due to an invalid ticket for [{}]", serviceCredential, e);
                return generateErrorView(e.getCode(), new Object[]{serviceTicketId}, request, service);
            } catch (final AbstractTicketException e) {
                LOGGER.error("Failed to create proxy granting ticket for [{}]", serviceCredential, e);
                return generateErrorView(e.getCode(), new Object[]{serviceCredential.getId()}, request, service);
            }
        }
        val assertion = validateServiceTicket(service, serviceTicketId);
        if (!validateAssertion(request, serviceTicketId, assertion, service)) {
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_TICKET, new Object[]{serviceTicketId}, request, service);
        }
        val ctxResult = validateAuthenticationContext(assertion, request);
        if (!ctxResult.getKey()) {
            throw new UnsatisfiedAuthenticationContextTicketValidationException(assertion.getService());
        }
        var proxyIou = StringUtils.EMPTY;
        if (serviceCredential != null && this.proxyHandler != null && this.proxyHandler.canHandle(serviceCredential)) {
            proxyIou = handleProxyIouDelivery(serviceCredential, proxyGrantingTicketId);
            if (StringUtils.isEmpty(proxyIou)) {
                return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK, new Object[]{serviceCredential.getId()}, request, service);
            }
        } else {
            LOGGER.debug("No service credentials specified, and/or the proxy handler [{}] cannot handle credentials", this.proxyHandler);
        }
        onSuccessfulValidation(serviceTicketId, assertion);
        LOGGER.debug("Successfully validated service ticket [{}] for service [{}]", serviceTicketId, service.getId());
        return generateSuccessView(assertion, proxyIou, service, request, ctxResult.getValue(), proxyGrantingTicketId);
    }

    /**
     * Validate service ticket assertion.
     *
     * @param service         the service
     * @param serviceTicketId the service ticket id
     * @return the assertion
     */
    protected Assertion validateServiceTicket(final WebApplicationService service, final String serviceTicketId) {
        return this.centralAuthenticationService.validateServiceTicket(serviceTicketId, service);
    }

    private String handleProxyIouDelivery(final Credential serviceCredential, final TicketGrantingTicket proxyGrantingTicketId) {
        return this.proxyHandler.handle(serviceCredential, proxyGrantingTicketId);
    }

    /**
     * Validate assertion.
     *
     * @param request         the request
     * @param serviceTicketId the service ticket id
     * @param assertion       the assertion
     * @param service         the service
     * @return true/false
     */
    private boolean validateAssertion(final HttpServletRequest request, final String serviceTicketId, final Assertion assertion, final Service service) {
        for (val s : this.validationSpecifications) {
            s.reset();
            val binder = new ServletRequestDataBinder(s, "validationSpecification");
            initBinder(request, binder);
            binder.bind(request);
            if (!s.isSatisfiedBy(assertion, request)) {
                LOGGER.warn("Service ticket [{}] does not satisfy validation specification.", serviceTicketId);
                return false;
            }
        }
        enforceTicketValidationAuthorizationFor(request, service, assertion);
        return true;
    }

    /**
     * Triggered on successful validation events. Extensions are to
     * use this as hook to plug in behavior.
     *
     * @param serviceTicketId the service ticket id
     * @param assertion       the assertion
     */
    protected void onSuccessfulValidation(final String serviceTicketId, final Assertion assertion) {
        // template method with nothing to do.
    }

    /**
     * Generate error view.
     *
     * @param code    the code
     * @param args    the args
     * @param request the request
     * @return the model and view
     */
    private ModelAndView generateErrorView(final String code, final Object[] args, final HttpServletRequest request, final WebApplicationService service) {
        val modelAndView = getModelAndView(request, false, service);
        val convertedDescription = this.applicationContext.getMessage(code, args, code, request.getLocale());
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_CODE, StringEscapeUtils.escapeHtml4(code));
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_DESCRIPTION, StringEscapeUtils.escapeHtml4(convertedDescription));
        return modelAndView;
    }

    private ModelAndView getModelAndView(final HttpServletRequest request, final boolean isSuccess, final WebApplicationService service) {
        val type = getValidationResponseType(request, service);
        if (type == ValidationResponseType.JSON) {
            return new ModelAndView(this.jsonView);
        }
        return new ModelAndView(isSuccess ? this.successView : this.failureView);
    }

    private ValidationResponseType getValidationResponseType(final HttpServletRequest request, final WebApplicationService service) {
        val format = request.getParameter(CasProtocolConstants.PARAMETER_FORMAT);
        final Function<String, ValidationResponseType> func = FunctionUtils.doIf(StringUtils::isNotBlank,
            t -> ValidationResponseType.valueOf(t.toUpperCase()),
            f -> service != null ? service.getFormat() : ValidationResponseType.XML);
        return func.apply(format);
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
                                             final WebApplicationService service, final HttpServletRequest request,
                                             final Optional<MultifactorAuthenticationProvider> contextProvider,
                                             final TicketGrantingTicket proxyGrantingTicket) {
        val modelAndView = getModelAndView(request, true, service);
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, assertion);
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, service);
        if (StringUtils.isNotBlank(proxyIou)) {
            modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU, proxyIou);
        }
        if (proxyGrantingTicket != null) {
            modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, proxyGrantingTicket.getId());
        }
        contextProvider.ifPresent(provider -> modelAndView.addObject(this.authnContextAttribute, provider.getId()));
        val augmentedModelObjects = augmentSuccessViewModelObjects(assertion);
        if (augmentedModelObjects != null) {
            modelAndView.addAllObjects(augmentedModelObjects);
        }
        return modelAndView;
    }

    /**
     * Enforce ticket validation authorization for.
     *
     * @param request   the request
     * @param service   the service
     * @param assertion the assertion
     */
    protected void enforceTicketValidationAuthorizationFor(final HttpServletRequest request, final Service service, final Assertion assertion) {
        val authorizers = this.validationAuthorizers.getAuthorizers();
        for (val a : authorizers) {
            try {
                a.authorize(request, service, assertion);
            } catch (final Exception e) {
                throw new UnauthorizedServiceTicketValidationException(service);
            }
        }
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
        return new HashMap<>(0);
    }

    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        return true;
    }

    public View getSuccessView() {
        return successView;
    }

    public View getFailureView() {
        return failureView;
    }

    /**
     * Add validation specification.
     *
     * @param validationSpecification the validation specification
     */
    public void addValidationSpecification(final CasProtocolValidationSpecification validationSpecification) {
        this.validationSpecifications.add(validationSpecification);
    }
}
