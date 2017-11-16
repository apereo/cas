package org.apereo.cas;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * This is {@link DistributedCacheObject}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DistributedCacheObject<V extends Serializable> implements Serializable {
    private static final long serialVersionUID = -6776499291439952013L;

    private final long timestamp;
    private final V value;

    public DistributedCacheObject(final V value) {
        this(new Date().getTime(), value);
    }
    
    public DistributedCacheObject(final long timestamp, final V value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("timestamp", timestamp)
                .append("value", value)
                .toString();
    }
}
