<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:import href="urn:docbkx:stylesheet"/>
  <xsl:import href="urn:docbkx:stylesheet/highlight.xsl"/>

  <xsl:param name="cas.version"/>

  <!-- set bellow all your custom xsl configuration -->

  <xsl:template match="*" mode="process.root">
    <xsl:variable name="doc" select="self::*"/>
  
    <xsl:call-template name="user.preroot"/>
    <xsl:call-template name="root.messages"/>
  
    <html>
      <head>
        <xsl:call-template name="system.head.content">
          <xsl:with-param name="node" select="$doc"/>
        </xsl:call-template>
        <xsl:call-template name="head.content">
          <xsl:with-param name="node" select="$doc"/>
        </xsl:call-template>
        <xsl:call-template name="user.head.content">
          <xsl:with-param name="node" select="$doc"/>
        </xsl:call-template>
      </head>
      <body>
        <xsl:call-template name="body.attributes"/>
        <div id="header">
          <xsl:call-template name="user.header.content">
            <xsl:with-param name="node" select="$doc"/>
          </xsl:call-template>
        </div>
        <div id="page">
          <div id="content">
            <xsl:apply-templates select="."/>
          </div>
        </div>
        <div id="footer">
          <xsl:call-template name="user.footer.content">
            <xsl:with-param name="node" select="$doc"/>
          </xsl:call-template>
        </div>
      </body>
    </html>
    <xsl:value-of select="$html.append"/>
    
    <!-- Generate any css files only once, not once per chunk -->
    <xsl:call-template name="generate.css.files"/>
  </xsl:template>
  
  <xsl:template name="user.header.content">
    <span>VERSION</span>
    <span class="version-number">
      <xsl:value-of select="$cas.version" />
    </span>
  </xsl:template>

  <!--
    Important links:
    - http://www.sagehill.net/docbookxsl/
    - http://docbkx-tools.sourceforge.net/
  -->

</xsl:stylesheet>