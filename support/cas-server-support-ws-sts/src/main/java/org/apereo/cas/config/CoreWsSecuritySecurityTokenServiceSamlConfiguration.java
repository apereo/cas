package org.apereo.cas.config;

import org.apereo.cas.support.saml.OpenSamlConfigBean;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.wss4j.common.crypto.WSProviderConfig;
import org.apache.wss4j.common.saml.OpenSAMLUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;

/**
 * This is {@link CoreWsSecuritySecurityTokenServiceSamlConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Paul Spaude
 * @since 5.3.6
 */
@Configuration(value = "coreWsSecuritySecurityTokenServiceSamlConfiguration", proxyBeanMethods = false)
@Slf4j
@AutoConfigureAfter(CoreSamlConfiguration.class)
public class CoreWsSecuritySecurityTokenServiceSamlConfiguration {

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @PostConstruct
    public void afterPropertiesSet() {
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
        val openSaml = openSamlConfigBean.getObject();
        findFieldAndSetValue("providerRegistry", openSaml.getXmlObjectProviderRegistry());
        findFieldAndSetValue("builderFactory", openSaml.getBuilderFactory());
        findFieldAndSetValue("marshallerFactory", openSaml.getMarshallerFactory());
        findFieldAndSetValue("unmarshallerFactory", openSaml.getUnmarshallerFactory());
        findFieldAndSetValue("samlEngineInitialized", Boolean.TRUE);
    }

    @SneakyThrows
    private static void findFieldAndSetValue(final String fieldName, final Object value) {
        LOGGER.trace("Locating field name [{}]", fieldName);
        val field = ReflectionUtils.findField(OpenSAMLUtil.class, fieldName);
        if (field == null) {
            LOGGER.error("[{}] is undefined and cannot be located in OpenSAMLUtil", fieldName);
            return;
        }
        ReflectionUtils.makeAccessible(field);

        LOGGER.trace("Setting field name [{}]", fieldName);
        field.set(null, value);
    }
}
