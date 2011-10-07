package org.jasig.cas.web.support;

import java.io.Serializable;

public class IntData implements Serializable {
	
	private String[] fields = new String[100];
	
	public void setFields(String[] fields) { this.fields = fields; }
	public String[] getFields() { return this.fields; }
	public String getField(int pos) { return this.fields[pos]; }
}