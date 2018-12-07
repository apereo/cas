package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * This is {@link UrlResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CoreSamlConfiguration.class
})
@Category(FileSystemCategory.class)
@TestPropertySource(properties = {"cas.authn.samlIdp.metadata.location=file:/tmp"})
public class UrlResourceMetadataResolverTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Test
    public void verifyResolverSupports() {
        final SamlIdPProperties props = new SamlIdPProperties();
        props.getMetadata().setLocation(new FileSystemResource(FileUtils.getTempDirectory()));
        final UrlResourceMetadataResolver resolver = new UrlResourceMetadataResolver(props, openSamlConfigBean);
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setMetadataLocation("http://www.testshib.org/metadata/testshib-providers.xml");
        assertTrue(resolver.supports(service));
        service.setMetadataLocation("classpath:sample-sp.xml");
        assertFalse(resolver.supports(service));
    }

    @Test
    public void verifyResolverResolves() {
        final SamlIdPProperties props = new SamlIdPProperties();
        props.getMetadata().setLocation(new FileSystemResource(FileUtils.getTempDirectory()));
        final UrlResourceMetadataResolver resolver = new UrlResourceMetadataResolver(props, openSamlConfigBean);
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        service.setMetadataLocation("https://raw.githubusercontent.com/apereo/cas/5.3.x/support/cas-server-support-saml-idp/src/test/resources/metadata/testshib-providers.xml");
        final Collection results = resolver.resolve(service);
        assertFalse(results.isEmpty());
    }
}
