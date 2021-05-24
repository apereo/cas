package org.apereo.cas;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWsSecurityTokenTicketCatalogConfiguration;
import org.apereo.cas.config.CasWsSecurityTokenTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderComponentSerializationConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderWebflowConfiguration;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenServiceConfiguration;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenServiceSamlConfiguration;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenTicketConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseCoreWsSecurityIdentityProviderConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    BaseCoreWsSecurityIdentityProviderConfigurationTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.wsfed-idp.idp.realm=urn:org:apereo:cas:ws:idp:realm-CAS",
    "cas.authn.wsfed-idp.idp.realm-name=CAS",

    "cas.authn.wsfed-idp.sts.signing-keystore-file=classpath:ststrust.jks",
    "cas.authn.wsfed-idp.sts.signing-keystore-password=storepass",

    "cas.authn.wsfed-idp.sts.encryption-keystore-file=classpath:stsencrypt.jks",
    "cas.authn.wsfed-idp.sts.encryption-keystore-password=storepass",

    "cas.authn.wsfed-idp.sts.subject-name-id-format=unspecified",
    "cas.authn.wsfed-idp.sts.encrypt-tokens=true",

    "cas.authn.wsfed-idp.sts.realm.keystore-file=stsrealm_a.jks",
    "cas.authn.wsfed-idp.sts.realm.keystore-password=storepass",
    "cas.authn.wsfed-idp.sts.realm.keystore-alias=realma",
    "cas.authn.wsfed-idp.sts.realm.key-password=realma",
    "cas.authn.wsfed-idp.sts.realm.issuer=CAS"
})
public abstract class BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CoreSamlConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreConfiguration.class,

        CoreWsSecurityIdentityProviderComponentSerializationConfiguration.class,
        CoreWsSecurityIdentityProviderConfiguration.class,
        CoreWsSecurityIdentityProviderWebflowConfiguration.class,

        CasWsSecurityTokenTicketCatalogConfiguration.class,
        CasWsSecurityTokenTicketComponentSerializationConfiguration.class,
        CoreWsSecuritySecurityTokenServiceConfiguration.class,
        CoreWsSecuritySecurityTokenTicketConfiguration.class,
        CoreWsSecuritySecurityTokenServiceSamlConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
