package org.jasig.cas.adaptors.ldap.lppe;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLdapDateConverter implements LdapDateConverter {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private DateTimeZone timeZone = DateTimeZone.UTC;
    
    public void setTimeZone(final DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public DateTimeZone getTimeZone() {
       return this.timeZone;
    }
}


