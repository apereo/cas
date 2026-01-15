package org.apereo.cas.web.view;

import module java.base;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import org.springframework.web.servlet.View;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Renders and prepares CAS3 views. This view is responsible
 * to just prep the base model, and delegates to
 * a the real view to render the final output.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class Cas30ResponseView extends Cas20ResponseView {
    public Cas30ResponseView(final boolean successResponse,
                             final ProtocolAttributeEncoder protocolAttributeEncoder,
                             final ServicesManager servicesManager,
                             final View view,
                             final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                             final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                             final CasProtocolAttributesRenderer attributesRenderer,
                             final AttributeDefinitionStore attributeDefinitionStore) {
        super(successResponse, protocolAttributeEncoder, servicesManager, view,
            authenticationAttributeReleasePolicy, serviceSelectionStrategy, attributesRenderer, attributeDefinitionStore);
    }

    @Override
    protected void prepareMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) throws Exception {
        super.prepareMergedOutputModel(model, request, response);
        prepareCasResponseAttributesForViewModel(model);
    }
    
}
