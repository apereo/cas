/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.FatalBeanException;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class AnnotationBeanPostProcessorTests extends TestCase {
    
    private AbstractAnnotationBeanPostProcessor[] processors = new AbstractAnnotationBeanPostProcessor[] {new GreaterThanAnnotationBeanPostProcessor(), new IsInAnnotationBeanPostProcessor(), new NotEmptyAnnotationBeanPostProcessor(), new NotNullAnnotationBeanPostProcessor()}; 
    
    public void testFailed() {
        for (final AbstractAnnotationBeanPostProcessor processor : this.processors) {
            try {
                processor.postProcessBeforeInitialization(new BadTestClass(), "test");
                fail("processor: " + processor.getClass().getName() + "did not fail.");
            } catch (final FatalBeanException e) {
                System.out.println(e.getMessage());
            }
        }
        
        try {
            this.processors[2].postProcessBeforeInitialization(new BadNotEmptyCollection(), "test");
            fail("processor: " + this.processors[2].getClass().getName() + " did not fail.");
        } catch (final FatalBeanException e) {
            System.out.println(e.getMessage());
        }
        
        try {
            this.processors[2].postProcessBeforeInitialization(new BadNotEmptyArray(), "test");
            fail("processor: " + this.processors[2].getClass().getName() + " did not fail.");
        } catch (final FatalBeanException e) {
            System.out.println(e.getMessage());
        }
        
        try {
            this.processors[2].postProcessBeforeInitialization(new BadNotEmptyArray2(), "test");
            fail("processor: " + this.processors[2].getClass().getName() + " did not fail.");
        } catch (final FatalBeanException e) {
            System.out.println(e.getMessage());
        }
    }

    public void testPassed() {
        for (final AbstractAnnotationBeanPostProcessor processor : this.processors) {
            try {
                processor.postProcessBeforeInitialization(new GoodTestClass(), "test");
                
            } catch (final FatalBeanException e) {
                fail("processor: " + processor.getClass().getName() + "did fail:" + e.getMessage());
            }
        }
    }
    
    protected class BadNotEmptyCollection {
        @NotEmpty
        private Collection c =  new ArrayList();
    }

    protected class BadNotEmptyArray {
        @NotEmpty
        private Object[] os2 = null;
    }
    
    protected class BadNotEmptyArray2 {
        @NotEmpty
        private Object[] os2 = new Object[0];
    }

    protected class BadSuperTestClass {

        @NotNull
        private String testNull = null;
    }
    
    protected class BadTestClass extends BadSuperTestClass {
        
        @GreaterThan(0)
        @IsIn({0,1,2})
        private int test = -1;

        @NotNull
        private String testNull = null;

        
        @NotEmpty
        private Map map = new HashMap();

    }
protected class GoodTestClass {
        
        @GreaterThan(0)
        @IsIn({0,1,2})
        private int test = 1;
        
        @NotNull
        private String testNull = "haha";
        
        @NotEmpty
        private Collection<Object> c =  new ArrayList<Object>();
        
        @NotEmpty
        private Object[] os = new Object[] {new Object()};
        
        @NotEmpty
        private Map<String,String> map = new HashMap<String,String>();

        
        public GoodTestClass() {
            this.c.add(new Object());
            this.map.put("test", "test");
        }
        
    }
    
}
