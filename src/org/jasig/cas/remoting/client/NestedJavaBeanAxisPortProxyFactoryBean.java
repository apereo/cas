/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.remoting.client;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.springframework.remoting.jaxrpc.JaxRpcPortProxyFactoryBean;

/**
 * Convenience extension of JaxrpcPorProxyFactoryBean that attempts to detect the JavaBeans it needs to register by inspecting the service interface's
 * return types and parameter types for each method. Nested version attempts to find beans nested within JavaBeans and register them also. It is
 * currently designed to ignore any java.* or javax.* class. It also assumes that the types are JavaBeans. It does not actually check. A more
 * sophisticated version would be able to check if a class was a valid JavaBean and only register valid JavaBeans.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class NestedJavaBeanAxisPortProxyFactoryBean extends JaxRpcPortProxyFactoryBean {

    private String namespace;
    
    private static final String PACKAGE_NAME_JAVA = "java.";

    private static final String PACKAGE_NAME_JAVAX = "javax.";
    
    private List beans = new ArrayList();

    protected void registerBeans(TypeMapping mapping, List registeredBeans, Class clazz) {
        if (registeredBeans.contains(clazz) || clazz.getName().startsWith(PACKAGE_NAME_JAVA) || clazz.getName().startsWith(PACKAGE_NAME_JAVAX))
            return;

        if (!clazz.equals(this.getServiceInterface())) {
            registeredBeans.add(clazz);
            addJavaBeanToMap(mapping, clazz);
        }

        Method[] methods = clazz.getDeclaredMethods(); // TODO getDeclaredMethods or getMethods ??

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Class returnType = method.getReturnType();
            Class[] params = method.getParameterTypes();

            registerBeans(mapping, registeredBeans, returnType);

            for (int j = 0; j < params.length; j++) {
                registerBeans(mapping, registeredBeans, params[j]);
            }
        }
    }

    /**
     * @see org.springframework.remoting.jaxrpc.JaxRpcPortClientInterceptor#postProcessJaxRpcService(javax.xml.rpc.Service)
     */
    protected void postProcessJaxRpcService(Service service) {
        final TypeMappingRegistry registry = service.getTypeMappingRegistry();
        final TypeMapping mapping = registry.createTypeMapping();
        final Class serviceInterface = this.getServiceInterface();

        registerBeans(mapping, new ArrayList(), serviceInterface);
        
        for (Iterator iter = beans.iterator(); iter.hasNext();) {
            final String bean = (String) iter.next();
            try {
                final Class clazz = Class.forName(bean);
                this.addJavaBeanToMap(mapping, clazz);
            } catch (Exception e) {
                throw new IllegalArgumentException("bean of class " + bean + "not found.");
            }
            
        }

        registry.register("http://schemas.xmlsoap.org/soap/encoding/", mapping);
    }

    protected void addJavaBeanToMap(TypeMapping mapping, Class clazz) {
        String name = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
        QName qName = new QName(this.namespace, name);
        mapping.register(clazz, qName, new BeanSerializerFactory(clazz, qName), new BeanDeserializerFactory(clazz, qName));
    }

    /**
     * @param namespace The namespace.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public void setJavaBeans(List beans) {
        this.beans = beans;
    }
}
