package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.saml.sps.AbstractSamlSPProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.SamlSPUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import java.util.function.Function;

/**
 * This is {@link CasSamlServiceProvidersAutoConfiguration}.
 * This class is marked as non-lazy, allowing to force-initialize the configuration
 * to process saml sp definitions as services.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAML)
@Slf4j
@AutoConfiguration
public class CasSamlServiceProvidersAutoConfiguration {

    private static void processSamlServiceProvider(final AbstractSamlSPProperties provider,
                                                   final ServicesManager servicesManager,
                                                   final SamlRegisteredServiceCachingMetadataResolver resolver) throws Exception {
        processSamlServiceProvider(provider, servicesManager, resolver, _ -> null, _ -> null);
    }

    private static void processSamlServiceProvider(final AbstractSamlSPProperties provider,
                                                   final ServicesManager servicesManager,
                                                   final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                   final Function<SamlRegisteredService, Void> afterSave) throws Exception {
        processSamlServiceProvider(provider, servicesManager, resolver, _ -> null, afterSave);
    }

    private static void processSamlServiceProvider(
        final AbstractSamlSPProperties provider,
        final ServicesManager servicesManager,
        final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
        final Function<SamlRegisteredService, Void> beforeSave,
        final Function<SamlRegisteredService, Void> afterSave) throws Exception {
        val service = SamlSPUtils.newSamlServiceProviderService(provider, samlRegisteredServiceCachingMetadataResolver);
        if (service != null) {
            LOGGER.trace("Constructed service definition [{}]", service);
            beforeSave.apply(service);
            SamlSPUtils.saveService(service, servicesManager);
            afterSave.apply(service);
        }
    }

    @Bean
    @Lazy(false)
    public InitializingBean coreSamlServiceProvidersInitializingBean(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
        final SamlRegisteredServiceCachingMetadataResolver resolver) {
        return () -> {
            val preloadMetadata = (Function<SamlRegisteredService, Void>) service -> {
                LOGGER.info("Launching background thread to load the metadata. This might take a while...");
                Thread.startVirtualThread(Unchecked.runnable(() -> {
                    LOGGER.debug("Loading metadata at [{}]...", service.getMetadataLocation());
                    resolver.resolve(service, new CriteriaSet());
                }));
                return null;
            };

            val samlSp = casProperties.getSamlSp();
            processSamlServiceProvider(samlSp.getAcademicHealthPlans(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getAdobeCloud(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getAmazon(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getAppDynamics(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getArcGIS(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getArmsSoftware(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getAsana(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getBenefitFocus(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getBlackBaud(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getBox(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getBynder(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getCccco(), servicesManager, resolver, preloadMetadata);
            processSamlServiceProvider(samlSp.getCherWell(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getConcurSolutions(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getConfluence(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getCraniumCafe(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getCrashPlan(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getDocuSign(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getDropbox(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getEasyIep(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getEgnyte(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getEmma(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getEverBridge(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getEvernote(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getFamis(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getGartner(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getGitlab(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getGiveCampus(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getInCommon(), servicesManager, resolver, preloadMetadata);
            processSamlServiceProvider(samlSp.getInfiniteCampus(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getJira(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getNeoGov(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getNetPartner(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getNewRelic(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getOffice365(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getOpenAthens(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getPagerDuty(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getPollEverywhere(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getQualtrics(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getRocketChat(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getSafariOnline(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getSalesforce(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getSaManage(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getSansSth(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getServiceNow(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getSlack(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getTopHat(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getSserca(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getSymplicity(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getTableau(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getWarpWire(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getWebAdvisor(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getWebex(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getWorkday(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getYuja(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getZendesk(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getZimbra(), servicesManager, resolver);
            processSamlServiceProvider(samlSp.getZoom(), servicesManager, resolver);
        };
    }
}
