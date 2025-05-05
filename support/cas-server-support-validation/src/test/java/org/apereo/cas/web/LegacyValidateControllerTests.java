package org.apereo.cas.web;

import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.DefaultServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.v1.LegacyValidateController;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

/**
 * This is {@link LegacyValidateControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ImportAutoConfiguration({
    CasThymeleafAutoConfiguration.class,
    CasValidationAutoConfiguration.class
})
@Tag("CAS")
@Getter
@ExtendWith(CasTestExtension.class)
class LegacyValidateControllerTests extends AbstractServiceValidateControllerTests {
    @Autowired
    @Qualifier(ServiceValidationViewFactory.BEAN_NAME)
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
