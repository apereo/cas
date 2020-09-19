package org.apereo.cas.qr.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.util.QRUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * This is {@link QRAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class QRAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public QRAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            val setAction = createSetAction("flowScope.qrAuthenticationEnabled", "true");
            state.getEntryActionList().add(setAction);

            state.getEntryActionList().add(requestContext -> {
                val id = UUID.randomUUID().toString();
                LOGGER.debug("Generating QR code with channel id [{}]", id);

                val stream = new ByteArrayOutputStream();
                QRUtils.generateQRCode(stream, id, QRUtils.WIDTH_LARGE, QRUtils.WIDTH_LARGE);
                val image = EncodingUtils.encodeBase64ToByteArray(stream.toByteArray());
                requestContext.getFlowScope().put("qrCode", new String(image, StandardCharsets.UTF_8));
                requestContext.getFlowScope().put("qrChannel", id);
                return null;
            });
        }
    }
}
