package org.apereo.cas;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWsSecurityIdentityProviderAutoConfiguration;
import org.apereo.cas.config.CasWsSecuritySecurityTokenAutoConfiguration;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseCoreWsSecurityIdentityProviderConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(
    classes = BaseCoreWsSecurityIdentityProviderConfigurationTests.SharedTestConfiguration.class,
    properties = {
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

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreSamlAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasWsSecurityIdentityProviderAutoConfiguration.class,
        CasWsSecuritySecurityTokenAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(CasRegisteredServicesTestConfiguration.class)
    public static class SharedTestConfiguration {
    }
}
