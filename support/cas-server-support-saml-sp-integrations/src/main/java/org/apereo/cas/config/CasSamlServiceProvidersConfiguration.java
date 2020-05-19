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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

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
@Lazy(false)
@Slf4j
public class CasSamlServiceProvidersConfiguration implements InitializingBean {

    /**
     * CAS properties.
     */
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> samlRegisteredServiceCachingMetadataResolver;

    @Override
    public void afterPropertiesSet() {
        val preloadMetadata = (Function<SamlRegisteredService, Void>) service -> {
            LOGGER.info("Launching background thread to load the metadata. This might take a while...");
            new Thread(() -> {
                LOGGER.debug("Loading metadata at [{}]...", service.getMetadataLocation());
                samlRegisteredServiceCachingMetadataResolver.getObject()
                    .resolve(service, new CriteriaSet());
            }, getClass().getSimpleName()).start();
            return null;
        };

        val samlSp = casProperties.getSamlSp();
        processSamlServiceProvider(samlSp.getAcademicHealthPlans());
        processSamlServiceProvider(samlSp.getAcademicWorks());
        processSamlServiceProvider(samlSp.getAdobeCloud());
        processSamlServiceProvider(samlSp.getAmazon());
        processSamlServiceProvider(samlSp.getAppDynamics());
        processSamlServiceProvider(samlSp.getArcGIS());
        processSamlServiceProvider(samlSp.getArmsSoftware());
        processSamlServiceProvider(samlSp.getAsana());
        processSamlServiceProvider(samlSp.getBenefitFocus());
        processSamlServiceProvider(samlSp.getBlackBaud());
        processSamlServiceProvider(samlSp.getBox());
        processSamlServiceProvider(samlSp.getBynder());
        processSamlServiceProvider(samlSp.getCccco(), preloadMetadata);
        processSamlServiceProvider(samlSp.getCherWell());
        processSamlServiceProvider(samlSp.getConcurSolutions());
        processSamlServiceProvider(samlSp.getConfluence());
        processSamlServiceProvider(samlSp.getCraniumCafe());
        processSamlServiceProvider(samlSp.getCrashPlan());
        processSamlServiceProvider(samlSp.getDocuSign());
        processSamlServiceProvider(samlSp.getDropbox());
        processSamlServiceProvider(samlSp.getEasyIep());
        processSamlServiceProvider(samlSp.getEgnyte());
        processSamlServiceProvider(samlSp.getEmma());
        processSamlServiceProvider(samlSp.getEverBridge());
        processSamlServiceProvider(samlSp.getEvernote());
        processSamlServiceProvider(samlSp.getFamis());
        processSamlServiceProvider(samlSp.getGartner());
        processSamlServiceProvider(samlSp.getGitlab());
        processSamlServiceProvider(samlSp.getGiveCampus());
        processSamlServiceProvider(samlSp.getHipchat());
        processSamlServiceProvider(samlSp.getInCommon(), preloadMetadata);
        processSamlServiceProvider(samlSp.getInfiniteCampus());
        processSamlServiceProvider(samlSp.getJira());
        processSamlServiceProvider(samlSp.getNeoGov());
        processSamlServiceProvider(samlSp.getNetPartner());
        processSamlServiceProvider(samlSp.getNewRelic());
        processSamlServiceProvider(samlSp.getOffice365());
        processSamlServiceProvider(samlSp.getOpenAthens());
        processSamlServiceProvider(samlSp.getPagerDuty());
        processSamlServiceProvider(samlSp.getPollEverywhere());
        processSamlServiceProvider(samlSp.getQualtrics());
        processSamlServiceProvider(samlSp.getRocketChat());
        processSamlServiceProvider(samlSp.getSafariOnline());
        processSamlServiceProvider(samlSp.getSalesforce());
        processSamlServiceProvider(samlSp.getSaManage());
        processSamlServiceProvider(samlSp.getSansSth());
        processSamlServiceProvider(samlSp.getServiceNow());
        processSamlServiceProvider(samlSp.getSlack());
        processSamlServiceProvider(samlSp.getTopHat());
        processSamlServiceProvider(samlSp.getSserca());
        processSamlServiceProvider(samlSp.getSymplicity());
        processSamlServiceProvider(samlSp.getTableau());
        processSamlServiceProvider(samlSp.getWarpWire());
        processSamlServiceProvider(samlSp.getWebAdvisor());
        processSamlServiceProvider(samlSp.getWebex());
        processSamlServiceProvider(samlSp.getWorkday());
        processSamlServiceProvider(samlSp.getYuja());
        processSamlServiceProvider(samlSp.getZendesk());
        processSamlServiceProvider(samlSp.getZimbra());
        processSamlServiceProvider(samlSp.getZoom());
    }

    private void processSamlServiceProvider(final AbstractSamlSPProperties provider) {
        processSamlServiceProvider(provider, s -> null, s -> null);
    }

    private void processSamlServiceProvider(final AbstractSamlSPProperties provider,
                                            final Function<SamlRegisteredService, Void> afterSave) {
        processSamlServiceProvider(provider, s -> null, afterSave);
    }

    private void processSamlServiceProvider(final AbstractSamlSPProperties provider,
                                            final Function<SamlRegisteredService, Void> beforeSave,
                                            final Function<SamlRegisteredService, Void> afterSave) {
        val service = SamlSPUtils.newSamlServiceProviderService(provider,
            samlRegisteredServiceCachingMetadataResolver.getObject());
        if (service != null) {
            LOGGER.debug("Constructed service definition [{}]", service);
            beforeSave.apply(service);
            SamlSPUtils.saveService(service, servicesManager.getObject());
            afterSave.apply(service);
        }
    }
}
