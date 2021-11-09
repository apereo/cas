package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseCasSamlSPConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    BaseSamlIdPConfigurationTests.SharedTestConfiguration.class,
    CasSamlServiceProvidersConfiguration.class
}, properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.core.require-valid-metadata=false",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/sp-idp-metadata"
})
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseCasSamlSPConfigurationTests {

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @AfterEach
    public void afterEach() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyOperation() {
        LOGGER.debug("Looking for service id [{}]", getServiceProviderId());
        val service = webApplicationServiceFactory.createService(getServiceProviderId());
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, List.of(getServiceProviderId()));
        
        assertNotNull(servicesManager.findServiceBy(service, SamlRegisteredService.class));
    }

    protected String getServiceProviderId() {
        return "https://example.org/shibboleth";
    }
}
