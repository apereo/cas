/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import java.util.List;


/**
 * 
 * Interface for a Proxy Ticket
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface ProxyTicket extends Ticket
{
	List getProxies();
}
