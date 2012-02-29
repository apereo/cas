/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */

package org.jasig.cas.monitor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link MemoryMonitor} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class MemoryMonitorTests {

    @Test
    public void testObserveOk() throws Exception {
        assertEquals(StatusCode.OK, new MemoryMonitor().observe().getCode());
    }

    @Test
    public void testObserveWarn() throws Exception {
        final MemoryMonitor monitor = new MemoryMonitor();
        monitor.setFreeMemoryWarnThreshold(100);
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }
}
