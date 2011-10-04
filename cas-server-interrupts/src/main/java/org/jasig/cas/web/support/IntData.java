package org.jasig.cas.web.support;

public class IntData {
	private String[] fields = new String[100];
	
	public void setFields(String[] fields) { this.fields = fields; }
	public String[] getFields() { return this.fields; }
	public String getField(int pos) { return this.fields[pos]; }
}