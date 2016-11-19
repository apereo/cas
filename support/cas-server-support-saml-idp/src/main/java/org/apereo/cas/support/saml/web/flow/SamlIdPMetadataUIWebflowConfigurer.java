package org.apereo.cas.support.saml.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SamlIdPMetadataUIWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlIdPMetadataUIWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private Action samlMetadataUIParserAction;

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ViewState state = (ViewState) flow.getTransitionableState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            state.getEntryActionList().add(this.samlMetadataUIParserAction);
        }
    }

    public void setSamlMetadataUIParserAction(final Action samlMetadataUIParserAction) {
        this.samlMetadataUIParserAction = samlMetadataUIParserAction;
    }
}
