package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.CaptchaActivationStrategy;
import org.apereo.cas.web.GoogleCaptchaV2Validator;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ValidateCaptchaActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseCaptchaConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.google-recaptcha.verify-url=http://localhost:${random.int[3000,9000]}")
@Tag("WebflowActions")
@ExtendWith(CasTestExtension.class)
class ValidateCaptchaActionTests {

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_VALIDATE_CAPTCHA)
    private Action validateCaptchaAction;

    @Autowired
    @Qualifier("captchaActivationStrategy")
    private CaptchaActivationStrategy captchaActivationStrategy;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyCaptchaValidationSkipped() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val data = "{\"success\": true }";
        context.setParameter(GoogleCaptchaV2Validator.REQUEST_PARAM_RECAPTCHA_RESPONSE, data);

        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.CAPTCHA_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty("false"));

        servicesManager.save(registeredService);
        WebUtils.putServiceIntoFlowScope(context, service);
        val result = validateCaptchaAction.execute(context);
        assertNull(result);
    }

    @Test
    void verifyCaptchaValidated() throws Throwable {
        val props = casProperties.getGoogleRecaptcha();
        val port = URI.create(props.getVerifyUrl()).getPort();
        
        val context = MockRequestContext.create(applicationContext);

        val data = "{\"success\": true }";
        context.setParameter(GoogleCaptchaV2Validator.REQUEST_PARAM_RECAPTCHA_RESPONSE, data);

        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val result = validateCaptchaAction.execute(context);
            assertNull(result);
        }
    }

    @Test
    void verifyCaptchaFails() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        try (val webServer = new MockWebServer(
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val props = new GoogleRecaptchaProperties()
                .setVerifyUrl("http://localhost:%s".formatted(webServer.getPort()));
            val validateAction = new ValidateCaptchaAction(new GoogleCaptchaV2Validator(props), captchaActivationStrategy);

            val result = validateAction.execute(context);
            assertNotNull(result);
            assertEquals(CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR, result.getId());
        }
    }
}
