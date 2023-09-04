package org.apereo.cas.adaptors.authy.core.okta.custommodels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Links{

	@JsonProperty("cancel")
	private Cancel cancel;

	@JsonProperty("poll")
	private Poll poll;

	public void setCancel(Cancel cancel){
		this.cancel = cancel;
	}

	public Cancel getCancel(){
		return cancel;
	}

	public void setPoll(Poll poll){
		this.poll = poll;
	}

	public Poll getPoll(){
		return poll;
	}

	@Override
 	public String toString(){
		return 
			"Links{" + 
			"cancel = '" + cancel + '\'' + 
			",poll = '" + poll + '\'' + 
			"}";
		}
}