/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.weld.bean.proxy;

import javassist.util.proxy.MethodHandler;
import org.jboss.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.exceptions.WeldException;
import org.slf4j.cal10n.LocLogger;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.BEAN_INSTANCE_NOT_SET_ON_PROXY;

/**
 * A general purpose MethodHandler for all proxies which routes calls to the
 * {@link org.jboss.weld.bean.proxy.BeanInstance} associated with this proxy or handler.
 *
 * @author David Allen
 *
 */
public class SubclassingMethodHandler implements MethodHandler, Serializable
{

   private static final long serialVersionUID = 5293834510764991583L;

   // The log provider
   protected static final LocLogger log = loggerFactory().getLogger(BEAN);


   /* (non-Javadoc)
    * @see javassist.util.proxy.MethodHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.reflect.Method, java.lang.Object[])
    */
   public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable
   {
      if (thisMethod == null)
      {
         log.trace("MethodHandler processing returning bean instance for " + self.getClass());
         return self;
      }
      log.trace("MethodHandler processing call to " + thisMethod + " for " + self.getClass());
      if (thisMethod.getName().equals("writeReplace"))
      {
         return new org.jboss.weld.bean.proxy.util.SerializableProxy(self);
      }
      else if (thisMethod.getName().equals("_initMH"))
      {
         log.trace("Setting new MethodHandler with bean instance for " + args[0] + " on " + self.getClass());
         return new SubclassingMethodHandler();
      }
      else
      {
         Object instance = this;
         Object result = null;
         try
         {
            result = proceed.invoke(self, args);
         }
         catch (InvocationTargetException e)
         {
            throw e.getCause();
         }
         // if the method returns this return the proxy instead
         // to prevent the bean instance escaping
         if (result == instance)
         {
            return self;
         }
         return result;
      }
   }


}