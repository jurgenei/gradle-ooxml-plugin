<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
                xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
                xmlns="http://www.w3.org/1998/Math/MathML"
                exclude-result-prefixes="m w">

    <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

    <xsl:template match="m:oMathPara">
        <math>
            <mrow>
                <xsl:apply-templates select="m:oMath/*"/>
            </mrow>
        </math>
    </xsl:template>

    <xsl:template match="m:oMath">
        <math>
            <mrow>
                <xsl:apply-templates select="*"/>
            </mrow>
        </math>
    </xsl:template>

    <xsl:template match="m:sSubSup">
        <msubsup>
            <xsl:apply-templates select="m:e/*"/>
            <xsl:apply-templates select="m:sub/*"/>
            <xsl:apply-templates select="m:sup/*"/>
        </msubsup>
    </xsl:template>

    <xsl:template match="m:sSub">
        <msub>
            <xsl:apply-templates select="m:e/*"/>
            <xsl:apply-templates select="m:sub/*"/>
        </msub>
    </xsl:template>

    <xsl:template match="m:sSup">
        <msup>
            <xsl:apply-templates select="m:e/*"/>
            <xsl:apply-templates select="m:sup/*"/>
        </msup>
    </xsl:template>

    <xsl:template match="m:f">
        <mfrac>
            <xsl:apply-templates select="m:num/*"/>
            <xsl:apply-templates select="m:den/*"/>
        </mfrac>
    </xsl:template>

    <xsl:template match="m:rad">
        <msqrt>
            <xsl:apply-templates select="m:e/*"/>
        </msqrt>
    </xsl:template>

    <xsl:template match="m:r">
        <xsl:if test="normalize-space(m:t) != ''">
            <mi>
                <xsl:value-of select="normalize-space(m:t)"/>
            </mi>
        </xsl:if>
    </xsl:template>

    <xsl:template match="m:t">
        <xsl:if test="normalize-space(.) != ''">
            <mi>
                <xsl:value-of select="normalize-space(.)"/>
            </mi>
        </xsl:if>
    </xsl:template>

    <xsl:template match="m:e|m:num|m:den|m:sub|m:sup|m:d|m:box|m:eqArr|m:mr|m:nary|m:func|m:groupChr|m:limLow|m:limUpp">
        <xsl:apply-templates select="*|text()"/>
    </xsl:template>

    <xsl:template match="w:r|w:t|m:rPr|m:oMathParaPr|m:oMathPr|m:ctrlPr|m:sSubSupPr|m:sSubPr|m:sSupPr|m:fPr|m:radPr|m:dPr|m:deg|m:chr|m:begChr|m:endChr">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*">
        <xsl:apply-templates select="*|text()"/>
    </xsl:template>

    <xsl:template match="text()">
        <!-- Ignore non-semantic whitespace and text fragments outside m:t. -->
    </xsl:template>
</xsl:stylesheet>

