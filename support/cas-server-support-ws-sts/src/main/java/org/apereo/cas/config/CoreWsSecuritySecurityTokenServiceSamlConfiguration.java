package org.apereo.cas.config;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.wss4j.common.crypto.WSProviderConfig;
import org.apache.wss4j.common.saml.OpenSAMLUtil;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.ReflectionUtils;

/**
 * This is {@link CoreWsSecuritySecurityTokenServiceSamlConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Paul Spaude
 * @since 5.3.6
 */
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WsFederationIdentityProvider)
@AutoConfigureAfter(CasCoreSamlAutoConfiguration.class)
@Configuration(value = "CoreWsSecuritySecurityTokenServiceSamlConfiguration", proxyBeanMethods = false)
class CoreWsSecuritySecurityTokenServiceSamlConfiguration {
    private static void findFieldAndSetValue(final String fieldName, final Object value) {
        LOGGER.trace("Locating field name [{}]", fieldName);
        val field = ReflectionUtils.findField(OpenSAMLUtil.class, fieldName);
        if (field == null) {
            LOGGER.error("[{}] is undefined and cannot be located in OpenSAMLUtil", fieldName);
            return;
        }
        ReflectionUtils.makeAccessible(field);
        LOGGER.trace("Setting field name [{}]", fieldName);
        Unchecked.consumer(__ -> field.set(null, value)).accept(field);
    }

    @Bean
    @Lazy(false)
    public InitializingBean wsSecurityTokenServiceInitializingBean(
        @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
        final OpenSamlConfigBean openSamlConfigBean) {
        return () -> {
            val warningMessage = "The security token service configuration of CAS will try to disable the OpenSAML bootstrapping process by wss4j, "
                                 + "as it interferes with and prevents CAS' own initialization of OpenSAML. Given the current API limitations of the wss4j library, "
                                 + "which is responsible for the implementation of the security token service in CAS, "
                                 + "Java reflection is used to disable the OpenSAML bootstrapping process. This approach is prone to error, "
                                 + "and may be revisited in future versions of CAS, "
                                 + "once the wss4j library opens up its OpenSAML bootstrapping API in more extensible ways";
            LOGGER.info(warningMessage);

            LOGGER.trace("Initializing WS provider configuration...");
            WSProviderConfig.init();

            LOGGER.trace("Marking OpenSAML components as initialized...");
            findFieldAndSetValue("providerRegistry", openSamlConfigBean.getXmlObjectProviderRegistry());
            findFieldAndSetValue("builderFactory", openSamlConfigBean.getBuilderFactory());
            findFieldAndSetValue("marshallerFactory", openSamlConfigBean.getMarshallerFactory());
            findFieldAndSetValue("unmarshallerFactory", openSamlConfigBean.getUnmarshallerFactory());
            findFieldAndSetValue("samlEngineInitialized", Boolean.TRUE);
        };
    }
}
