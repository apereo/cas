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
<%@include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp"%>

<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/8.4/highlight.min.js"></script>
<script>hljs.initHighlightingOnLoad();</script>

<h1>SSO Sessions Report</h1>

<div><p/>
    <pre><code class="json">${activeSsoSessions}</code></pre>
</div>

<%@include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" %>
