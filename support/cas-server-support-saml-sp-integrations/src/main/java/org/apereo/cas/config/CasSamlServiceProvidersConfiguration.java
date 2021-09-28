package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.sps.AbstractSamlSPProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.SamlSPUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * This is {@link CasSamlServiceProvidersConfiguration}.
 * This class is marked as non-lazy, allowing to force-initialize the configuration
 * to process saml sp definitions as services.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "casSamlServiceProvidersConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasSamlServiceProvidersConfiguration {

    private static void processSamlServiceProvider(final AbstractSamlSPProperties provider,
                                                   final ServicesManager servicesManager,
                                                   final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver) {
        processSamlServiceProvider(provider, servicesManager, samlRegisteredServiceCachingMetadataResolver, s -> null, s -> null);
    }

    private static void processSamlServiceProvider(final AbstractSamlSPProperties provider,
                                                   final ServicesManager servicesManager,
                                                   final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                                   final Function<SamlRegisteredService, Void> afterSave) {
        processSamlServiceProvider(provider, servicesManager, samlRegisteredServiceCachingMetadataResolver, s -> null, afterSave);
    }

    private static void processSamlServiceProvider(final AbstractSamlSPProperties provider,
                                                   final ServicesManager servicesManager,
                                                   final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                                   final Function<SamlRegisteredService, Void> beforeSave,
                                                   final Function<SamlRegisteredService, Void> afterSave) {
        val service = SamlSPUtils.newSamlServiceProviderService(provider, samlRegisteredServiceCachingMetadataResolver);
        if (service != null) {
            LOGGER.trace("Constructed service definition [{}]", service);
            beforeSave.apply(service);
            SamlSPUtils.saveService(service, servicesManager);
            afterSave.apply(service);
        }
    }

    @Bean
    @Autowired
    public InitializingBean coreSamlServiceProvidersInitializingBean(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier(SamlRegisteredServiceCachingMetadataResolver.DEFAULT_BEAN_NAME)
        final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver) {
        return () -> {
            val preloadMetadata = (Function<SamlRegisteredService, Void>) service -> {
                LOGGER.info("Launching background thread to load the metadata. This might take a while...");
                new Thread(() -> {
                    LOGGER.debug("Loading metadata at [{}]...", service.getMetadataLocation());
                    samlRegisteredServiceCachingMetadataResolver.resolve(service, new CriteriaSet());
                }, getClass().getSimpleName()).start();
                return null;
            };

            val samlSp = casProperties.getSamlSp();
            processSamlServiceProvider(samlSp.getAcademicHealthPlans(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getAcademicWorks(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getAdobeCloud(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getAmazon(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getAppDynamics(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getArcGIS(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getArmsSoftware(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getAsana(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getBenefitFocus(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getBlackBaud(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getBox(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getBynder(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getCccco(), servicesManager, samlRegisteredServiceCachingMetadataResolver, preloadMetadata);
            processSamlServiceProvider(samlSp.getCherWell(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getConcurSolutions(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getConfluence(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getCraniumCafe(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getCrashPlan(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getDocuSign(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getDropbox(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getEasyIep(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getEgnyte(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getEmma(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getEverBridge(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getEvernote(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getFamis(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getGartner(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getGitlab(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getGiveCampus(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getHipchat(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getInCommon(), servicesManager, samlRegisteredServiceCachingMetadataResolver, preloadMetadata);
            processSamlServiceProvider(samlSp.getInfiniteCampus(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getJira(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getNeoGov(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getNetPartner(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getNewRelic(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getOffice365(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getOpenAthens(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getPagerDuty(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getPollEverywhere(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getQualtrics(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getRocketChat(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getSafariOnline(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getSalesforce(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getSaManage(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getSansSth(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getServiceNow(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getSlack(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getTopHat(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getSserca(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getSymplicity(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getTableau(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getWarpWire(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getWebAdvisor(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getWebex(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getWorkday(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getYuja(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getZendesk(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getZimbra(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
            processSamlServiceProvider(samlSp.getZoom(), servicesManager, samlRegisteredServiceCachingMetadataResolver);
        };
    }
}
