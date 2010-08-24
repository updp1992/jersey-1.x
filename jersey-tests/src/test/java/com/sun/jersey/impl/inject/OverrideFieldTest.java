/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.impl.inject;

import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class OverrideFieldTest extends AbstractResourceTester {
    
    public OverrideFieldTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class FieldInjectResource {
        @QueryParam("x") String x = "DEFAULT";
        
        public FieldInjectResource() {
            assertEquals("DEFAULT", x);
        }

        @PostConstruct
        public void pc() {
            assertNotSame("DEFAULT", x);
        }

        @GET
        public String get() {
            return x;
        }
    }
    
    
    public void testFieldInjectResource() throws IOException {
        initiateWebApplication(FieldInjectResource.class);
        
        assertEquals("x", resource("/").queryParam("x", "x").get(String.class));
    }       
    
    @Path("/provider")
    public static class StringWriterResource {
        @GET
        public String get() { return "GET"; }
    }

    @Provider
    @Consumes({"text/plain", "*/*"})
    @Produces({"text/plain", "*/*"})
    public static class StringWriterField implements MessageBodyWriter<String> {

        UriInfo _ui = (UriInfo) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{UriInfo.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        @Context UriInfo ui = _ui;

        public StringWriterField() {
            assertNotNull(ui);
        }

        @PostConstruct
        public void pc() {
            assertNotNull(ui);
            assertNotSame(_ui, ui);
        }

        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == String.class;
        }

        public long getSize(String arg0, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return -1;
        }

        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            String s = arg0 + ui.getPath();
            arg6.write(s.getBytes());
        }

    }
    
    
    public void testProviderField() throws IOException {
        initiateWebApplication(StringWriterResource.class, StringWriterField.class);

        assertEquals("GETprovider", resource("/provider").get(String.class));
    }
    
 
}