/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.client.validation;

import org.jasig.cas.client.receipt.CasReceipt;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface TicketValidator {
    CasReceipt validate(String ticketId);
}
