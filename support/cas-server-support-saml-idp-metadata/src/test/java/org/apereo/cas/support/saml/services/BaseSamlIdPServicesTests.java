package org.apereo.cas.support.saml.services;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.UrlResourceMetadataResolver;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link BaseSamlIdPServicesTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreSamlAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseSamlIdPServicesTests {
    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    protected OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
    protected HttpClient httpClient;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    protected SamlRegisteredService getSamlRegisteredService(
        final long id,
        final String entityId,
        final String metadataLocation) {
        val svc = new SamlRegisteredService();
        svc.setName("AggregatedService-" + id);
        svc.setId(id);
        svc.setServiceId(entityId);
        svc.setMetadataLocation(metadataLocation);
        return svc;
    }

    protected CriteriaSet getCriteriaFor(final String entityId) {
        val criteriaSet1 = new CriteriaSet();
        criteriaSet1.add(new EntityIdCriterion(entityId));
        criteriaSet1.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        return criteriaSet1;
    }

    protected UrlResourceMetadataResolver getMetadataResolver() {
        return new UrlResourceMetadataResolver(httpClient, casProperties.getAuthn().getSamlIdp(), openSamlConfigBean);
    }
}
