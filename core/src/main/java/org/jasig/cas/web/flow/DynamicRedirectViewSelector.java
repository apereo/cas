/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.web.support.ArgumentExtractor;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.support.ExternalRedirect;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public final class DynamicRedirectViewSelector implements ViewSelector {
    
    private final ArgumentExtractor[] argumentExtractors;

    public DynamicRedirectViewSelector(final ArgumentExtractor[] argumentExtractors) {
        Assert.notNull(argumentExtractors, "argumentExtractors cannot be null.");
        this.argumentExtractors = argumentExtractors;
    }

    public ViewSelection makeRefreshSelection(final RequestContext context) {
        return makeSelection(context);
    }

    public ViewSelection makeSelection(final RequestContext context) {
        for (int i = 0; i < this.argumentExtractors.length; i++) {
            final String url = this.argumentExtractors[i].constructUrlForRedirct(context);
            
            if (url != null) {
                return new ExternalRedirect(url);
            }
        }
        
        return null;
    }
}
