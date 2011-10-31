/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.ldap;

import org.jasig.cas.authentication.AbstractPasswordWarningCheck;
import org.jasig.cas.util.LdapUtils;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.util.Assert;

import javax.management.timer.Timer;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Class that fetches an password expiration date from an LDAP database.
 * Based on AccountStatusGetter by Bart Ophelders & Johan Peeters
 * 
 * @author Eric Pierce
 * @version 1.3 12/14/2009 11:47:37
 * 
 */
public class LdapPasswordWarningCheck extends AbstractPasswordWarningCheck {

	private static final String CHECK_TYPE_PWDCHANGE = "change";
	private static final String CHECK_TYPE_PWDEXPIRE = "expire";
	
	/** Time Segments for AD Password age **/
	private long INTERVALS_PER_MILLISECOND = 10000;
	
	/** Number of milliseconds between 1/1/1601 and 1/1/1970 **/
	private long MILLISECONDS_BETWEEN_1601_AND_1970 = Timer.ONE_DAY * 134775;  
		
	/** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 10;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;

    /** The list of valid scope values. */
    private static final int[] VALID_SCOPE_VALUES = new int[] {
        SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE,
        SearchControls.SUBTREE_SCOPE};

    /** LdapTemplate to execute ldap queries. */
    private LdapTemplate ldapTemplate;

    /** The filter path to the lookup value of the user. */
    private String filter;

    /** Disregard WarnPeriod and warn all users of password expiration */
    protected Boolean WarnAll;
    
    /** Calculate password warning from last password change time or expiration date */
    protected String WarningCheckType;

    /** The attribute that contains the date the password will expire or last password change */
    protected String DateAttribute;
    
    /** The attribute that contains the user's warning days */
    protected String WarningDaysAttribute;

    /** The attribute that contains the number of days the user's password is valid */
    protected String ValidDaysAttribute;
    
    /** The format of the date in DateAttribute */
    protected String dateFormat;

    /** default number of days that a warning message will be displayed */
    private int warningDays;
    
    /** default number of days a password is valid */
    private int validDays;
    
    /** The attribute that contains the data that will determine if password warning is skipped  */
    private String noWarnAttribute;
    
    /** The value that will cause password warning to be bypassed  */
    private List<String> noWarnValues;

    /** The search base to find the user under. */
    private String searchBase;

    /** The scope. */
    private int scope = SearchControls.SUBTREE_SCOPE;
    
    /** Whether the LdapTemplate should ignore partial results. */
    private boolean ignorePartialResultException = false;

    /** The maximum number of results to return. */
    private int maxNumberResults = DEFAULT_MAX_NUMBER_OF_RESULTS;

    /** The amount of time to wait. */
    private int timeout = DEFAULT_TIMEOUT;

    private String[] attributeIds;
    
    public int getPasswordWarning(String userID) {

    	//Create a list of LDAP attributes we need to search for
    	List<String> attributeList = new ArrayList<String>();
    	attributeList.add(this.DateAttribute);
    	if (this.WarningDaysAttribute != null) {
    		attributeList.add(this.WarningDaysAttribute);
    	}
    	if (this.ValidDaysAttribute != null) {
    		attributeList.add(this.ValidDaysAttribute);
    	}
    	if (this.noWarnAttribute != null) {
    		attributeList.add(this.noWarnAttribute);
    	}
    	
    	//Convert the list to a string array
    	this.attributeIds = new String[attributeList.size()];
    	attributeList.toArray(this.attributeIds);
    	
        LdapResult ldapResult = getResultsFromLDAP(userID);

        if (ldapResult == null) {
            this.logger.warn("No LDAP entry for " + userID);
            return STATUS_ERROR;
        }  
        
        String WarnDaysResult = ldapResult.getWarnDaysResult();
        String ValidDaysResult = ldapResult.getValidDaysResult();
        String WarnAttribute = ldapResult.getNoWarnAttributeResult();
        
        this.logger.debug("No Warn flag is set to: " + WarnAttribute);
        
        if (this.noWarnAttribute != null) {
        	if (this.noWarnValues.contains(WarnAttribute)){
        		 this.logger.info("No Warn flag is set, bypassing warning check");
        		 return STATUS_PASS;
        	}
        }
        
        if (WarnDaysResult == null) {
            this.logger.debug("No Warning Days found for " + userID + " using system default of " + warningDays);
        } else {
            warningDays = Integer.parseInt(WarnDaysResult);          
        }
        
        //Convert number of days to number of seconds 
        long CurrentTime = (new Date()).getTime();
        long ExpireTime = CurrentTime;
        
        //Use the last password change date for expiration calculations
        if (this.WarningCheckType.equals(CHECK_TYPE_PWDCHANGE)) {  
        	String ChangeDateResult = ldapResult.getDateResult();
        	this.logger.debug("Calculating warning period from last change date");
        	this.logger.debug("WarnDays Result='" + WarnDaysResult + "'");
        	this.logger.debug("ChangeDate Result='" + ChangeDateResult + "'");
        	this.logger.debug("ValidDays Result='" + ValidDaysResult + "'");
        	if (ChangeDateResult == null) {
                this.logger.warn("No password change date for " + userID);
                return STATUS_ERROR;
            }
        	
        	
        	GregorianCalendar ChangeDate = new GregorianCalendar();
        	if (dateFormat.equals("ActiveDirectory")) {
	        	ChangeDate = convertDateAD(ChangeDateResult);
	        } else {
	        	ChangeDate = convertDate(ChangeDateResult, dateFormat);
	            if (ChangeDate == null) {
	            	this.logger.warn("Returning error, last password change date is invalid");
	            	return STATUS_ERROR;
	            }
	        }
            
            if (ValidDaysResult == null) {
            	this.logger.debug("No maximum password age found for " + userID + ".  Using system default of " + validDays + " days");
            } else {
                validDays = Integer.parseInt(ValidDaysResult);
                
            }	
            
            //Password expiration is the time the password was last changed plus the maximum password age
            long ValidPeriod = validDays * Timer.ONE_DAY;
        	long ChangeTime = (ChangeDate.getTime()).getTime();
        	ExpireTime = ChangeTime + ValidPeriod;
        	
        //The expiration date is stored in LDAP	
        } else if (this.WarningCheckType.equals(CHECK_TYPE_PWDEXPIRE)) {
        	String ExpireDateResult = ldapResult.getDateResult();
        	this.logger.debug("Calculating warning period from expiration date");
        	this.logger.debug("WarnDays Result='" + WarnDaysResult + "'");
        	this.logger.debug("ExpireDate Result='" + ExpireDateResult + "'");
        	if (ExpireDateResult == null) {
                this.logger.warn("No Expiration date for " + userID);
                return STATUS_ERROR;
            }
        	
        	GregorianCalendar ExpireDate = convertDate(ExpireDateResult, dateFormat);
            if (ExpireDate == null) {
                this.logger.warn("Returning error, expiration date is invalid");
                return STATUS_ERROR;
            }
            
        	ExpireTime = (ExpireDate.getTime()).getTime();
        	
        } else {
        	this.logger.warn("Invalid value for 'warningCheckType': " + this.WarningCheckType);
            return STATUS_ERROR;
        }

        float DateDiff = ExpireTime - CurrentTime;
        float WarnPeriod = warningDays * Timer.ONE_DAY;

        this.logger.debug("Current Time=" + CurrentTime / 1000);        
        this.logger.debug("Expiration Time=" + ExpireTime / 1000);
        this.logger.debug("Difference in seconds=" + DateDiff);
        this.logger.debug("Warn Period in seconds=" + WarnPeriod);
 
        //The LDAP server should have thrown an error because the password has already expired
        if (DateDiff < 0) {
            this.logger.warn("Expiration Date has passed for " + userID + " but authentication succeeded!");
            return STATUS_ERROR;
        }
      
        //WarnAll is true -- warn everyone
        if (WarnAll.equals(true)) {
     	   this.logger.info("Show Warning (WarnALL is TRUE!) -- The password for " + userID + " will expire in " + Math.round(DateDiff / Timer.ONE_DAY) + " days");
     	   return (int) Math.round(DateDiff / Timer.ONE_DAY);
        }
        
       int comp = Float.compare(DateDiff, (float) WarnPeriod );
       if (comp > 0) {
    	   this.logger.debug("Skip Warning -- The password for " + userID + " will expire in " + Math.round(DateDiff / Timer.ONE_DAY) + " days");
    	   return STATUS_PASS;
       } else {
    	   this.logger.info("Show Warning -- The password for " + userID + " will expire in " + Math.round(DateDiff / Timer.ONE_DAY) + " days");
    	   return (int) Math.round(DateDiff / Timer.ONE_DAY);	   
       }

    }
    
    private GregorianCalendar convertDateAD(String pwdLastSet) {
    	long l = Long.parseLong(pwdLastSet.trim());
    	long t= l / INTERVALS_PER_MILLISECOND  ;
        this.logger.debug("pwdLastSet= " + t + " millisec since 1601");

        t-= MILLISECONDS_BETWEEN_1601_AND_1970;
        this.logger.debug("pwdLastSet= " + t + " millisec since 1970");
  
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(t);
        this.logger.debug("pwdLastSet= " + calendar.getTime().toString() );
        return calendar;
      }

    private GregorianCalendar convertDate(String ldapResult, String dateFormat) {
        DateFormat format = new SimpleDateFormat(dateFormat);
        format.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
        format.setLenient(false);
        Date date = null;
        try {
            date = format.parse(ldapResult);
        } catch (ParseException e) {
            this.logger.warn("The attribute'" + DateAttribute
                + "' with value '" + ldapResult
                + "' should contain a date in the format " + dateFormat);
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        return calendar;
    }

    private SearchControls getSearchControls(String[] attributeIds) {
        final SearchControls constraints = new SearchControls();

        constraints.setSearchScope(this.scope);
        constraints.setReturningAttributes(this.attributeIds);
        constraints.setTimeLimit(this.timeout);
        constraints.setCountLimit(this.maxNumberResults);

        return constraints;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.ldapTemplate, "ldapTemplate cannot be null");
        Assert.notNull(this.filter, "filter cannot be null");
        Assert.notNull(this.searchBase, "searchBase cannot be null");
        Assert.notNull(this.WarningCheckType, "warningCheckType cannot be null");
        Assert.notNull(this.WarnAll, "warnAll cannot be null");
        Assert.notNull(this.DateAttribute, "dateAttribute cannot be null");
        Assert.notNull(this.dateFormat, "dateFormat cannot be null");
        Assert.notNull(this.warningDays, "warningDays cannot be null");
        Assert.isTrue(this.filter.contains("%u"), "filter must contain %u");
        this.ldapTemplate.setIgnorePartialResultException(this.ignorePartialResultException);

        for (int i = 0; i < VALID_SCOPE_VALUES.length; i++) {
            if (this.scope == VALID_SCOPE_VALUES[i]) {
                return;
            }
        }
        throw new IllegalStateException("You must set a valid scope.");
    }

    private LdapResult getResultsFromLDAP(final String userID) {
        final String searchFilter = LdapUtils.getFilterWithValues(this.filter,
            userID);

        this.logger.debug("LDAP: starting search with searchFilter '" + searchFilter + "'");
        String attributeListLog = this.attributeIds[0];
        for(int i=1;i<this.attributeIds.length;i++) {
        	attributeListLog = attributeListLog.concat(":" + this.attributeIds[i]);	
    	}
        this.logger.debug("LDAP: Returning attributes '" + attributeListLog + "'");
        
        try {
            // searching the directory

            AttributesMapper mapper = new AttributesMapper(){

                public Object mapFromAttributes(Attributes attrs)
                    throws NamingException {
                    LdapResult result = new LdapResult();

                    if (LdapPasswordWarningCheck.this.DateAttribute != null){
	                    if (attrs.get(LdapPasswordWarningCheck.this.DateAttribute) != null) {
	                        String date = (String) attrs.get(LdapPasswordWarningCheck.this.DateAttribute).get();
	                        result.setDateResult(date);
	                    }
                    }
                    
                    if (LdapPasswordWarningCheck.this.WarningDaysAttribute != null){
	                    if (attrs.get(LdapPasswordWarningCheck.this.WarningDaysAttribute) != null) {
	                        String warn = (String) attrs.get(LdapPasswordWarningCheck.this.WarningDaysAttribute).get();
	                        result.setWarnDaysResult(warn);
	                    }
                    }
                    
                    if (LdapPasswordWarningCheck.this.noWarnAttribute != null){
	                    if (attrs.get(LdapPasswordWarningCheck.this.noWarnAttribute) != null) {
	                        String Attrib = (String) attrs.get(LdapPasswordWarningCheck.this.noWarnAttribute).get();
	                        result.setNoWarnAttributeResult(Attrib);
	                    }
                    }
                    
                    if (WarningCheckType.equals(CHECK_TYPE_PWDCHANGE)) { 
                    	if (attrs.get(LdapPasswordWarningCheck.this.ValidDaysAttribute) != null) {
                    		String valid = (String) attrs.get(LdapPasswordWarningCheck.this.ValidDaysAttribute).get();
                    		result.setValidDaysResult(valid);
                    	}
                    }

                    return result;
                }
            };

            List<?> LdapResultList = this.ldapTemplate.search(this.searchBase,searchFilter, getSearchControls(this.attributeIds), mapper);

            return (LdapResult) LdapResultList.get(0);

        } catch (Exception e) {
            logger.error("Error searching the directory",e);
            return null;
        }
    }

    /**
     * Method to set the datasource and generate a LDAPTemplate.
     * 
     * @param dataSource the datasource to use.
     */
    public final void setContextSource(final ContextSource contextSource) {
        this.ldapTemplate = new LdapTemplate(contextSource);
    }

    /**
     * @param filter The LDAP filter to set.
     */
    public void setFilter(final String filter) {
        this.filter = filter;
        this.logger.info("Search Filter: '" + filter + "'");
    }
    
    /**
     * @param warnAll Disregard warningPeriod and warn all users of password expiration.
     */
    public final void setWarnAll(Boolean warnAll) {
        this.WarnAll = warnAll;
        this.logger.info("warnAll: '" + warnAll + "'");
    }

    /**
     * @param warningCheckType The warningCheckType to set.
     */
    public final void setWarningCheckType(String WarningCheckType) {
        this.WarningCheckType = WarningCheckType;
        this.logger.info("warningCheckType: '" + WarningCheckType + "'");
    }
    
    /**
     * @param DateAttribute The DateAttribute to set.
     */
    public final void setDateAttribute(String DateAttribute) {
        this.DateAttribute = DateAttribute;
        this.logger.info("Date Attribute: '" + DateAttribute + "'");
    }
	
    /**
     * @param WarningDaysAttribute The WarningDaysAttribute to set.
     */
    public final void setWarningDaysAttribute(String WarningDaysAttribute) {
        this.WarningDaysAttribute = WarningDaysAttribute;
        this.logger.info("Warning Days Attribute: '" + WarningDaysAttribute + "'");
    }
    
    /**
     * @param ValidDaysAttribute The ValidDaysAttribute to set.
     */
    public final void setValidDaysAttribute(String ValidDaysAttribute) {
        this.ValidDaysAttribute = ValidDaysAttribute;
        this.logger.info("Valid Days Attribute: '" + ValidDaysAttribute + "'");
    }

    /**
     * @param filter The scope to set.
     */
    public final void setScope(final int scope) {
        this.scope = scope;
    }

    /**
     * @param maxNumberResults The maxNumberResults to set.
     */
    public final void setMaxNumberResults(final int maxNumberResults) {
        this.maxNumberResults = maxNumberResults;
    }

    /**
     * @param searchBase The searchBase to set.
     */
    public final void setSearchBase(final String searchBase) {
        this.searchBase = searchBase;
        this.logger.info("LDAP Search Base: '" + searchBase + "'");
    }

    /**
     * @param noWarnAttribute The noWarnAttribute to set.
     */
    public final void setNoWarnAttribute(final String noWarnAttribute) {
        this.noWarnAttribute = noWarnAttribute;
        this.logger.info("LDAP Attribute to flag warning bypass: '" + noWarnAttribute + "'");
    }

    /**
     * @param noWarnAttribute The noWarnAttribute to set.
     */
    public final void setNoWarnValues(final List<String> noWarnValues) {
        this.noWarnValues = noWarnValues;
        this.logger.info("Value to flag warning bypass: '" + noWarnValues.toString() + "'");
    }

    /**
     * @param timeout The timeout to set.
     */
    public final void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    /**
     * @param warningDays Number of days before expiration that a warning
     * message is displayed to set. Used as a default if warningDaysAttribute is
     * not set or is not found in the LDAP results. This parameter is required.
     */
    public final void setWarningDays(final int warningDays) {
        this.warningDays = warningDays;
        this.logger.info("Default Warning Days: '" + warningDays + "'");
    }
    
    /**
     * @param validDays Number of days that a password is valid for.
     * Used as a default if DateAttribute is not set or is not found in the LDAP results
     */
    public final void setValidDays(final int validDays) {
        this.validDays = validDays;
        this.logger.info("Password Max Age (in days): '" + validDays + "'");
    }

    /**
     * @param dateFormat String to pass to SimpleDateFormat() that describes the
     * date in the ExpireDateAttribute. This parameter is required.
     */
    public final void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
        this.logger.info("Date format: '" + dateFormat + "'");
    }
    
    public final void setIgnorePartialResultException(final boolean ignorePartialResultException) {
        this.ignorePartialResultException = ignorePartialResultException;
    }

    /**
     * Class to hold the data returned from LDAP
     * 
     */
    public class LdapResult {

        String warnDaysResult;
        String validDaysResult;
        String DateResult;
        String noWarnAttributeResult;
        
        public String getWarnDaysResult() {
            return warnDaysResult;
        }

        public void setWarnDaysResult(String warn) {
            this.warnDaysResult = warn;
        }
        
        public String getNoWarnAttributeResult() {
            return noWarnAttributeResult;
        }

        public void setNoWarnAttributeResult(String noWarnAttributeResult) {
            this.noWarnAttributeResult = noWarnAttributeResult;
        }

        public String getValidDaysResult() {
            return validDaysResult;
        }

        public void setValidDaysResult(String valid) {
            this.validDaysResult = valid;
        }
        
        public String getDateResult() {
            return DateResult;
        }
        
        public void setDateResult(String date) {
            this.DateResult = date;
        }
    }
}
