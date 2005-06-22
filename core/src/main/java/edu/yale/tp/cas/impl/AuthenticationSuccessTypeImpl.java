/*
 * XML Type:  AuthenticationSuccessType
 * Namespace: http://www.yale.edu/tp/cas
 * Java type: edu.yale.tp.cas.AuthenticationSuccessType
 *
 * Automatically generated - do not modify.
 */
package edu.yale.tp.cas.impl;
/**
 * An XML AuthenticationSuccessType(@http://www.yale.edu/tp/cas).
 *
 * This is a complex type.
 */
public class AuthenticationSuccessTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements edu.yale.tp.cas.AuthenticationSuccessType
{
    
    public AuthenticationSuccessTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName USER$0 = 
        new javax.xml.namespace.QName("http://www.yale.edu/tp/cas", "user");
    private static final javax.xml.namespace.QName PROXYGRANTINGTICKET$2 = 
        new javax.xml.namespace.QName("http://www.yale.edu/tp/cas", "proxyGrantingTicket");
    private static final javax.xml.namespace.QName PROXIES$4 = 
        new javax.xml.namespace.QName("http://www.yale.edu/tp/cas", "proxies");
    
    
    /**
     * Gets the "user" element
     */
    public java.lang.String getUser()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "user" element
     */
    public org.apache.xmlbeans.XmlString xgetUser()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(USER$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "user" element
     */
    public void setUser(java.lang.String user)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USER$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(USER$0);
            }
            target.setStringValue(user);
        }
    }
    
    /**
     * Sets (as xml) the "user" element
     */
    public void xsetUser(org.apache.xmlbeans.XmlString user)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(USER$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(USER$0);
            }
            target.set(user);
        }
    }
    
    /**
     * Gets the "proxyGrantingTicket" element
     */
    public java.lang.String getProxyGrantingTicket()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROXYGRANTINGTICKET$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "proxyGrantingTicket" element
     */
    public org.apache.xmlbeans.XmlString xgetProxyGrantingTicket()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PROXYGRANTINGTICKET$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "proxyGrantingTicket" element
     */
    public boolean isSetProxyGrantingTicket()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PROXYGRANTINGTICKET$2) != 0;
        }
    }
    
    /**
     * Sets the "proxyGrantingTicket" element
     */
    public void setProxyGrantingTicket(java.lang.String proxyGrantingTicket)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROXYGRANTINGTICKET$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PROXYGRANTINGTICKET$2);
            }
            target.setStringValue(proxyGrantingTicket);
        }
    }
    
    /**
     * Sets (as xml) the "proxyGrantingTicket" element
     */
    public void xsetProxyGrantingTicket(org.apache.xmlbeans.XmlString proxyGrantingTicket)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PROXYGRANTINGTICKET$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(PROXYGRANTINGTICKET$2);
            }
            target.set(proxyGrantingTicket);
        }
    }
    
    /**
     * Unsets the "proxyGrantingTicket" element
     */
    public void unsetProxyGrantingTicket()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PROXYGRANTINGTICKET$2, 0);
        }
    }
    
    /**
     * Gets the "proxies" element
     */
    public edu.yale.tp.cas.ProxiesType getProxies()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ProxiesType target = null;
            target = (edu.yale.tp.cas.ProxiesType)get_store().find_element_user(PROXIES$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "proxies" element
     */
    public boolean isSetProxies()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PROXIES$4) != 0;
        }
    }
    
    /**
     * Sets the "proxies" element
     */
    public void setProxies(edu.yale.tp.cas.ProxiesType proxies)
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ProxiesType target = null;
            target = (edu.yale.tp.cas.ProxiesType)get_store().find_element_user(PROXIES$4, 0);
            if (target == null)
            {
                target = (edu.yale.tp.cas.ProxiesType)get_store().add_element_user(PROXIES$4);
            }
            target.set(proxies);
        }
    }
    
    /**
     * Appends and returns a new empty "proxies" element
     */
    public edu.yale.tp.cas.ProxiesType addNewProxies()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ProxiesType target = null;
            target = (edu.yale.tp.cas.ProxiesType)get_store().add_element_user(PROXIES$4);
            return target;
        }
    }
    
    /**
     * Unsets the "proxies" element
     */
    public void unsetProxies()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PROXIES$4, 0);
        }
    }
}
