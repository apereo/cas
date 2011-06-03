/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;


/**
 * Generates the login ticket parameter as described in section 3.5 of the
 * <a href="http://www.jasig.org/cas/protocol">CAS protocol</a>.
 *
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.4.9
 *
 */
public class GenerateLoginTicketAction {
    /** 3.5.1 - Login tickets SHOULD begin with characters "LT-" */
    private static final String PREFIX = "LT";

    /** Logger instance */
    private final Log logger = LogFactory.getLog(getClass());


    @NotNull
    private UniqueTicketIdGenerator ticketIdGenerator;

    public final String generate(final RequestContext context) {
        final String loginTicket = this.ticketIdGenerator.getNewTicketId(PREFIX);
        this.logger.debug("Generated login ticket " + loginTicket);
        WebUtils.putLoginTicket(context, loginTicket);
        return "success";
    }

    public void setTicketIdGenerator(final UniqueTicketIdGenerator generator) {
        this.ticketIdGenerator = generator;
    }
}
