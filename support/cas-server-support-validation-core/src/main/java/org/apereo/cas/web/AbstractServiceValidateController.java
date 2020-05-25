package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.UnauthorizedProxyingException;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.AbstractTicketValidationException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationContextTicketValidationException;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.UnauthorizedServiceTicketValidationException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.HashMap;
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
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractServiceValidateController extends AbstractDelegateController {
    private final ServiceValidateConfigurationContext serviceValidateConfigurationContext;

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
            val msg = String.format("ServiceManagement: Unauthorized Service Access. "
                + "Service [%s] is not enabled in the CAS service registry.", service.getId());
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
                val registeredService = serviceValidateConfigurationContext.getServicesManager().findServiceBy(service);
                verifyRegisteredServiceProperties(registeredService, service);
                return new HttpBasedServiceCredential(new URL(pgtUrl), registeredService);
            } catch (final Exception e) {
                LOGGER.error("Error constructing [{}]", CasProtocolConstants.PARAMETER_PROXY_CALLBACK_URL, e);
            }
        }
        return null;
    }

    /**
     * Initialize the binder with the required fields.
     *
     * @param request the request
     * @param binder  the binder
     */
    protected void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) {
        if (serviceValidateConfigurationContext.isRenewEnabled()) {
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
        val serviceTicket = serviceValidateConfigurationContext.getCentralAuthenticationService().getTicket(serviceTicketId, ServiceTicket.class);
        val authenticationResult = serviceValidateConfigurationContext.getAuthenticationSystemSupport()
            .handleAndFinalizeSingleAuthenticationTransaction(serviceTicket.getService(), credential);
        val proxyGrantingTicketId = serviceValidateConfigurationContext.getCentralAuthenticationService()
            .createProxyGrantingTicket(serviceTicketId, authenticationResult);
        LOGGER.debug("Generated proxy-granting ticket [{}] off of service ticket [{}] and credential [{}]",
            proxyGrantingTicketId.getId(), serviceTicketId, credential);
        return proxyGrantingTicketId;
    }

    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val service = serviceValidateConfigurationContext.getArgumentExtractor().extractService(request);
        val serviceTicketId = Optional.ofNullable(service).map(WebApplicationService::getArtifactId).orElse(null);
        if (service == null || StringUtils.isBlank(serviceTicketId)) {
            LOGGER.warn("Could not identify service and/or service ticket for service: [{}]", service);
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST, StringUtils.EMPTY, request, service);
        }
        try {
            prepareForTicketValidation(request, service, serviceTicketId);
            return handleTicketValidation(request, service, serviceTicketId);
        } catch (final AbstractTicketValidationException e) {
            val code = e.getCode();
            val description = getTicketValidationErrorDescription(code,
                new Object[]{serviceTicketId, e.getService().getId(), service.getId()}, request);
            return generateErrorView(code, description, request, service);
        } catch (final AbstractTicketException e) {
            val description = getTicketValidationErrorDescription(e.getCode(), new Object[]{serviceTicketId}, request);
            return generateErrorView(e.getCode(), description, request, service);
        } catch (final UnauthorizedProxyingException e) {
            val description = getTicketValidationErrorDescription(
                CasProtocolConstants.ERROR_CODE_UNAUTHORIZED_SERVICE_PROXY, new Object[]{service.getId()}, request);
            return generateErrorView(CasProtocolConstants.ERROR_CODE_UNAUTHORIZED_SERVICE_PROXY, description, request, service);
        } catch (final UnauthorizedServiceException | PrincipalException e) {
            return generateErrorView(CasProtocolConstants.ERROR_CODE_UNAUTHORIZED_SERVICE, null, request, service);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST, StringUtils.EMPTY, request, service);
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
                val description = getTicketValidationErrorDescription(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                    new Object[]{serviceCredential.getId()}, request);
                return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK, description, request, service);
            } catch (final InvalidTicketException e) {
                LOGGER.error("Failed to create proxy granting ticket due to an invalid ticket for [{}]", serviceCredential, e);
                val description = getTicketValidationErrorDescription(e.getCode(), new Object[]{serviceTicketId}, request);
                return generateErrorView(e.getCode(), description, request, service);
            } catch (final AbstractTicketException e) {
                LOGGER.error("Failed to create proxy granting ticket for [{}]", serviceCredential, e);
                val description = getTicketValidationErrorDescription(e.getCode(), new Object[]{serviceCredential.getId()}, request);
                return generateErrorView(e.getCode(), description, request, service);
            }
        }
        val assertion = validateServiceTicket(service, serviceTicketId);
        if (!validateAssertion(request, serviceTicketId, assertion, service)) {
            val description = getTicketValidationErrorDescription(CasProtocolConstants.ERROR_CODE_INVALID_TICKET, new Object[]{serviceTicketId}, request);
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_TICKET, description, request, service);
        }

        val ctxResult = serviceValidateConfigurationContext.getRequestedContextValidator().validateAuthenticationContext(assertion, request);
        if (!ctxResult.getKey()) {
            throw new UnsatisfiedAuthenticationContextTicketValidationException(assertion.getService());
        }

        var proxyIou = StringUtils.EMPTY;
        val proxyHandler = serviceValidateConfigurationContext.getProxyHandler();
        if (serviceCredential != null && proxyHandler != null && proxyHandler.canHandle(serviceCredential)) {
            val registeredService = ((HttpBasedServiceCredential) serviceCredential).getService();
            val authorizedToReleaseProxyGrantingTicket = registeredService.getAttributeReleasePolicy().isAuthorizedToReleaseProxyGrantingTicket();
            if (!authorizedToReleaseProxyGrantingTicket) {
                LOGGER.debug("The service: {} is not authorized to release the PGT directly, make a proxy callback", registeredService);
                proxyIou = handleProxyIouDelivery(serviceCredential, proxyGrantingTicketId);
                if (StringUtils.isEmpty(proxyIou)) {
                    val description = getTicketValidationErrorDescription(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                            new Object[]{serviceCredential.getId()}, request);
                    return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK, description, request, service);
                }
            } else {
                LOGGER.debug("The service: {} is authorized to release the PGT directly, skip the proxy callback", registeredService);
            }
        } else {
            LOGGER.debug("No service credentials specified, and/or the proxy handler [{}] cannot handle credentials", proxyHandler);
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
        return serviceValidateConfigurationContext.getCentralAuthenticationService().validateServiceTicket(serviceTicketId, service);
    }

    private String handleProxyIouDelivery(final Credential serviceCredential, final TicketGrantingTicket proxyGrantingTicketId) {
        return serviceValidateConfigurationContext.getProxyHandler().handle(serviceCredential, proxyGrantingTicketId);
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
        for (val spec : serviceValidateConfigurationContext.getValidationSpecifications()) {
            spec.reset();
            val binder = new ServletRequestDataBinder(spec, "validationSpecification");
            initBinder(request, binder);
            binder.bind(request);
            if (!spec.isSatisfiedBy(assertion, request)) {
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
    }

    /**
     * Generate error view.
     *
     * @param code        the code
     * @param description the description
     * @param request     the request
     * @param service     the service
     * @return the model and view
     */
    private ModelAndView generateErrorView(final String code,
                                           final String description,
                                           final HttpServletRequest request,
                                           final WebApplicationService service) {
        val modelAndView = serviceValidateConfigurationContext.getValidationViewFactory()
            .getModelAndView(request, false, service, getClass());
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_CODE, StringEscapeUtils.escapeHtml4(code));
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_DESCRIPTION, StringEscapeUtils.escapeHtml4(description));
        return modelAndView;
    }

    private String getTicketValidationErrorDescription(final String code, final Object[] args, final HttpServletRequest request) {
        return this.applicationContext.getMessage(code, args, code, request.getLocale());
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
        val modelAndView = serviceValidateConfigurationContext.getValidationViewFactory().getModelAndView(request, true, service, getClass());
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, assertion);
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, service);
        if (StringUtils.isNotBlank(proxyIou)) {
            modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU, proxyIou);
        }
        if (proxyGrantingTicket != null) {
            modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, proxyGrantingTicket.getId());
        }
        contextProvider.ifPresent(provider -> modelAndView.addObject(serviceValidateConfigurationContext.getAuthnContextAttribute(), provider.getId()));
        val augmentedModelObjects = augmentSuccessViewModelObjects(assertion);
        modelAndView.addAllObjects(augmentedModelObjects);
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
        val authorizers = serviceValidateConfigurationContext.getValidationAuthorizers().getAuthorizers();
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

    /**
     * Add validation specification.
     *
     * @param validationSpecification the validation specification
     */
    public void addValidationSpecification(final CasProtocolValidationSpecification validationSpecification) {
        serviceValidateConfigurationContext.getValidationSpecifications().add(validationSpecification);
    }
}
