/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.5
 */
public final class StatisticsController extends AbstractController {

    private final TicketRegistry ticketRegistry;

    public StatisticsController(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws Exception {
        final ModelAndView modelAndView = new ModelAndView("viewStatisticsView");
        modelAndView.addObject("totalMemory", Runtime.getRuntime().totalMemory() / 1024 / 1024);
        modelAndView.addObject("maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024);
        modelAndView.addObject("freeMemory", Runtime.getRuntime().freeMemory() / 1024 / 1024);
        modelAndView.addObject("availableProcessors", Runtime.getRuntime().availableProcessors());

        int unexpiredTgts = 0;
        int unexpiredSts = 0;
        int expiredTgts = 0;
        int expiredSts = 0;

        try {
            final Collection<Ticket> tickets = this.ticketRegistry.getTickets();

            for (final Ticket ticket : tickets) {
                if (ticket instanceof ServiceTicket) {
                    if (ticket.isExpired()) {
                        expiredSts++;
                    } else {
                        unexpiredSts++;
                    }
                } else {
                    if (ticket.isExpired()) {
                        expiredTgts++;
                    } else {
                        unexpiredTgts++;
                    }
                }
            }
        } catch (final UnsupportedOperationException e) {
            // this means the ticket registry doesn't support this information.
        }

        modelAndView.addObject("unexpiredTgts", unexpiredTgts);
        modelAndView.addObject("unexpiredSts", unexpiredSts);
        modelAndView.addObject("expiredTgts", expiredTgts);
        modelAndView.addObject("expiredSts", expiredSts);
        modelAndView.addObject("pageTitle", modelAndView.getViewName());

        return modelAndView;
    }
}
