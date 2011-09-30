package org.jasig.cas.web.flow;

import org.jasig.cas.web.support.AuthHandlerAction;

import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.Message;
import org.springframework.binding.message.Severity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class HandleAuthError {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private List<AuthHandlerAction> authHandlerActions;
	
	public String check(final MessageContext messageContext) throws Exception {
    	Message[] myMessages = messageContext.getAllMessages();
    	for (int i=0;i < myMessages.length;i++){
    		if (myMessages[i].getSeverity() == Severity.ERROR){
    			this.log.info ("Authentication Error Thown: " + myMessages[i].getText());
    			/*
    			 *  Handle the password warning exceptions
    			 */
    			for (AuthHandlerAction authHandlerAction : authHandlerActions){
    				if (myMessages[i].getText() == authHandlerAction.errMessage){
    					return authHandlerAction.returnState;
    				}
    			}
    		}
		}
    	return "error";
	}
	
	public void setAuthHandlerActions(List<AuthHandlerAction> authHandlerActions){
		this.authHandlerActions=authHandlerActions;
	}
}
