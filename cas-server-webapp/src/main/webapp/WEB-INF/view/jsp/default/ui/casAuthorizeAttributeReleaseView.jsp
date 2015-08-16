<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<jsp:directive.include file="includes/top.jsp" />

<style>
    .CSSTableGenerator {
        margin:0px;padding:0px;
        width:100%;
        box-shadow: 10px 10px 5px #888888;
        border:1px solid #000000;

        -moz-border-radius-bottomleft:0px;
        -webkit-border-bottom-left-radius:0px;
        border-bottom-left-radius:0px;

        -moz-border-radius-bottomright:0px;
        -webkit-border-bottom-right-radius:0px;
        border-bottom-right-radius:0px;

        -moz-border-radius-topright:0px;
        -webkit-border-top-right-radius:0px;
        border-top-right-radius:0px;

        -moz-border-radius-topleft:0px;
        -webkit-border-top-left-radius:0px;
        border-top-left-radius:0px;
    }.CSSTableGenerator table{
         border-collapse: collapse;
         border-spacing: 0;
         width:100%;
         height:100%;
         margin:0px;padding:0px;
     }.CSSTableGenerator tr:last-child td:last-child {
          -moz-border-radius-bottomright:0px;
          -webkit-border-bottom-right-radius:0px;
          border-bottom-right-radius:0px;
      }
    .CSSTableGenerator table tr:first-child td:first-child {
        -moz-border-radius-topleft:0px;
        -webkit-border-top-left-radius:0px;
        border-top-left-radius:0px;
    }
    .CSSTableGenerator table tr:first-child td:last-child {
        -moz-border-radius-topright:0px;
        -webkit-border-top-right-radius:0px;
        border-top-right-radius:0px;
    }.CSSTableGenerator tr:last-child td:first-child{
         -moz-border-radius-bottomleft:0px;
         -webkit-border-bottom-left-radius:0px;
         border-bottom-left-radius:0px;
     }.CSSTableGenerator tr:hover td{
          background-color:#d3e9ff;


      }
    .CSSTableGenerator td{
        vertical-align:middle;
        background:-o-linear-gradient(bottom, #ffffff 5%, #d3e9ff 100%);    background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #ffffff), color-stop(1, #d3e9ff) );
        background:-moz-linear-gradient( center top, #ffffff 5%, #d3e9ff 100% );
        filter:progid:DXImageTransform.Microsoft.gradient(startColorstr="#ffffff", endColorstr="#d3e9ff");  background: -o-linear-gradient(top,#ffffff,d3e9ff);

        background-color:#ffffff;

        border:1px solid #000000;
        border-width:0px 1px 1px 0px;
        text-align:left;
        padding:7px;
        font-size:10px;
        font-family:inherit;
        font-weight:normal;
        color:#000000;
    }.CSSTableGenerator tr:last-child td{
         border-width:0px 1px 0px 0px;
     }.CSSTableGenerator tr td:last-child{
          border-width:0px 0px 1px 0px;
      }.CSSTableGenerator tr:last-child td:last-child{
           border-width:0px 0px 0px 0px;
       }
    .CSSTableGenerator tr:first-child td{
        background:-o-linear-gradient(bottom, #093f75 5%, #107dea 100%);    background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #093f75), color-stop(1, #107dea) );
        background:-moz-linear-gradient( center top, #093f75 5%, #107dea 100% );
        filter:progid:DXImageTransform.Microsoft.gradient(startColorstr="#093f75", endColorstr="#107dea");  background: -o-linear-gradient(top,#093f75,107dea);

        background-color:#093f75;
        border:0px solid #000000;
        text-align:center;
        border-width:0px 0px 1px 1px;
        font-size:14px;
        font-family:inherit;
        font-weight:normal;
        color:#ffffff;
    }
    .CSSTableGenerator tr:first-child:hover td{
        background:-o-linear-gradient(bottom, #093f75 5%, #107dea 100%);    background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #093f75), color-stop(1, #107dea) );
        background:-moz-linear-gradient( center top, #093f75 5%, #107dea 100% );
        filter:progid:DXImageTransform.Microsoft.gradient(startColorstr="#093f75", endColorstr="#107dea");  background: -o-linear-gradient(top,#093f75,107dea);

        background-color:#093f75;
    }
    .CSSTableGenerator tr:first-child td:first-child{
        border-width:0px 0px 1px 0px;
    }
    .CSSTableGenerator tr:first-child td:last-child{
        border-width:0px 0px 1px 1px;
    }
</style>

<script type="text/javascript">
    function cancelAuthorize() {
        $("#eventId").val("cancel");
        $("#fm1").submit();
    }
</script>


<p>The following attributes will be released to <strong>${registeredService.name}</strong></p>
<table class="CSSTableGenerator" id="attributesTbl">

    <tr>
        <td>Name</td>
        <td>Value(s)</td>
    </tr>


    <c:forEach var="attr"
               items="${attributes}"
               varStatus="loopStatus" begin="0"
               end="${fn:length(attributes)}"
               step="1">

        <tr>
            <td>${attr.key}</td>

            <td>
                <c:forEach var="attrval" items="${attr.value}">
                    <div>${fn:escapeXml(attrval)}</div>
                </c:forEach>

            </td>
        </tr>
    </c:forEach>

</table>

<div id="login">
    <form:form method="post" id="fm1" htmlEscape="true">
        <section class="row btn-row">
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" id="eventId" name="_eventId" value="accept" />

            <input class="btn-submit" type="submit"
                   value="<spring:message code="screen.aup.button.accept" />" >
            <input class="btn-reset" type="reset"
                   onclick="cancelAuthorize();"
                   value="<spring:message code="screen.aup.button.cancel" />" >
        </section>
    </form:form>

</div>
<jsp:directive.include file="includes/bottom.jsp" />