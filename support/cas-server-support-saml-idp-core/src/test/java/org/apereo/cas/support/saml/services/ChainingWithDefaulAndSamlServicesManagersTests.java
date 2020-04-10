package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.services.ChainingServicesManager;
import org.apereo.cas.services.ChainingWithOnlyDefaulServicesManagerTests;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * This is {@link ChainingWithDefaulAndSamlServicesManagersTests}.
 *
 * @author Dmitriy Kopylenko
 * @since 6.2.0
 */
public class ChainingWithDefaulAndSamlServicesManagersTests extends ChainingWithOnlyDefaulServicesManagerTests {
    @Override
    protected ServicesManager getServicesManagerInstance() {
        var chain = (ChainingServicesManager) super.getServicesManagerInstance();
        var samlSvcMgr = new SamlServicesManager(serviceRegistry, mock(ApplicationEventPublisher.class), new HashSet<>());
        chain.registerServiceManager(samlSvcMgr);
        return chain;
    }

    @Test
    public void verifyCorrectRegisteredServiceReturnedForRegularServiceWhenSamlAndRegularTypesDefinedIndependentOfEvalOrder() {
        createAndSaveServicesFixtures(r -> r.setEvaluationOrder(2), s -> s.setEvaluationOrder(1));

        SimpleWebApplicationServiceImpl service = new SimpleWebApplicationServiceImpl();
        service.setId("https://example.com");

        var lookedUpRegSvc = this.servicesManager.findServiceBy(service);
        assertEquals(RegexRegisteredService.class, lookedUpRegSvc.getClass());
    }

    @Test
    public void verifyCorrectRegisteredServiceReturnedForSamlServiceWhenSamlAndRegularTypesDefinedIndependentOfEvalOrder() {
        createAndSaveServicesFixtures(r -> r.setEvaluationOrder(1), s -> s.setEvaluationOrder(2));

        SamlService service = new SamlService();
        service.setId("https://some.saml.sp.com");

        var lookedUpRegSvc = this.servicesManager.findServiceBy(service);
        assertEquals(SamlRegisteredService.class, lookedUpRegSvc.getClass());
    }

    private void createAndSaveServicesFixtures(final Consumer<RegexRegisteredService> regexSvcCustomizer,
                                               final Consumer<SamlRegisteredService> samlSvcCustomizer) {

        var regexSvc = new RegexRegisteredService();
        regexSvc.setId(1L);
        regexSvc.setName("wildcard-regex");
        regexSvc.setServiceId("https://.*");
        regexSvcCustomizer.accept(regexSvc);

        var samlSvc = new SamlRegisteredService();
        samlSvc.setId(2L);
        samlSvc.setName("wildcard-saml");
        samlSvc.setServiceId(".+");
        samlSvcCustomizer.accept(samlSvc);

        this.servicesManager.save(samlSvc);
        this.servicesManager.save(regexSvc);
    }
}
