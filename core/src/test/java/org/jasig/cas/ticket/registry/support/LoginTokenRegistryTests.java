/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry.support;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id: LoginTokenRegistryTests.java,v 1.2 2005/02/19 23:42:36
 * sbattaglia Exp $
 */
public class LoginTokenRegistryTests extends TestCase {

    private LoginTokenRegistryCleaner loginTokenRegistryCleaner = new LoginTokenRegistryCleaner();

    private Map loginTokens;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.loginTokens = new HashMap();
        this.loginTokenRegistryCleaner.setLoginTokens(this.loginTokens);
    }

    public void testCleanEmptyLoginTokens() {
        this.loginTokenRegistryCleaner.clean();
        assertTrue(this.loginTokens.isEmpty());

    }

    public void testCleanAllExpiredLoginTokens() {
        this.loginTokenRegistryCleaner.setTimeOut(5);

        for (int i = 0; i < 10; i++)
            this.loginTokens.put("test" + i, new Date());
        try {
            Thread.sleep(15);
        }
        catch (InterruptedException e) {
            fail("InterruptedException caught");
        }

        this.loginTokenRegistryCleaner.clean();

        assertTrue(this.loginTokens.isEmpty());
    }

    public void testCleanNoLoginTokens() {
        this.loginTokenRegistryCleaner.setTimeOut(50);

        for (int i = 0; i < 10; i++)
            this.loginTokens.put("test" + i, new Date());
        try {
            Thread.sleep(30);
        }
        catch (InterruptedException e) {
            fail("InterruptedException caught");
        }

        this.loginTokenRegistryCleaner.clean();

        assertEquals(10, this.loginTokens.size());
    }

    public void testCleanSomeLoginTokens() {
        this.loginTokenRegistryCleaner.setTimeOut(50);

        for (int i = 0; i < 10; i++)
            this.loginTokens.put("test" + i, new Date());

        try {
            Thread.sleep(60);
        }
        catch (InterruptedException e) {
            fail("InterruptedException caught");
        }

        for (int i = 10; i < 20; i++)
            this.loginTokens.put("test" + i, new Date());

        this.loginTokenRegistryCleaner.clean();

        assertEquals(this.loginTokens.size(), 10);
    }
}