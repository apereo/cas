package org.apereo.cas.util;

import org.ldaptive.Connection;
import org.ldaptive.pool.Activator;
/*
  We need to specify out own activator, based on ConnectActivator from ldaptive, 
  because ConnectActivator does not check whether the connection is already open, 
  which can cause an exception (when opening an open connection)
  Feel free to move this class somewhere else...
*/
  
    public class OpenActivator implements Activator<Connection> {
        
        public boolean activate(Connection c) {
            boolean success = false;
            if (c != null && !c.isOpen()) {
                try {
                    c.open();
                    success = true;
                } catch (Exception e) {
                    LOGGER.error("unable to connect to the ldap", e);
                }
            }

            return success;
        }
    }                        
