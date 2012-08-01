/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action to expire web sessions in some seconds. The goal is to decrease memory consumption by deleting as soon as possible the web
 * sessions created mainly for login process. This action is triggered on redirect step in webflow.
 * 
 * @author Jerome Leleu
 * @since 3.5.1
 */
public final class DeleteSessionAction extends AbstractAction {
    
    private int timeToDieInSeconds = 2;
    
    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        
        HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        // get session but don't create it if it doesn't already exist
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // set the session to die in timeToDieInSeconds
            session.setMaxInactiveInterval(this.timeToDieInSeconds);
        }
        
        // return null event not to trigger transition in webflow
        return null;
    }
    
    public int getTimeToDieInSeconds() {
        return this.timeToDieInSeconds;
    }
    
    public void setTimeToDieInSeconds(int timeToDieInSeconds) {
        this.timeToDieInSeconds = timeToDieInSeconds;
    }
}
