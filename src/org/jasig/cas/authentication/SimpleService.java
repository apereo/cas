package org.jasig.cas.authentication;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class SimpleService implements Service {
	private String id;

	public SimpleService(String id) {
        
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");

		this.id = id;
	}

	/**
	 * @see org.jasig.cas.authentication.Service#getName()
	 */
	public String getId() {
		return id;
	}
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        
        if (!(o instanceof SimpleService))
            return false;
        
        SimpleService s = (SimpleService) o;

        return this.getId().equals(s.getId());
    }
    
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
