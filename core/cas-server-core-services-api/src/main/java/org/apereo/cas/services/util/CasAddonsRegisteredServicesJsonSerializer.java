package org.apereo.cas.services.util;

import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is {@link CasAddonsRegisteredServicesJsonSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CasAddonsRegisteredServicesJsonSerializer extends RegisteredServiceJsonSerializer {

    private static final long serialVersionUID = 1874802012930264278L;

    private static final String SERVICE_REGISTRY_FILENAME = "servicesRegistry";

    private static final String SERVICES_KEY = "services";

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static RegisteredService convertServiceProperties(final Map serviceDataMap) {
        val service = new RegexRegisteredService();

        service.setId(Long.parseLong(serviceDataMap.get("id").toString()));
        service.setName(serviceDataMap.get("name").toString());
        service.setDescription(serviceDataMap.getOrDefault("description", StringUtils.EMPTY).toString());
        service.setServiceId(serviceDataMap.get("serviceId").toString().replace("**", ".*"));
        service.setTheme(serviceDataMap.getOrDefault("theme", StringUtils.EMPTY).toString());
        service.setEvaluationOrder(Integer.parseInt(serviceDataMap.getOrDefault("evaluationOrder", Integer.MAX_VALUE).toString()));

        val allowedProxy = Boolean.parseBoolean(serviceDataMap.getOrDefault("allowedToProxy", Boolean.FALSE).toString());
        val enabled = Boolean.parseBoolean(serviceDataMap.getOrDefault("enabled", Boolean.TRUE).toString());
        val ssoEnabled = Boolean.parseBoolean(serviceDataMap.getOrDefault("ssoEnabled", Boolean.TRUE).toString());
        val anonymousAccess = Boolean.parseBoolean(serviceDataMap.getOrDefault("anonymousAccess", Boolean.TRUE).toString());

        if (allowedProxy) {
            service.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy(".+"));
        }
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(enabled, ssoEnabled));
        if (anonymousAccess) {
            service.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider());
        }
        val attributes = (List<String>) serviceDataMap.getOrDefault("allowedAttributes", new ArrayList<>(0));
        service.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(attributes));
        return service;
    }

    @Override
    public Collection<RegisteredService> load(final InputStream stream) {
        val results = new ArrayList<RegisteredService>();
        try {
            val servicesMap = (Map<String, List>) this.objectMapper.readValue(stream, Map.class);
            val it = (Iterator<Map>) servicesMap.get(SERVICES_KEY).iterator();
            while (it.hasNext()) {
                val record = it.next();
                val svc = convertServiceProperties(record);
                LOGGER.debug("Loaded service [{}] from legacy syntax", svc);
                results.add(svc);
            }
            LOGGER.warn("CAS has successfully loaded [{}] service(s) which contain definitions designed in a legacy syntax provided by cas-addons. "
                    + "While this behavior is strictly kept for backward-compatibility reasons, it is STRONGLY recommended that "
                    + "you convert these definitions into the official syntax to take full advantage of the service capabilities. "
                    + "Future CAS versions may decide to entirely ignore the legacy syntax altogether.",
                results.size());
            results.forEach(Unchecked.consumer(s -> {
                val fileName = new File(FileUtils.getTempDirectory(), s.getName() + '-' + s.getId() + ".json");
                to(fileName, s);
                LOGGER.warn("Converted legacy service definition for [{}] may be reviewed at [{}]", s.getServiceId(), fileName);
            }));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return results;
    }

    @Override
    public boolean supports(final File file) {
        return file.getName().startsWith(SERVICE_REGISTRY_FILENAME);
    }
}
