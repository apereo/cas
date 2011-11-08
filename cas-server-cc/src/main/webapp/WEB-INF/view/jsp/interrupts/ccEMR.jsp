<jsp:directive.include file="includes/Top.jsp" />
<c:set var="hasForm" value="1" scope="page" />
<div class="info">
<c:choose>
 		<c:when test='${ValidEmr == 0}'>
			There is a problem with your  CamelWeb account or emergency contact information preventing you from logging in.<br />
			To correct the problem, please contact the Help Desk  at email: <a href="mailto:help@conncoll.edu">help@conncoll.edu</a> or phone: 860) 439-HELP (4357)
		</c:when>
		<c:otherwise>
<form:form commandName="${commandName}" htmlEscape="true" method="post">
<c:forEach var="x" begin="0" end="${fn:length(Phones)-1}">
	<c:choose>
	<c:when test="${Phones[x].PhoneCode == 'P'}">
    	<c:set var="Primary" value="${x}" />
    </c:when>
    <c:when test="${Phones[x].PhoneCode == 'C'}">
    	<c:set var="Cell" value="${x}" />
    </c:when>
    </c:choose>
</c:forEach>
<table width="100%" border="0" align="center" cellpadding="2" cellspacing="0" >
<c:choose>
	<c:when test='${emrData.EMRID == 0}'>
        <tr>
            <td colspan="4" bgcolor="#FFFF33">
            	<span class="btext3">
                	In case of a campus emergency, the College will send messages to your campus voice mail and e-mail address. Please fill out the information below to help us reach you. Questions about the alert system? A link FAQ is at the bottom of the page.
                </span>
            </td>
        </tr>
    </c:when>
    <c:otherwise>
    	<tr>
            <td colspan="4" bgcolor="#FFFF33">
            	<span class="btext3">
                	Please review your information and change or update any information as needed. When you are finished reviewing your information, press the "Submit" button at the bottom of the page, even if you have not made changes.
                </span>
            </td>
        </tr>
    </c:otherwise>
</c:choose>

<tr>
    <td colspan="4" align="center">
        <span class="btext4">Connecticut College ${emrData.ContactType} Emergency Contact Form </span>
    </td> 
</tr>
<tr>
    <td colspan="4" align="center">
        <hr />
    </td> 
</tr>
<tr>
	<td colspan="4" bgcolor="#BBD4F9"><span class="btext3">
    	In case of a campus emergency, the College will send messages to your campus voice mail and e-mail address. </span>
    </td>
</tr>
<tr>
	<td colspan="4" align="left" bgcolor="#F3F3F3"><span class="btext3">Your Information</span></td> 
</tr>
<input type="hidden" value="${emrData.ContactType}" name="fields[40]" />
<tr>
    <td align="right"><span class="btext1">First Name</span></td>
    <td align="left">
		<input type="text" value="${ccData.FirstName}" size="20" readonly="true" />
    </td>  
    <td align="right"><span class="btext1">Last Name</span></td>
    <td align="left">
		<input type="Text" value="${ccData.LastName}" size="20" readonly="true" />
    </td> 
</tr>
<tr>
	<td align="right"><span class="btext1">Banner I.D.#</span></td> 
    <td align="left" nowrap>
		<input name="BannerID" type="Text" value="${ccData.CcId}" size="20" readonly="true"/>
    </td> 
	<td align="right"><span class="btext1">Campus e-mail </span></td> 
    <td align="left">
		<input type="Text" value="${ccData.Email}" size="20" readonly="true" />
    </td> 
</tr>
<tr> 
    <td align="right"><span class="btext1">Campus Phone </span></td> 
    <td align="left">
		<input type="Text" value="${ccData.CollegePhone}" size="20" readonly="true" />
        <c:set var="collegephone" value='${fn:replace(ccData.CollegePhone, "-", "")}' />
        <c:set var="x" value='${fn:length(collegephone)}' />
		<input type="hidden" value="${fn:substring(collegephone,x - 7,7)}" name="fields[1]" id="cPhone" />
    </td> 
</tr>
<tr>
	<td colspan="4" align="left" bgcolor="#BBD4F9"><span class="btext3">
We may also send recorded emergency messages to your personal phone, such as a cell phone, and your non-college e-mail.</span></td>
</tr>
<tr>
	<td colspan="4" align="left" bgcolor="#F3F3F3"><span class="btext3">Choice to Opt Out</span></td> 

</tr>
<tr>
	<td colspan="4" align="left">
  		<span class="text1"> We strongly recommend that you provide these contacts so that we have as many ways as possible to contact you in an emergency. However, if you do not wish to receive emergency notifications beyond those to your campus e-mail and voice mail, check this box:</span> 
	</td> 
</tr>

<tr>
	<td></td>
	<td colspan="3" align="left">
  		<input type="checkbox" value="1" name="fields[2]" onclick="ClearReq();" /><span class="text1"> Opt out of emergency contact.</span> 
	</td> 
</tr>
<tr>
	<td colspan="4" align="left" bgcolor="#F3F3F3"><span class="btext3">Your Primary Emergency Phone Number</span></td> 

</tr>
<tr>
	<td colspan="4" align="left">
  		<span class="text1"> Primary number should be a reliable alternative contact number for you; ideally your personal cell phone. </span> 
	</td> 
</tr>
<input name="fields[3]" type="hidden"  value="${ccData.FirstName} ${ccData.LastName}" />
<input name="fields[4]" type="hidden" value="5" />
<tr>
	<td align="right">
    	<sup style="color:red">*</sup>
		<span class="btext1">Phone Number<br />
    	(area code in first box)</span>
	</td> 
	<td align="left" nowrap>
    	<input name="fields[5]" id="areap" type="Text" size="3" maxlength="3" ccminlength="3" ccreq="true" ccnumonly="true" ccnumtype="whole" title="Primary Number Area Code" value="${Phones[Primary].AreaCode}" />  
		<input name="fields[6]" id="phonep" type="text" size="8" maxlength="8" ccminlength="7" ccreq="true" ccnumonly="true" ccnumtype="Phone" title="Primary Phone Number" value="${Phones[Primary].PhoneNum}" />
	</td> 
    <c:choose>
    	<c:when test='${emrData.ContactType == "Student"}'>
        <td align="right"><span class="btext1">Type </span></td> 
		
        <td align="left">
            <select name="fields[7]" title="Primary Contact Number Type">
                <option value="C"<c:if test="${Phones[Primary].phoneType == 'C'}"> selected="true"</c:if>>Personal Cell</option>
                <option value="H"<c:if test="${Phones[Primary].phoneType == 'H'}"> selected="true"</c:if>>Off Campus Residence</option>
            </select>
        </td>   
    	</c:when>
        <c:otherwise>
        <td align="right"><span class="btext1">Type </span></td> 
        <td align="left">
            <select name="fields[7]" title="Primary Contact Number Type">
                <option value="C"<c:if test="${Phones[Primary].phoneType == 'C'}"> selected="true"</c:if>>cell</option>
                <option value="W"<c:if test="${Phones[Primary].phoneType == 'W'}"> selected="true"</c:if>>work</option>
                <option value="H"<c:if test="${Phones[Primary].phoneType == 'H'}"> selected="true"</c:if>>home</option>
            </select>
        </td> 
    	</c:otherwise>
	</c:choose>
</tr>
<input name="fields[8]" type="hidden"  value="${ccData.FirstName} ${ccData.LastName}" />
<input name="fields[9]" type="hidden" value="5" />
<tr>
	<td colspan="4" align="left" bgcolor="#F3F3F3"><span class="btext3">Text Messaging</span></td> 
</tr>
<tr>
  <td colspan="4" align="left"><span class="text1"> If you have a text messaging-capable device and wish to receive a text message in the event of an emergency, add device number and vendor below: </span></td> 
</tr>
<tr>
	<td align="right">
		<span class="btext1">Device Number<br />
    	(area code in first box)</span>
	</td> 
	<td align="left" nowrap>
    	<input name="fields[10]" type="Text" size="3" maxlength="3" ccminlength="3" ccnumonly="true" ccnumtype="whole" title="SMS Device Number area code" value="${Phones[Cell].AreaCode}" />
    	<input name="fields[11]" type="text" size="8" maxlength="8" ccminlength="7" ccnumonly="true" ccnumtype="phone" title="SMS Device Number" value="${Phones[Cell].PhoneNum}" />
        
    <input name="fields[12]" type="hidden" value="C" />
    <input name="fields[13]" type="hidden"  value="${ccData.FirstName} ${ccData.LastName}" />
    <input name="fields[14]" type="hidden" value="5" />
	</td> 
  	<td align="right"><span class="btext1">Provider </span></td> 
  	<td align="left">
    	<select name="fields[41]" title="SMS Device Vendor" />
        <c:forEach var="x" begin="0" end="${fn:length(SMSVendors)-1}">
         	<option value="${SMSVendors[x].SMSVendor}"<c:if test="${emrData.SmsVendor == SMSVendors[x].SMSVendor}"> selected="true"</c:if>>
            	${SMSVendors[x].VendorName}</option>
         </c:forEach>
    	</select>
    </td>
</tr>
<tr>
	<td colspan="4" align="left" bgcolor="#F3F3F3"><span class="btext3">Non-College e-mail</span></td> 
</tr>
<tr>
	<td align="right"><span class="btext1">Personal e-mail</span></td>
    <td align="left">
		<input name="fields[42]" type="text" value="${emrData.toEmail}" size="20" ccvalid="email" title="Off Campus Email" />
    </td>
</tr>
<tr>
	<td colspan="4" align="left" bgcolor="#BBD4F9"><span class="btext3">
    <c:choose> 
    	<c:when test='${emrData.ContactType == "Student"}'>
    In an emergency, the College may want or need to provide information to parents and guardians. You may provide up to 4 contacts for these individuals in the boxes below. This information will be used for emergency communication purposes only and in accordance with our confidentiality obligations under FERPA and other relevant statutes.<br /></span><font size="-1">(U.S. and Canadian numbers only. International students are asked to contact their parent(s)/guardian(s) directly in the event of an emergency.)</font>
    	</c:when>
        <c:otherwise>
    Depending on circumstances, the College may also send emergency messages to spouses, partner or other individuals designated by staff members. If you wish, you may provide contact information for up to four individuals (direct numbers only, no extensions).</span>
    	</c:otherwise>
    </c:choose>
    </td>
</tr>
<tr>
    <td colspan="4" align="left" bgcolor="#F3F3F3"><span class="btext3">Other Emergency Contacts </span></td> 
</tr>
<tr>
  	<td colspan="4" align="center">
    	<table border="0">
    	<tr>
      		<th scope="col"><span class="btext1">Phone</span></th>
      		<th scope="col"><span class="btext1">Type</span></th>
      		<th scope="col"><span class="btext1">Name</span></th>
      		<th scope="col"><span class="btext1">Relationship</span></th>
      	</tr>
		<c:forEach var="x" begin="1" end="4">
        	<c:set var="found" value="0" />
        	<c:set var="y" value="${(x * 5) + 9}" />
        	<c:forEach var="z" begin="0" end="${fn:length(Phones)-1}">
                <c:if test="${Phones[z].PhoneCode == x}">
                    <tr align="center">
                        <td>
                            #${x}<input name="fields[${y + 1}]" type="text" size="3" maxlength="3" ccnumonly="true" ccnumtype="whole" title="Phone Number area code for contact ${x}" value="${Phones[z].AreaCode}" />
                            <input name="fields[${y + 2}]" type="text" size="8" maxlength="8" ccnumonly="true" ccnumtype="phone" title="Phone Number for contact ${x}" value="${Phones[z].PhoneNum}" />
                        </td>
                        <td>
                            <select name="fields[${y + 3}]" title="Phone Type for Contact ${x}">
                                <option value="C"<c:if test="${Phones[z].phoneType == 'C'}"> selected="true"</c:if>>cell</option>
                                <option value="W"<c:if test="${Phones[z].phoneType == 'W'}"> selected="true"</c:if>>work</option>
                                <option value="H"<c:if test="${Phones[z].phoneType == 'H'}"> selected="true"</c:if>>home</option>
                            </select>
                        </td>
                        <td><input name="fields[${y + 4}]" type="text" size="30" maxlength="100" title="Name of conact ${x}" value="${Phones[z].ContactName}" /></td>
                        <td>
                            <select name="fields[${y + 5}]" title="Relationship to Contact ${x}">
                                <c:forEach var="y" begin="0" end="${fn:length(Relations)-1}">
                                    <option value="${Relations[y].ContactRelation}"<c:if test="${Relations[y].ContactRelation == Phones[z].ContactRelation}">
                                         selected="true"</c:if>>${Relations[y].Relationship}</option>
                                </c:forEach>
                            </select>
                        </td>
                     </tr>
                     <c:set var="found" value="1" />
            	</c:if>
         	</c:forEach>
            <c:if test="${found == 0}">
            	<tr align="center">
                    <td>
                        #${x}<input name="fields[${y + 1}]" type="text" size="3" maxlength="3" ccnumonly="true" ccnumtype="whole" title="Phone Number area code for contact ${x}" value="" />
                        <input name="fields[${y + 2}]" type="text" size="8" maxlength="8" ccnumonly="true" ccnumtype="phone" title="Phone Number for contact ${x}" value="" />
                    </td>
                    <td>
                        <select name="fields[${y + 3}]" title="Phone Type for Contact ${x}">
                            <option value="C" >cell</option>
                            <option value="W" >work</option>
                            <option value="H" >home</option>
                        </select>
                    </td>
                    <td><input name="fields[${y + 4}]" type="text" size="30" maxlength="100" title="Name of conact ${x}" value="" /></td>
                    <td>
                        <select name="fields[${y + 5}]" title="Relationship to Contact ${x}">
                            <c:forEach var="y" begin="0" end="${fn:length(Relations)-1}">
                                <option value="${Relations[y].ContactRelation}" >${Relations[y].Relationship}</option>
                            </c:forEach>
                        </select>
                    </td>
                 </tr>
            </c:if>
         </c:forEach>
         </table>
   	</td>
</tr>
<input name="fields[39]" type="hidden" value=" " />
<tr>
    <td colspan="4" align="left" bgcolor="#F3F3F3"><span class="btext3">Preferred Language for Emergency Messages</span></td> 
</tr>
<tr>
  <td colspan="4" align="left"><span class="text1">Preferred language is collected for future use, at this time all emergency messages will be transmitted in English.</span>
  </td>
</tr>
<tr align="center">
    <td align="right"><span class="btext1">Language</span></td>
    <td align="left">       
        <input type="text" name="fields[36]" title="Preferred language" value="${emrData.Language}">

    </td>
</tr>
<tr >
    <td colspan="4" align="left" bgcolor="#F3F3F3"><span class="btext3">TTY Device (telecommunications device for the hearing impaired)</span></td> 
</tr>
<tr>
    <td colspan="4" align="left">
    	<span class="text1">If one of the contact phones is a TTY device, please indicate below: </span>
    </td> 
</tr>
<tr>
    <td colspan="4" align="left">
        <table width="100%" border="0">
        <tr>
            <th scope="col"><input name="fields[38]" type="radio" value="0"<c:if test="${emrData.Tty == '0'}"> checked="true"</c:if> />
                <span class="btext1">No TTY</span></th>
            <th scope="col"><input name="fields[38]" type="radio" value="p"<c:if test="${emrData.Tty == 'p'}"> checked="true"</c:if> />
                <span class="btext1">Primary</span></th>
            <c:forEach var="x" begin="1" end="4">
            	<th scope="col"><input name="fields[38]" type="radio" value="${x}"<c:if test="${emrData.Tty == x}"> checked="true"</c:if> />
                <span class="btext1">Other #${x}</span></th>
            </c:forEach>
        </tr>
        </table>
    </td>
</tr>
<tr>
	<td bgcolor="#F3F3F3" colspan="4">
    	<font color="red">*</font> Required Field<br />
        
    </td>
</tr>
<tr>
	<td bgcolor="#D7E5F9" colspan="4" align="center">
    	<input type="Submit" value="Submit"><br />
        <a href="http://www.conncoll.edu/camelweb/digitalAsset/?id1=824864813">Connect ED FAQ</a>
    </td>
</tr>
</table>
<input type="hidden" name="lt" value="${loginTicket}" />
<input type="hidden" name="execution" value="${flowExecutionKey}" />
<input type="hidden" name="_eventId" value="submit" />
</form:form>
<script src="https://www.conncoll.edu/Scripts/ccValidator.js" language="javascript"></script>
</c:otherwise>
</c:choose>
</div>
<jsp:directive.include file="includes/Bottom.jsp" />