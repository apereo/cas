package org.apereo.cas.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;

import lombok.val;
import org.springframework.http.MediaType;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;

/**
 * Custom View to Return the CAS 1.0 Protocol Response. Implemented as a view
 * class rather than a JSP (like CAS 2.0 spec) because of the requirement of the
 * line feeds to be "\n".
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class Cas10ResponseView extends AbstractCasView {

    public Cas10ResponseView(final boolean successResponse,
                             final ProtocolAttributeEncoder protocolAttributeEncoder,
                             final ServicesManager servicesManager,
                             final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
                             final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                             final CasProtocolAttributesRenderer attributesRenderer,
                             final AttributeDefinitionStore attributeDefinitionStore) {
        super(successResponse, protocolAttributeEncoder, servicesManager,
            authenticationAttributeReleasePolicy, serviceSelectionStrategy,
            attributesRenderer, attributeDefinitionStore);
    }

    @Override
    protected void renderMergedOutputModel(
        @Nonnull
        final Map model, @Nonnull
        final HttpServletRequest request,
        @Nonnull
        final HttpServletResponse response) throws Exception {
        try (val writer = new StringWriter()) {
            if (this.successResponse) {
                prepareViewModelWithAuthenticationPrincipal(model);
                prepareCasResponseAttributesForViewModel(model);
                writer.write("yes\n" + getPrimaryAuthenticationFrom(model).getPrincipal().getId() + '\n');
                if (model.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FORMATTED_ATTRIBUTES)) {
                    val attributes = (Collection) model.get(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FORMATTED_ATTRIBUTES);
                    attributes.forEach(attr -> {
                        writer.write(attr.toString());
                        writer.write('\n');
                    });
                }
            } else {
                writer.write("no\n\n");
            }
            val message = writer.toString();
            LoggingUtils.protocolMessage("CAS v1 Response", Map.of(), message);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write(message);
        }
    }
}
