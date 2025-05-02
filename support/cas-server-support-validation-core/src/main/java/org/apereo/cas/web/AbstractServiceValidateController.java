package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.UnauthorizedProxyingException;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.events.ticket.CasServiceTicketValidationFailedEvent;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.AbstractTicketValidationException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationContextTicketValidationException;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.UnauthorizedServiceTicketValidationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
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
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractServiceValidateController extends AbstractDelegateController {
    private final ServiceValidateConfigurationContext serviceValidateConfigurationContext;

    private static void verifyRegisteredServiceProperties(final RegisteredService registeredService, final Service service) {
        if (registeredService == null) {
            val msg = String.format("Service [%s] is not found in service registry.", service.getId());
            LOGGER.warn(msg);
            throw UnauthorizedServiceException.denied(msg);
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, service)) {
            val msg = String.format("ServiceManagement: Unauthorized Service Access. "
                + "Service [%s] is not enabled in the CAS service registry.", service.getId());
            LOGGER.warn(msg);
            throw UnauthorizedServiceException.denied(msg);
        }
    }

    protected Ticket handleProxyGrantingTicketDelivery(final String serviceTicketId, final Credential credential) throws Throwable {
        val serviceTicket = serviceValidateConfigurationContext.getTicketRegistry().getTicket(serviceTicketId, ServiceTicket.class);
        val authenticationResult = serviceValidateConfigurationContext.getAuthenticationSystemSupport()
            .finalizeAuthenticationTransaction(serviceTicket.getService(), credential);
        val proxyGrantingTicket = serviceValidateConfigurationContext.getCentralAuthenticationService()
            .createProxyGrantingTicket(serviceTicketId, authenticationResult);
        LOGGER.debug("Generated proxy-granting ticket [{}] off of service ticket [{}] and credential [{}]",
            proxyGrantingTicket.getId(), serviceTicketId, credential);
        return proxyGrantingTicket;
    }

    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request,
                                              final HttpServletResponse response) throws Exception {
        val service = serviceValidateConfigurationContext.getArgumentExtractor().extractService(request);
        val serviceTicketId = Optional.ofNullable(service).map(WebApplicationService::getArtifactId).orElse(null);
        if (service == null || StringUtils.isBlank(serviceTicketId)) {
            LOGGER.warn("Could not identify service and/or service ticket for service: [{}]", service);
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST, StringUtils.EMPTY, request, service);
        }
        try {
            prepareForTicketValidation(request, service, serviceTicketId);
            return handleTicketValidation(request, response, service, serviceTicketId);
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
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST, StringUtils.EMPTY, request, service);
        }
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

    protected Credential getServiceCredentialsFromRequest(final WebApplicationService service, final HttpServletRequest request) {
        val pgtUrl = request.getParameter(CasProtocolConstants.PARAMETER_PROXY_CALLBACK_URL);
        if (StringUtils.isNotBlank(pgtUrl)) {
            try {
                val registeredService = serviceValidateConfigurationContext.getServicesManager()
                    .findServiceBy(service, CasModelRegisteredService.class);
                verifyRegisteredServiceProperties(registeredService, service);
                val credential = new HttpBasedServiceCredential(new URI(pgtUrl).toURL(), registeredService);
                val serviceTicket = serviceValidateConfigurationContext.getTicketRegistry().getTicket(service.getArtifactId(), ServiceTicket.class);
                val httpCredential = new BasicIdentifiableCredential(serviceTicket.getAuthentication().getPrincipal().getId());
                credential.setCredentialMetadata(new BasicCredentialMetadata(httpCredential));
                return credential;
            } catch (final Exception e) {
                LOGGER.error("Error constructing [{}]", CasProtocolConstants.PARAMETER_PROXY_CALLBACK_URL);
                LoggingUtils.error(LOGGER, e);
            }
        }
        return null;
    }

    protected void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) {
        if (serviceValidateConfigurationContext.getCasProperties().getSso().isRenewAuthnEnabled()) {
            binder.setRequiredFields(CasProtocolConstants.PARAMETER_RENEW);
        }
    }

    protected void prepareForTicketValidation(final HttpServletRequest request, final WebApplicationService service, final String serviceTicketId) {
    }

    protected ModelAndView handleTicketValidation(final HttpServletRequest request,
                                                  final HttpServletResponse response,
                                                  final WebApplicationService service, final String serviceTicketId) throws Throwable {
        var proxyGrantingTicket = (Ticket) null;
        val serviceCredential = getServiceCredentialsFromRequest(service, request);
        if (serviceCredential != null) {
            try {
                proxyGrantingTicket = handleProxyGrantingTicketDelivery(serviceTicketId, serviceCredential);
            } catch (final AuthenticationException e) {
                LOGGER.warn("Failed to authenticate service credential [{}]", serviceCredential);
                val description = getTicketValidationErrorDescription(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                    new Object[]{serviceCredential.getId()}, request);
                return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK, description, request, service);
            } catch (final InvalidTicketException e) {
                LOGGER.error("Failed to create proxy granting ticket due to an invalid ticket for [{}]", serviceCredential);
                LoggingUtils.error(LOGGER, e);
                val description = getTicketValidationErrorDescription(e.getCode(), new Object[]{serviceTicketId}, request);
                return generateErrorView(e.getCode(), description, request, service);
            } catch (final AbstractTicketException e) {
                LOGGER.error("Failed to create proxy granting ticket for [{}]", serviceCredential);
                LoggingUtils.error(LOGGER, e);
                val description = getTicketValidationErrorDescription(e.getCode(), new Object[]{serviceCredential.getId()}, request);
                return generateErrorView(e.getCode(), description, request, service);
            }
        }
        val assertion = validateServiceTicket(service, serviceTicketId);
        if (!validateAssertion(request, serviceTicketId, assertion, service)) {
            val description = getTicketValidationErrorDescription(CasProtocolConstants.ERROR_CODE_INVALID_TICKET, new Object[]{serviceTicketId}, request);
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_TICKET, description, request, service);
        }

        val ctxResult = serviceValidateConfigurationContext.getRequestedContextValidator()
            .validateAuthenticationContext(assertion, request, response);
        if (!ctxResult.isSuccess()) {
            throw new UnsatisfiedAuthenticationContextTicketValidationException(assertion.getService());
        }

        var proxyIou = StringUtils.EMPTY;
        val proxyHandler = serviceValidateConfigurationContext.getProxyHandler();
        if (serviceCredential != null && proxyHandler != null && proxyHandler.canHandle(serviceCredential)) {
            val registeredService = ((HttpBasedServiceCredential) serviceCredential).getService();
            val authorizedToReleaseProxyGrantingTicket = registeredService.getAttributeReleasePolicy().isAuthorizedToReleaseProxyGrantingTicket();
            if (authorizedToReleaseProxyGrantingTicket) {
                LOGGER.debug("Service [{}] is authorized to release the PGT directly, skip the proxy callback", registeredService);
            } else {
                LOGGER.debug("Service [{}] is not authorized to release the PGT directly, make a proxy callback", registeredService);
                proxyIou = handleProxyIouDelivery(serviceCredential, proxyGrantingTicket);
                if (StringUtils.isEmpty(proxyIou)) {
                    val description = getTicketValidationErrorDescription(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                        new Object[]{serviceCredential.getId()}, request);
                    return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK, description, request, service);
                }
            }
        } else {
            LOGGER.debug("No service credentials specified, and/or the proxy handler [{}] cannot handle credentials", proxyHandler);
        }
        onSuccessfulValidation(serviceTicketId, assertion);
        LOGGER.debug("Successfully validated service ticket [{}] for service [{}]", serviceTicketId, service.getId());
        return generateSuccessView(assertion, proxyIou, service, request, ctxResult.getContextId(), proxyGrantingTicket);
    }

    protected Assertion validateServiceTicket(final WebApplicationService service, final String serviceTicketId) throws Throwable {
        return serviceValidateConfigurationContext.getCentralAuthenticationService().validateServiceTicket(serviceTicketId, service);
    }

    protected void onSuccessfulValidation(final String serviceTicketId, final Assertion assertion) {
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
        for (val authorizer : authorizers) {
            try {
                authorizer.authorize(request, service, assertion);
            } catch (final Exception e) {
                throw new UnauthorizedServiceTicketValidationException(service);
            }
        }
    }

    protected Map<String, ?> augmentSuccessViewModelObjects(final Assertion assertion) {
        return new HashMap<>();
    }

    private String handleProxyIouDelivery(final Credential serviceCredential, final Ticket proxyGrantingTicket) throws Throwable {
        return serviceValidateConfigurationContext.getProxyHandler().handle(serviceCredential, proxyGrantingTicket);
    }

    private boolean validateAssertion(final HttpServletRequest request, final String serviceTicketId,
                                      final Assertion assertion, final Service service) {
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

    private ModelAndView generateErrorView(final String code,
                                           final String description,
                                           final HttpServletRequest request,
                                           final WebApplicationService service) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val event = new CasServiceTicketValidationFailedEvent(this, code, description, service, clientInfo);
        getServiceValidateConfigurationContext().getApplicationContext().publishEvent(event);

        val modelAndView = serviceValidateConfigurationContext.getValidationViewFactory()
            .getModelAndView(request, false, service, getClass());
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_CODE, StringEscapeUtils.escapeHtml4(code));
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_DESCRIPTION, StringEscapeUtils.escapeHtml4(description));
        return modelAndView;
    }

    private String getTicketValidationErrorDescription(final String code, final Object[] args, final HttpServletRequest request) {
        return applicationContext.getMessage(code, args, code, request.getLocale());
    }

    private ModelAndView generateSuccessView(final Assertion assertion,
                                             final String proxyIou,
                                             final WebApplicationService service,
                                             final HttpServletRequest request,
                                             final Optional<String> multifactorProvider,
                                             final Ticket proxyGrantingTicket) {
        val modelAndView = serviceValidateConfigurationContext.getValidationViewFactory().getModelAndView(request, true, service, getClass());
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, assertion);
        modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, service);
        if (StringUtils.isNotBlank(proxyIou)) {
            modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU, proxyIou);
        }
        if (proxyGrantingTicket != null) {
            modelAndView.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, proxyGrantingTicket);
        }
        multifactorProvider.ifPresent(provider -> {
            val authenticationContextAttribute = serviceValidateConfigurationContext.getCasProperties().getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
            org.springframework.util.StringUtils.commaDelimitedListToSet(authenticationContextAttribute)
                .forEach(attr -> modelAndView.addObject(attr, provider));
        });
        val augmentedModelObjects = augmentSuccessViewModelObjects(assertion);
        modelAndView.addAllObjects(augmentedModelObjects);
        return modelAndView;
    }
}
