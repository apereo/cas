/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.webservice.client;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.springframework.remoting.jaxrpc.JaxRpcPortProxyFactoryBean;

/**
 * Convenience extension of JaxrpcPorProxyFactoryBean that attempts to detect the
 * JavaBeans it needs to register by inspecting the service interface's return types
 * and parameter types for each method.
 * 
 * It is currently designed to ignore any java.* or javax.* class.  It also assumes
 * that the types are JavaBeans.  It does not actually check.
 * 
 * A more sophisticated version would be able to check if a class was a valid
 * JavaBean and only register valid JavaBeans.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class SimpleJavaBeanAxisPortProxyFactoryBean extends JaxRpcPortProxyFactoryBean {
	private String namespace;
	
	/**
	 * 
	 * @param namespace The namespace.
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	/**
	 * 
	 * @param mapping the TypeMapping we will be registering the beans with.
	 * @param type The JavaBean class that needs to be registered.
	 * @param name The name of the JavaBean class.
	 */
	protected void registerBeanMapping(TypeMapping mapping, Class type, String name) {
		QName qName = new QName(this.namespace, name);
		mapping.register(type, qName, new BeanSerializerFactory(type, qName), new BeanDeserializerFactory(type, qName));
	}
	
	/**
	 * 
	 * @see org.springframework.remoting.jaxrpc.JaxRpcPortClientInterceptor#postProcessJaxRpcService(javax.xml.rpc.Service)
	 */
	protected void postProcessJaxRpcService(Service service) {
		TypeMappingRegistry registry = service.getTypeMappingRegistry();
		TypeMapping mapping = registry.createTypeMapping();
		Class serviceInterface = this.getServiceInterface();
		Method[] methods = serviceInterface.getDeclaredMethods(); // TODO should be getMethods() ?
		
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			Class returnType = method.getReturnType();
			Class[] params = method.getParameterTypes();
			
			addJavaBeanToMap(mapping, returnType);
			
			for (int j = 0; j < params.length; j++) {
				addJavaBeanToMap(mapping, params[j]);
			}
		}
		registry.register("http://schemas.xmlsoap.org/soap/encoding/", mapping);
	}
	
	protected void addJavaBeanToMap(TypeMapping mapping, Class clazz) {
		String shortName = clazz.getName().substring(clazz.getName().lastIndexOf(".")+1);
		if (!clazz.getPackage().getName().startsWith("java.") && !clazz.getPackage().getName().startsWith("javax.")) {
			this.registerBeanMapping(mapping, clazz, shortName);
		}
	}
}
