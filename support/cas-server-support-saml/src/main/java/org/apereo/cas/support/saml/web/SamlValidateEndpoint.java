package org.apereo.cas.support.saml.web;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.RegisteredServiceUsernameProviderContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.authentication.SamlResponseBuilder;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SamlValidateEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Endpoint(id = "samlValidate", defaultAccess = Access.NONE)
public class SamlValidateEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<@NonNull ServicesManager> servicesManager;

    private final ObjectProvider<@NonNull AuthenticationSystemSupport> authenticationSystemSupport;

    private final ObjectProvider<@NonNull ServiceFactory<WebApplicationService>> serviceFactory;

    private final ObjectProvider<@NonNull PrincipalFactory> principalFactory;

    private final ObjectProvider<@NonNull SamlResponseBuilder> samlResponseBuilder;

    private final ObjectProvider<@NonNull OpenSamlConfigBean> openSamlConfigBean;

    private final ObjectProvider<@NonNull AuditableExecution> registeredServiceAccessStrategyEnforcer;

    private final ObjectProvider<@NonNull PrincipalResolver> principalResolver;

    public SamlValidateEndpoint(final CasConfigurationProperties casProperties,
                                final ConfigurableApplicationContext applicationContext,
                                final ObjectProvider<@NonNull ServicesManager> servicesManager,
                                final ObjectProvider<@NonNull AuthenticationSystemSupport> authenticationSystemSupport,
                                final ObjectProvider<@NonNull ServiceFactory<WebApplicationService>> serviceFactory,
                                final ObjectProvider<@NonNull PrincipalFactory> principalFactory,
                                final ObjectProvider<@NonNull SamlResponseBuilder> samlResponseBuilder,
                                final ObjectProvider<@NonNull OpenSamlConfigBean> openSamlConfigBean,
                                final ObjectProvider<@NonNull AuditableExecution> registeredServiceAccessStrategyEnforcer,
                                final ObjectProvider<@NonNull PrincipalResolver> principalResolver) {
        super(casProperties, applicationContext);
        this.servicesManager = servicesManager;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.serviceFactory = serviceFactory;
        this.principalFactory = principalFactory;
        this.samlResponseBuilder = samlResponseBuilder;
        this.openSamlConfigBean = openSamlConfigBean;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
        this.principalResolver = principalResolver;
    }

    /**
     * Handle validation request and produce saml1 payload.
     *
     * @param request  the request
     * @param username the username
     * @param password the password
     * @param service  the service
     * @return the map
     * @throws Throwable the throwable
     */
    @PostMapping(produces = {
        MediaType.TEXT_XML_VALUE,
        MediaType.APPLICATION_XML_VALUE,
        MediaType.APPLICATION_JSON_VALUE
    }, consumes = {
        MediaType.TEXT_XML_VALUE,
        MediaType.APPLICATION_XML_VALUE,
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE
    })
    @Operation(summary = "Handle validation request and produce saml1 payload.", parameters = {
        @Parameter(name = "username", required = true, description = "The username"),
        @Parameter(name = "password", required = false, description = "The password"),
        @Parameter(name = "service", required = true, description = "The service")
    })
    public ResponseEntity handle(
        final HttpServletRequest request,
        final String username,
        @RequestParam(required = false) final String password,
        final String service) throws Throwable {
        val selectedService = serviceFactory.getObject().createService(service);
        val authentication = buildAuthentication(username, password, selectedService);

        val registeredService = servicesManager.getObject().findServiceBy(selectedService);
        val audit = AuditableContext.builder()
            .service(selectedService)
            .authentication(authentication)
            .registeredService(registeredService)
            .build();
        val accessResult = registeredServiceAccessStrategyEnforcer.getObject().execute(audit);
        accessResult.throwExceptionIfNeeded();

        val principal = authentication.getPrincipal();

        val context = RegisteredServiceAttributeReleasePolicyContext
            .builder()
            .registeredService(registeredService)
            .service(selectedService)
            .principal(principal)
            .applicationContext(openSamlConfigBean.getObject().getApplicationContext())
            .build();
        val attributesToRelease = registeredService.getAttributeReleasePolicy().getAttributes(context);

        val usernameContext = RegisteredServiceUsernameProviderContext
            .builder()
            .registeredService(registeredService)
            .service(selectedService)
            .principal(principal)
            .applicationContext(openSamlConfigBean.getObject().getApplicationContext())
            .build();
        val principalId = registeredService.getUsernameAttributeProvider().resolveUsername(usernameContext);

        val modifiedPrincipal = principalFactory.getObject().createPrincipal(principalId, attributesToRelease);
        val builder = DefaultAuthenticationBuilder.newInstance(authentication);
        builder.setPrincipal(modifiedPrincipal);
        val finalAuthentication = builder.build();

        val samlResponse = samlResponseBuilder.getObject().createResponse(selectedService.getId(), selectedService);
        samlResponseBuilder.getObject().prepareSuccessfulResponse(Map.of(), samlResponse, selectedService, finalAuthentication, principal,
            finalAuthentication.getAttributes(), principal.getAttributes());

        val encoded = SamlUtils.transformSamlObject(openSamlConfigBean.getObject(), samlResponse).toString();

        val contentType = StringUtils.defaultIfBlank(request.getHeader(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON_VALUE);
        if (MediaType.APPLICATION_XML_VALUE.equals(contentType) || MediaType.TEXT_XML_VALUE.equals(contentType)) {
            val headers = new HttpHeaders();
            headers.put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.TEXT_XML_VALUE));
            return new ResponseEntity<>(encoded, headers, HttpStatus.OK);
        }

        val resValidation = new LinkedHashMap<String, Object>();
        resValidation.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, encoded);
        resValidation.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, selectedService);
        resValidation.put("registeredService", registeredService);
        val headers = new HttpHeaders();
        headers.put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(resValidation, headers, HttpStatus.OK);
    }

    private Authentication buildAuthentication(final String username, final String password,
                                               final WebApplicationService selectedService) throws Throwable {
        if (StringUtils.isNotBlank(password)) {
            val credential = new UsernamePasswordCredential(username, password);
            val result = authenticationSystemSupport.getObject().finalizeAuthenticationTransaction(selectedService, credential);
            return result.getAuthentication();
        }
        val principal = principalResolver.getObject().resolve(new BasicIdentifiableCredential(username),
            Optional.of(principalFactory.getObject().createPrincipal(username)),
            Optional.empty(), Optional.of(selectedService));
        return DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
    }
}
