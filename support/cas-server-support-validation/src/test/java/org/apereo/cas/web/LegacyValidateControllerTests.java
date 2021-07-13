package org.apereo.cas.web;

import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.config.CasThymeleafConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.DefaultServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.v1.LegacyValidateController;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link LegacyValidateControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    BaseCasCoreTests.SharedTestConfiguration.class,
    CasThemesConfiguration.class,
    CasThymeleafConfiguration.class,
    CasValidationConfiguration.class
})
@Tag("CAS")
public class LegacyValidateControllerTests extends AbstractServiceValidateControllerTests {
    @Autowired
    @Qualifier("serviceValidationViewFactory")
    private ServiceValidationViewFactory serviceValidationViewFactory;

    @Override
    public AbstractServiceValidateController getServiceValidateControllerInstance() {
        val context = ServiceValidateConfigurationContext.builder()
            .validationSpecifications(CollectionUtils.wrapSet(getValidationSpecification()))
            .authenticationSystemSupport(getAuthenticationSystemSupport())
            .servicesManager(getServicesManager())
            .centralAuthenticationService(getCentralAuthenticationService())
            .argumentExtractor(getArgumentExtractor())
            .proxyHandler(getProxyHandler())
            .requestedContextValidator(new MockRequestedAuthenticationContextValidator())
            .authnContextAttribute("authenticationContext")
            .validationAuthorizers(new DefaultServiceTicketValidationAuthorizersExecutionPlan())
            .renewEnabled(true)
            .validationViewFactory(serviceValidationViewFactory)
            .build();
        return new LegacyValidateController(context);
    }
}
