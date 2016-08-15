package org.apereo.cas.config;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link CasSamlSPDropboxConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSamlSPDropboxConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSamlSPDropboxConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @PostConstruct
    public void init() {
        try {
            if (StringUtils.isNotBlank(casProperties.getSamlSP().getDropbox().getMetadata())) {
                final SamlRegisteredService service = new SamlRegisteredService();
                service.setName(casProperties.getSamlSP().getDropbox().getName());
                service.setDescription(casProperties.getSamlSP().getDropbox().getDescription());

                final Resource resource = ResourceUtils.prepareClasspathResourceIfNeeded(
                        ResourceUtils.getResourceFrom(casProperties.getSamlSP().getDropbox().getMetadata())
                );
                final String content = IOUtils.toString(resource.getInputStream(), "UTF-8");
                final Matcher m = Pattern.compile("entityID=\"(\\w+)", Pattern.CASE_INSENSITIVE).matcher(content);
                if (m.find()) {
                    service.setServiceId(m.group(1));
                } else {
                    throw new IllegalArgumentException("Could not locate entityID from the supplied metadata file " +
                            casProperties.getSamlSP().getDropbox().getMetadata());
                }
                
                service.setEvaluationOrder(Integer.MIN_VALUE);
                service.setMetadataLocation(casProperties.getSamlSP().getDropbox().getMetadata());
                service.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(
                        Lists.newArrayList(casProperties.getSamlSP().getDropbox().getNameIdAttribute())));
                service.setUsernameAttributeProvider(
                        new PrincipalAttributeRegisteredServiceUsernameProvider(
                                casProperties.getSamlSP().getDropbox().getNameIdAttribute()));
                servicesManager.save(service);
                servicesManager.load();
            }
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
        
        
    }
}
