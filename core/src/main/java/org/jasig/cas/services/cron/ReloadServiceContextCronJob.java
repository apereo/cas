/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services.cron;

/**
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface ReloadServiceContextCronJob {

    void reloadServiceRegistry();
}
