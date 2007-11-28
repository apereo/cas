/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.event.TicketEvent.TicketEventType;
import org.jasig.cas.logging.LogRequest;
import org.jasig.cas.logging.LogSearchRequest;
import org.jasig.cas.logging.LoggingManager;
import org.jasig.cas.util.annotation.NotNull;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public class SearchLogsController extends AbstractController {
    
    @NotNull
    private final LoggingManager loggingManager;
    
    public SearchLogsController(final LoggingManager loggingManager) {
        this.loggingManager = loggingManager;
    }

    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        
        final LogSearchRequest logSearchRequest = new LogSearchRequest();
        final ServletRequestDataBinder dataBinder = new ServletRequestDataBinder(logSearchRequest);
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH:mm"),false));
        
        dataBinder.bind(request);
        
        final List<LogRequest> logRequests = this.loggingManager.search(logSearchRequest);
        final ModelAndView modelAndView = new ModelAndView("logSearchResultsView");
        final List<String> eventTypes = new ArrayList<String>();
        
        eventTypes.add("AUTHENTICATION_SUCCESS");
        eventTypes.add("AUTHENTICATION_FAILURE");
        
        for (final TicketEventType t : TicketEventType.values()) {
            eventTypes.add(t.name());
        }
        
        modelAndView.addObject("pageTitle", "logSearchResultsView");
        modelAndView.addObject("logRequests", logRequests);
        modelAndView.addObject("logSearchRequest", logSearchRequest);
        modelAndView.addObject("eventTypes", eventTypes);
        
        return modelAndView;
    }
}
