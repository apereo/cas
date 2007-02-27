/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import java.util.List;

import org.jasig.cas.web.support.ArgumentExtractor;
import org.springframework.util.Assert;
import org.springframework.webflow.engine.ViewSelector;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ViewSelection;
import org.springframework.webflow.execution.support.ExternalRedirect;

/**
 * ViewSelector that grabs the redirect URL from the proper
 * {@link ArgumentExtractor}.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class DynamicRedirectViewSelector implements ViewSelector {

    private final List<ArgumentExtractor> argumentExtractors;

    public DynamicRedirectViewSelector(
        final List<ArgumentExtractor> argumentExtractors) {
        Assert
            .notNull(argumentExtractors, "argumentExtractors cannot be null.");
        this.argumentExtractors = argumentExtractors;
    }

    public ViewSelection makeRefreshSelection(final RequestContext context) {
        return makeEntrySelection(context);
    }

    public boolean isEntrySelectionRenderable(final RequestContext request) {
        return false;
    }

    public ViewSelection makeEntrySelection(final RequestContext request) {
        for (final ArgumentExtractor argumentExtractor : this.argumentExtractors) {
            final String url = argumentExtractor
                .constructUrlForRedirect(request);

            if (url != null) {
                return new ExternalRedirect(url);
            }
        }
        return null;
    }
}
