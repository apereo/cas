package org.apereo.cas.web;

import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.DefaultServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.v1.LegacyValidateController;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link LegacyValidateControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Import(CasPersonDirectoryTestConfiguration.class)
@ImportAutoConfiguration({
    CasThymeleafAutoConfiguration.class,
    CasValidationAutoConfiguration.class
})
@Tag("CAS")
@Getter
class LegacyValidateControllerTests extends AbstractServiceValidateControllerTests {
    @Autowired
    @Qualifier("serviceValidationViewFactory")
    private ServiceValidationViewFactory serviceValidationViewFactory;

    @Override
    public AbstractServiceValidateController getServiceValidateControllerInstance() {
        val context = ServiceValidateConfigurationContext.builder()
            .applicationContext(applicationContext)
            .casProperties(casProperties)
            .principalFactory(getPrincipalFactory())
            .principalResolver(getDefaultPrincipalResolver())
            .ticketRegistry(getTicketRegistry())
            .validationSpecifications(CollectionUtils.wrapSet(getValidationSpecification()))
            .authenticationSystemSupport(getAuthenticationSystemSupport())
            .servicesManager(getServicesManager())
            .centralAuthenticationService(getCentralAuthenticationService())
            .argumentExtractor(getArgumentExtractor())
            .proxyHandler(getProxyHandler())
            .requestedContextValidator(new MockRequestedAuthenticationContextValidator())
            .validationAuthorizers(new DefaultServiceTicketValidationAuthorizersExecutionPlan())
            .validationViewFactory(serviceValidationViewFactory)
            .serviceFactory(getWebApplicationServiceFactory())
            .build();
        return new LegacyValidateController(context);
    }
}
