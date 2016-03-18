package org.jasig.cas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasWebApplicationSupportConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casWebApplicationSupportConfiguration")
public class CasWebApplicationSupportConfiguration {

    /**
     * Web content interceptor web content interceptor.
     *
     * @return the web content interceptor
     */
    @Bean(name = "webContentInterceptor")
    public WebContentInterceptor webContentInterceptor() {
        final WebContentInterceptor interceptor = new WebContentInterceptor();
        interceptor.setCacheSeconds(0);
        interceptor.setAlwaysUseFullPath(true);
        return interceptor;
    }

    /**
     * Service theme resolver supported browsers map.
     *
     * @return the map
     */
    @Bean(name = "serviceThemeResolverSupportedBrowsers")
    public Map serviceThemeResolverSupportedBrowsers() {
        final Map<String, String> map = new HashMap<>();
        map.put(".*iPhone.*", "iphone");
        map.put(".*Android.*", "android");
        map.put(".*Safari.*Pre.*", "safari");
        map.put(".*iPhone.*", "iphone");
        map.put(".*Nokia.*AppleWebKit.*", "nokiawebkit");
        return map;
    }
}
