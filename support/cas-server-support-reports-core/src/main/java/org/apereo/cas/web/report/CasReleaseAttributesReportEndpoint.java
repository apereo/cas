package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.CasViewConstants;
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
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import org.apereo.cas.validation.ImmutableAssertion;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link CasReleaseAttributesReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Endpoint(id = "releaseAttributes", defaultAccess = Access.NONE)
public class CasReleaseAttributesReportEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<@NonNull ServicesManager> servicesManager;

    private final ObjectProvider<@NonNull AuthenticationSystemSupport> authenticationSystemSupport;

    private final ObjectProvider<@NonNull ServiceFactory<WebApplicationService>> serviceFactory;

    private final ObjectProvider<@NonNull PrincipalFactory> principalFactory;

    private final ObjectProvider<@NonNull PrincipalResolver> principalResolver;

    private final ConfigurableApplicationContext applicationContext;

    public CasReleaseAttributesReportEndpoint(final CasConfigurationProperties casProperties,
                                              final ConfigurableApplicationContext applicationContext,
                                              final ObjectProvider<@NonNull ServicesManager> servicesManager,
                                              final ObjectProvider<@NonNull AuthenticationSystemSupport> authenticationSystemSupport,
                                              final ObjectProvider<@NonNull ServiceFactory<WebApplicationService>> serviceFactory,
                                              final ObjectProvider<@NonNull PrincipalFactory> principalFactory,
                                              final ObjectProvider<@NonNull PrincipalResolver> principalResolver) {
        super(casProperties);
        this.applicationContext = applicationContext;
        this.servicesManager = servicesManager;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.serviceFactory = serviceFactory;
        this.principalFactory = principalFactory;
        this.principalResolver = principalResolver;
    }

    protected Map<String, Object> releasePrincipalAttributes(
        final String username,
        @Nullable final String password,
        final String service) throws Throwable {

        val selectedService = serviceFactory.getObject().createService(service);
        val registeredService = NumberUtils.isCreatable(service)
            ? servicesManager.getObject().findServiceBy(Long.parseLong(service))
            : servicesManager.getObject().findServiceBy(selectedService);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(selectedService, registeredService);

        val authentication = buildAuthentication(username, password, selectedService);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(selectedService)
            .principal(authentication.getPrincipal())
            .build();

        val attributesToRelease = registeredService.getAttributeReleasePolicy().getAttributes(context);
        val builder = DefaultAuthenticationBuilder.of(
            applicationContext,
            authentication.getPrincipal(),
            principalFactory.getObject(),
            attributesToRelease,
            selectedService,
            registeredService,
            authentication);

        val finalAuthentication = builder.build();
        val assertion = DefaultAssertionBuilder
            .builder()
            .primaryAuthentication(finalAuthentication)
            .service(selectedService)
            .authentications(CollectionUtils.wrap(finalAuthentication))
            .registeredService(registeredService)
            .build()
            .assemble();

        val resValidation = new LinkedHashMap<String, Object>();
        resValidation.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, assertion);
        resValidation.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, selectedService);
        resValidation.put("registeredService", registeredService);

        return resValidation;
    }

    private Authentication buildAuthentication(final String username, final String password,
                                               final WebApplicationService selectedService) throws Throwable {
        if (StringUtils.isNotBlank(password)) {
            val credential = new UsernamePasswordCredential(username, password);
            val result = authenticationSystemSupport.getObject().finalizeAuthenticationTransaction(selectedService, credential);
            return result.getAuthentication();
        }
        val principal = principalResolver.getObject()
            .resolve(new BasicIdentifiableCredential(username),
                Optional.of(principalFactory.getObject().createPrincipal(username)),
                Optional.empty(), Optional.of(selectedService));
        return DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
    }

    /**
     * Method that accepts a JSON body through a POST method to receive user credentials and only returns a
     * map of attributes released for the authenticated user.
     *
     * @param username - the username
     * @param password - the password; this may be optional.
     * @param service  - the service id
     * @return - the map
     * @throws Throwable the throwable
     */
    @WriteOperation
    @Operation(summary = "Get collection of released attributes for the user and application",
        parameters = {
            @Parameter(name = "username", required = true, description = "The username to authenticate"),
            @Parameter(name = "password", required = false, description = "The password to authenticate"),
            @Parameter(name = "service", required = true, description = "May be the service id or its numeric identifier")
        })
    public Map<String, Object> releaseAttributes(final String username, @Nullable final String password,
                                                 final String service) throws Throwable {
        val map = releasePrincipalAttributes(username, password, service);
        val assertion = (ImmutableAssertion) map.get("assertion");
        return Map.of("username", username, "attributes", assertion.getPrimaryAuthentication().getPrincipal().getAttributes());
    }
}
