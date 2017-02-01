package org.apereo.cas.web.report.util;

import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.config.server.config.ConfigServerProperties;

import java.util.Map;

/**
 * This is {@link ControllerUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public final class ControllerUtils {
    private ControllerUtils() {
    }

    /**
     * Configure model map for config server cloud bus endpoints.
     *
     * @param busProperties          the bus properties
     * @param configServerProperties the config server properties
     * @param path                   the path
     * @param model                  the model
     */
    public static void configureModelMapForConfigServerCloudBusEndpoints(final BusProperties busProperties,
                                                                         final ConfigServerProperties configServerProperties,
                                                                         final String path,
                                                                         final Map model) {
        if (busProperties != null && busProperties.isEnabled()) {
            model.put("refreshEndpoint", path + configServerProperties.getPrefix() + "/cas/bus/refresh");
            model.put("refreshMethod", "GET");
        } else {
            model.put("refreshEndpoint", path + "/status/refresh");
            model.put("refreshMethod", "POST");
        }
    }
}
