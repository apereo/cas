package org.jasig.cas;

/**
 * Simple mutex class for synchronizing actions.
 * @author tduehr
 * @since 4.3
 */
public final class SimpleMutex {
    private String id;

    /**
     * Creates a synchronization handle.
     * @param id String mutex id
     */
    public SimpleMutex(final String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    /**
     * Checks equality via +id+.
     * @param o Object other
     * @return boolean
     */
    public boolean equals(final Object o) {
        if (o==null) {
            return false;
        } else if (this.getClass() == o.getClass()) {
            return this.id.equals(o.toString());
        } else {
            return false;
        }
    }

    /**
     * Returns id.hashCode().
     * @return id
     */
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns +id+.
     * @return id
     */
    public String toString() {
        return id;
    }
}
