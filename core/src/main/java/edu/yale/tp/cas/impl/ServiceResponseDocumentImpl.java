/*
 * An XML document type.
 * Localname: serviceResponse
 * Namespace: http://www.yale.edu/tp/cas
 * Java type: edu.yale.tp.cas.ServiceResponseDocument
 *
 * Automatically generated - do not modify.
 */
package edu.yale.tp.cas.impl;
/**
 * A document containing one serviceResponse(@http://www.yale.edu/tp/cas) element.
 *
 * This is a complex type.
 */
public class ServiceResponseDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements edu.yale.tp.cas.ServiceResponseDocument
{
    
    public ServiceResponseDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SERVICERESPONSE$0 = 
        new javax.xml.namespace.QName("http://www.yale.edu/tp/cas", "serviceResponse");
    
    
    /**
     * Gets the "serviceResponse" element
     */
    public edu.yale.tp.cas.ServiceResponseType getServiceResponse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ServiceResponseType target = null;
            target = (edu.yale.tp.cas.ServiceResponseType)get_store().find_element_user(SERVICERESPONSE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "serviceResponse" element
     */
    public void setServiceResponse(edu.yale.tp.cas.ServiceResponseType serviceResponse)
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ServiceResponseType target = null;
            target = (edu.yale.tp.cas.ServiceResponseType)get_store().find_element_user(SERVICERESPONSE$0, 0);
            if (target == null)
            {
                target = (edu.yale.tp.cas.ServiceResponseType)get_store().add_element_user(SERVICERESPONSE$0);
            }
            target.set(serviceResponse);
        }
    }
    
    /**
     * Appends and returns a new empty "serviceResponse" element
     */
    public edu.yale.tp.cas.ServiceResponseType addNewServiceResponse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ServiceResponseType target = null;
            target = (edu.yale.tp.cas.ServiceResponseType)get_store().add_element_user(SERVICERESPONSE$0);
            return target;
        }
    }
}
