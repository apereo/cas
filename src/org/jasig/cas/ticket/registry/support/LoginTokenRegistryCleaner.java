/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry.support;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.util.RegistryCleaner;

/**
 * Class to determine if a loginToken is ready to be removed.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class LoginTokenRegistryCleaner implements RegistryCleaner {

    protected final Log log = LogFactory.getLog(getClass());

    private Map loginTokens;

    private long timeOut;

    /**
     * @see org.jasig.cas.util.RegistryCleaner#clean()
     */
    public void clean() {
        final long currentTime = System.currentTimeMillis();
        log.info("Started cleaning up login tokens at [" + new Date() + "]");
        synchronized (this.loginTokens) {
            final Set keys = this.loginTokens.keySet();

            for (Iterator iter = keys.iterator(); iter.hasNext();) {
                final String key = (String)iter.next();
                final Date lastUsed = (Date)loginTokens.get(key);

                if ((lastUsed.getTime() - currentTime) > this.timeOut)
                    this.loginTokens.remove(key);
            }
        }
        log.info("Finished cleaning up login tokens at [" + new Date() + "]");
    }

    /**
     * @param loginTokens The loginTokens to set.
     */
    public void setLoginTokens(Map loginTokens) {
        this.loginTokens = loginTokens;
    }

    /**
     * @param timeOut The timeOut to set.
     */
    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }
}
