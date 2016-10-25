<?xml version="1.0" encoding="UTF-8"?>

<!-- ********************************************************** -->
<!-- * This XSL Script are from Mark Michaelis.               * -->
<!-- * original codes from [https://blog.coremedia.com/cm/post/1505148/Apache_Ant_Task_to_Upper_and_LowerCase.html] -->
<!-- ********************************************************** -->

<!-- XSL Script to convert Ant properties from any case to either lower
     or uppercase. Echo the properties to convert with <echoproperties format="xml"/>.
     This stylesheet will output the properties in the format of a properties file.

     Arguments:
       case: either lower or upper. "upper" by default.
       prefix: Prefix for the new properties. Empty by default.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="text" indent="yes" encoding="UTF-8"/>
  <xsl:strip-space elements="*"/>

  <xsl:variable name="upcaseABC"  select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
  <xsl:variable name="lowcaseABC" select="'abcdefghijklmnopqrstuvwxyz'"/>

  <xsl:param name="case" select="'upper'"/>
  <xsl:param name="prefix" select="''"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="properties">
    <xsl:choose>
      <xsl:when test="$case = 'upper'">
        <xsl:apply-templates mode="upper"/>
      </xsl:when>
      <xsl:when test="$case = 'lower'">
        <xsl:apply-templates mode="lower"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          <xsl:text>Unknown case </xsl:text>
          <xsl:value-of select="$case"/>
          <xsl:text>. Please specify either lower or upper.</xsl:text>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="property" mode="upper">
    <xsl:variable name="propname" select="@name"/>
    <xsl:variable name="propvalue" select="@value"/>
    <xsl:value-of select="$prefix"/>
    <xsl:value-of select="$propname"/>
    <xsl:text>=</xsl:text>
    <xsl:value-of select="translate($propvalue,$lowcaseABC,$upcaseABC)"/>
    <xsl:text>
    </xsl:text>
  </xsl:template>

  <xsl:template match="property" mode="lower">
    <xsl:variable name="propname" select="@name"/>
    <xsl:variable name="propvalue" select="@value"/>
    <xsl:value-of select="$prefix"/>
    <xsl:value-of select="$propname"/>
    <xsl:text>=</xsl:text>
    <xsl:value-of select="translate($propvalue,$upcaseABC,$lowcaseABC)"/>
    <xsl:text>
    </xsl:text>
  </xsl:template>

</xsl:stylesheet>
