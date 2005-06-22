/*
 * XML Type:  ProxySuccessType
 * Namespace: http://www.yale.edu/tp/cas
 * Java type: edu.yale.tp.cas.ProxySuccessType
 *
 * Automatically generated - do not modify.
 */
package edu.yale.tp.cas.impl;
/**
 * An XML ProxySuccessType(@http://www.yale.edu/tp/cas).
 *
 * This is a complex type.
 */
public class ProxySuccessTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements edu.yale.tp.cas.ProxySuccessType
{
    
    public ProxySuccessTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PROXYTICKET$0 = 
        new javax.xml.namespace.QName("http://www.yale.edu/tp/cas", "proxyTicket");
    
    
    /**
     * Gets the "proxyTicket" element
     */
    public java.lang.String getProxyTicket()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROXYTICKET$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "proxyTicket" element
     */
    public org.apache.xmlbeans.XmlString xgetProxyTicket()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PROXYTICKET$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "proxyTicket" element
     */
    public void setProxyTicket(java.lang.String proxyTicket)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROXYTICKET$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PROXYTICKET$0);
            }
            target.setStringValue(proxyTicket);
        }
    }
    
    /**
     * Sets (as xml) the "proxyTicket" element
     */
    public void xsetProxyTicket(org.apache.xmlbeans.XmlString proxyTicket)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PROXYTICKET$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(PROXYTICKET$0);
            }
            target.set(proxyTicket);
        }
    }
}
