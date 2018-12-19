package org.apereo.cas.config;

import org.apereo.cas.support.saml.OpenSamlConfigBean;

import lombok.extern.slf4j.Slf4j;
import org.apache.wss4j.common.crypto.WSProviderConfig;
import org.apache.wss4j.common.saml.OpenSAMLUtil;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

/**
 * This is {@link CoreWsSecuritySecurityTokenServiceSamlConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Paul Spaude
 * @since 5.3.6
 */
@Configuration("coreWsSecuritySecurityTokenServiceSamlConfiguration")
@Slf4j
@AutoConfigureAfter(CoreSamlConfiguration.class)
public class CoreWsSecuritySecurityTokenServiceSamlConfiguration {

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        final String warningMessage = "The security token service configuration of CAS will try to disable the OpenSAML bootstrapping process by wss4j, "
            + "as it interferes with and prevents CAS' own initialization of OpenSAML. Given the current API limitations of the wss4j library, "
            + "which is responsible for the implementation of the security token service in CAS, "
            + "Java reflection is used to disable the OpenSAML bootstrapping process. This approach is prone to error, "
            + "and may be revisited in future versions of CAS, "
            + "once the wss4j library opens up its OpenSAML bootstrapping API in more extensible ways";

        LOGGER.warn(warningMessage);

        WSProviderConfig.init();

        final Field providerRegistry = ReflectionUtils.findField(OpenSAMLUtil.class, "providerRegistry");
        final Field builderFactory = ReflectionUtils.findField(OpenSAMLUtil.class, "builderFactory");
        final Field marshallerFactory = ReflectionUtils.findField(OpenSAMLUtil.class, "marshallerFactory");
        final Field unmarshallerFactory = ReflectionUtils.findField(OpenSAMLUtil.class, "unmarshallerFactory");
        final Field samlEngineInitialized = ReflectionUtils.findField(OpenSAMLUtil.class, "samlEngineInitialized");

        ReflectionUtils.makeAccessible(providerRegistry);
        ReflectionUtils.makeAccessible(builderFactory);
        ReflectionUtils.makeAccessible(marshallerFactory);
        ReflectionUtils.makeAccessible(unmarshallerFactory);
        ReflectionUtils.makeAccessible(samlEngineInitialized);

        providerRegistry.set(null, ConfigurationService.get(XMLObjectProviderRegistry.class));
        builderFactory.set(null, openSamlConfigBean.getBuilderFactory());
        marshallerFactory.set(null, openSamlConfigBean.getMarshallerFactory());
        unmarshallerFactory.set(null, openSamlConfigBean.getUnmarshallerFactory());

        samlEngineInitialized.setBoolean(null, true);
    }
}
