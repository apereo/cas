package org.apereo.cas.config.support.authentication;

import net.phonefactor.pfsdk.PFAuth;
import net.phonefactor.pfsdk.PFAuthParams;
import net.phonefactor.pfsdk.PFAuthResult;
import net.phonefactor.pfsdk.PlainTextPinInfo;
import net.phonefactor.pfsdk.StandardPinInfo;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.azure.AzureAuthenticatorAuthenticationHandler;
import org.apereo.cas.adaptors.azure.AzureAuthenticatorAuthenticationRequestBuilder;
import org.apereo.cas.adaptors.azure.AzureAuthenticatorMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.azure.web.flow.AzureAuthenticatorGenerateTokenAction;
import org.apereo.cas.authentication.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * This is {@link AzureAuthenticatorAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("azureAuthenticatorAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AzureAuthenticatorAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean
    public PFAuth azureAuthenticatorInstance() {
        try {
            final MultifactorAuthenticationProperties.Azure azure = casProperties.getAuthn().getMfa().getAzure();
            final File cfg = new File(azure.getConfigDir());
            if (!cfg.exists() || !cfg.isDirectory()) {
                throw new FileNotFoundException(cfg.getAbsolutePath() + " does not exist or is not a directory");
            }
            final PFAuth pf = new PFAuth();
            pf.setDebug(azure.isDebugEnabled());
            pf.setAllowInternationalCalls(azure.isAllowInternationalCalls());

            final String dir = StringUtils.appendIfMissing(azure.getConfigDir(), "/");
            pf.initialize(dir, azure.getPrivateKeyPassword());
            return pf;
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public AzureAuthenticatorAuthenticationRequestBuilder azureAuthenticationRequestBuilder() {
        final MultifactorAuthenticationProperties.Azure azure = casProperties.getAuthn().getMfa().getAzure();
        return new AzureAuthenticatorAuthenticationRequestBuilder(
                azure.getPhoneAttributeName(),
                azure.getMode());
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler azureAuthenticatorAuthenticationHandler() {
        final AzureAuthenticatorAuthenticationHandler h =
                new AzureAuthenticatorAuthenticationHandler(azureAuthenticatorInstance(), azureAuthenticationRequestBuilder());
        h.setPrincipalFactory(azurePrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setName(casProperties.getAuthn().getMfa().getAzure().getName());
        return h;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass azureBypassEvaluator() {
        return new DefaultMultifactorAuthenticationProviderBypass(
                casProperties.getAuthn().getMfa().getAzure().getBypass(),
                ticketRegistrySupport
        );
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider azureAuthenticatorAuthenticationProvider() {
        final MultifactorAuthenticationProperties.Azure azure = casProperties.getAuthn().getMfa().getAzure();
        final AzureAuthenticatorMultifactorAuthenticationProvider p = new AzureAuthenticatorMultifactorAuthenticationProvider();
        p.setBypassEvaluator(azureBypassEvaluator());
        p.setGlobalFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        p.setOrder(azure.getRank());
        p.setId(azure.getId());
        return p;
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator azureAuthenticatorAuthenticationMetaDataPopulator() {
        return new AuthenticationContextAttributeMetaDataPopulator(
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                azureAuthenticatorAuthenticationHandler(),
                azureAuthenticatorAuthenticationProvider()
        );
    }

    @Bean
    @RefreshScope
    public Action azureGenerateTokenAction() {
        final MultifactorAuthenticationProperties.Azure azure = casProperties.getAuthn().getMfa().getAzure();
        return new AzureAuthenticatorGenerateTokenAction(azure.getMode());
    }

    @ConditionalOnMissingBean(name = "azurePrincipalFactory")
    @Bean
    public PrincipalFactory azurePrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getAzure().getConfigDir())) {
            plan.registerAuthenticationHandler(azureAuthenticatorAuthenticationHandler());
            plan.registerMetadataPopulator(azureAuthenticatorAuthenticationMetaDataPopulator());
        }
    }

    public static void main(final String[] args) {
        try {
            final PFAuth pf = new PFAuth();
            pf.setDebug(true);
            pf.setAllowInternationalCalls(true);
            pf.initialize("/etc/cas/azure/", "XKPN3CBG4QWI8CNP");

            final PFAuthParams params = new PFAuthParams();
            params.setPhoneNumber("3477464665");
            params.setCountryCode("1");
            params.setUsername("casuser");
            params.setAuthInfo(new StandardPinInfo());
            //params.setAuthInfo(new PlainTextPinInfo("000000"));
            final PFAuthResult r = pf.authenticate(params);
           
            if (r.getAuthenticated()) {
                System.out.println(r.getOtp());
                System.out.println(r.getEnteredPin());

                System.out.println("GOOD AUTH " + r.getCallStatus());
                System.out.println("Call Status: " + r.getCallStatusString());

                switch (r.getCallStatus()) {
                    case PFAuthResult.CALL_STATUS_PIN_ENTERED:
                        System.out.println("I have detected that a PIN was entered.");
                        break;

                    case PFAuthResult.CALL_STATUS_NO_PIN_ENTERED:
                        System.out.println("I have detected that NO PIN was entered.");
                        break;

                    default:
                }
            } else {
                System.out.println("BAD AUTH");
                System.out.println("Call Status: " + r.getCallStatusString());

                switch (r.getCallStatus()) {
                    case PFAuthResult.CALL_STATUS_USER_HUNG_UP:
                        System.out.println("I have detected that the user hung up.");
                        break;

                    case PFAuthResult.CALL_STATUS_PHONE_BUSY:
                        System.out.println("I have detected that the phone was busy.");
                        break;

                    default:
                }
                if (r.getMessageErrorId() != 0) {
                    System.out.println("Message Error ID: " + r.getMessageErrorId());

                    String messageError = r.getMessageError();

                    if (messageError != null)
                        System.out.println("Message Error: " + messageError);
                }
            }
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }
}
