/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.statistics;

/**
 * Extension to the Statistic class that not only holds on to the average, min, and max
 * but the current calculated value.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public class ComparisonStatistic extends Statistic {
    
    /** The current value of the statistic. */
    private long currentValue = 0;
    
    public final long getCurrentValue() {
        return this.currentValue;
    }

    public final void incrementCurrentValue() {
        this.currentValue++;
    }
}
