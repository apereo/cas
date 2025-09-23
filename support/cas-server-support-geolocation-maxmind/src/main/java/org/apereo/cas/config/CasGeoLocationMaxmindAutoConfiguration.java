package org.apereo.cas.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.geo.GeoLocationServiceConfigurer;
import org.apereo.cas.support.geo.maxmind.MaxmindDatabaseGeoLocationService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.Resource;

/**
 * This is {@link CasGeoLocationMaxmindAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GeoLocation, module = "maxmind")
@AutoConfiguration
public class CasGeoLocationMaxmindAutoConfiguration {

    private static DatabaseReader readDatabase(final Resource maxmindDatabase) {
        return FunctionUtils.doIf(ResourceUtils.doesResourceExist(maxmindDatabase),
                Unchecked.supplier(() ->
                    new DatabaseReader.Builder(maxmindDatabase.getFile())
                        .fileMode(Reader.FileMode.MEMORY)
                        .withCache(new CHMCache())
                        .build()),
                () -> null)
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "maxMindGeoLocationService")
    public GeoLocationService maxMindGeoLocationService(final CasConfigurationProperties casProperties) throws Exception {
        val properties = casProperties.getGeoLocation().getMaxmind();
        val cityDatabase = readDatabase(properties.getCityDatabase());
        val countryDatabase = readDatabase(properties.getCountryDatabase());
        return new MaxmindDatabaseGeoLocationService(properties, cityDatabase, countryDatabase, null);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "maxMindGeoLocationServiceConfigurer")
    public GeoLocationServiceConfigurer maxMindGeoLocationServiceConfigurer(
        @Qualifier("maxMindGeoLocationService")
        final GeoLocationService maxMindGeoLocationService) {
        return () -> maxMindGeoLocationService;
    }
}
