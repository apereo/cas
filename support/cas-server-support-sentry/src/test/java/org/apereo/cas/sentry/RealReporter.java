package org.apereo.cas.sentry;

import org.apereo.cas.monitor.NotMonitorable;
import lombok.NoArgsConstructor;

/**
 * This is {@link RealReporter}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@NoArgsConstructor
@NotMonitorable
public class RealReporter implements Reporter {
    @Override
    public Object report() {
        return "RealReporter";
    }
}
