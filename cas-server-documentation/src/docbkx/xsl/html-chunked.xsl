<!--
  ~ Licensed to Jasig under one or more contributor license
  ~ agreements. See the NOTICE file distributed with this work
  ~ for additional information regarding copyright ownership.
  ~ Jasig licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file
  ~ except in compliance with the License.  You may obtain a
  ~ copy of the License at the following location:
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:d="http://docbook.org/ns/docbook"
  version="1.0">

  <xsl:import href="urn:docbkx:stylesheet/chunk.xsl"/>
  <xsl:import href="urn:docbkx:stylesheet/highlight.xsl"/>

  <xsl:param name="cas.version"/>

  <!-- set bellow all your custom xsl configuration -->

  <xsl:template name="chunk-element-content">
    <xsl:param name="prev"/>
    <xsl:param name="next"/>
    <xsl:param name="nav.context"/>
    <xsl:param name="content">
      <xsl:apply-imports/>
    </xsl:param>
  
    <xsl:call-template name="user.preroot"/>
  
    <html>
      <xsl:call-template name="html.head">
        <xsl:with-param name="prev" select="$prev"/>
        <xsl:with-param name="next" select="$next"/>
      </xsl:call-template>
  
      <body>
        <div id="header">
          <span>VERSION</span>
          <span class="version-number">
            <xsl:value-of select="$cas.version" />
          </span>
        </div>
        <xsl:call-template name="body.attributes"/>
        <div id="page">
          <xsl:call-template name="user.header.navigation"/>
    
          <xsl:call-template name="header.navigation">
            <xsl:with-param name="prev" select="$prev"/>
            <xsl:with-param name="next" select="$next"/>
            <xsl:with-param name="nav.context" select="$nav.context"/>
          </xsl:call-template>
    
          <xsl:call-template name="user.header.content"/>
   
          <div id="content"> 
            <xsl:copy-of select="$content"/>
          </div>
        </div>

        <xsl:call-template name="footer.navigation">
          <xsl:with-param name="prev" select="$prev"/>
          <xsl:with-param name="next" select="$next"/>
          <xsl:with-param name="nav.context" select="$nav.context"/>
        </xsl:call-template>
  
        <xsl:call-template name="user.footer.navigation"/>

        <div id="footer">
          <xsl:call-template name="user.footer.content"/>
        </div>
      </body>
    </html>
    <xsl:value-of select="$chunk.append"/>
  </xsl:template>

  <!--
    Important links:
    - http://www.sagehill.net/docbookxsl/
    - http://docbkx-tools.sourceforge.net/
  -->

</xsl:stylesheet>