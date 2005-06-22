/*
 * An XML document type.
 * Localname: serviceResponse
 * Namespace: http://www.yale.edu/tp/cas
 * Java type: edu.yale.tp.cas.ServiceResponseDocument
 *
 * Automatically generated - do not modify.
 */
package edu.yale.tp.cas;


/**
 * A document containing one serviceResponse(@http://www.yale.edu/tp/cas) element.
 *
 * This is a complex type.
 */
public interface ServiceResponseDocument extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)schema.system.sB9D3B5FB07C1B153996203ECA5FC9DDC.TypeSystemHolder.typeSystem.resolveHandle("serviceresponse67c2doctype");
    
    /**
     * Gets the "serviceResponse" element
     */
    edu.yale.tp.cas.ServiceResponseType getServiceResponse();
    
    /**
     * Sets the "serviceResponse" element
     */
    void setServiceResponse(edu.yale.tp.cas.ServiceResponseType serviceResponse);
    
    /**
     * Appends and returns a new empty "serviceResponse" element
     */
    edu.yale.tp.cas.ServiceResponseType addNewServiceResponse();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static edu.yale.tp.cas.ServiceResponseDocument newInstance() {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseDocument parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (edu.yale.tp.cas.ServiceResponseDocument) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
