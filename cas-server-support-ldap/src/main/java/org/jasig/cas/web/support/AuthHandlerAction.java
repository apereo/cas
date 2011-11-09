package org.jasig.cas.web.support;

public class AuthHandlerAction {
	
	public String errMessage;
	
	public String returnState;
	
	public void setErrMessage(String errMessage){
		this.errMessage=errMessage;
	}
	
	public void setReturnState(String returnState){
		this.returnState=returnState;
	}

}
