package org.apereo.cas.support.saml;

import org.apereo.cas.config.CasSamlIdPDynamoDbAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.SdkSystemSetting;

/**
 * This is {@link BaseDynamoDbSamlMetadataTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    CasSamlIdPDynamoDbAutoConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.saml-idp.metadata.dynamo-db.endpoint=http://localhost:8000",
    "cas.authn.saml-idp.metadata.dynamo-db.drop-tables-on-startup=true",
    "cas.authn.saml-idp.metadata.dynamo-db.local-instance=true",
    "cas.authn.saml-idp.metadata.dynamo-db.region=us-east-1"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseDynamoDbSamlMetadataTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }
    
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("dynamoDbSamlRegisteredServiceMetadataResolver")
    protected SamlRegisteredServiceMetadataResolver resolver;
    
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(SamlIdPMetadataGenerator.BEAN_NAME)
    protected SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Autowired
    @Qualifier(SamlIdPMetadataLocator.BEAN_NAME)
    protected SamlIdPMetadataLocator samlIdPMetadataLocator;
}
