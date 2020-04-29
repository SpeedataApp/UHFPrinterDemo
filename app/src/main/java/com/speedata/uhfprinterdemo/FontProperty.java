package com.speedata.uhfprinterdemo;

import android.content.Context;
import android.graphics.Typeface;

public class FontProperty {
    boolean bBold;
    boolean bItalic;
    boolean bUnderLine;
    boolean bStrikeout;
    int iSize;
    Typeface sFace;

    public void setFont(boolean bBold, boolean bItalic, boolean bUnderLine, boolean bStrikeout, int iSize, Typeface sFace) {
        this.bBold = bBold;
        this.bItalic = bItalic;
        this.bUnderLine = bUnderLine;
        this.bStrikeout = bStrikeout;
        this.iSize = iSize;
        this.sFace = sFace;
    }

    public void initTypeface(Context mContext, String path) {
        this.sFace = Typeface.createFromAsset(mContext.getAssets(), path);
    }

    public void initTypefaceToString(String familyName, int style) {
        this.sFace = Typeface.create(familyName, style);
    }

    public void setBold(boolean bBold) {
        this.bBold = bBold;
    }

    public void setItalic(boolean bItalic) {
        this.bItalic = bItalic;
    }

    public void setUnderLine(boolean bUnderLine) {
        this.bUnderLine = bUnderLine;
    }

    public void setStrikeout(boolean bStrikeout) {
        this.bStrikeout = bStrikeout;
    }

    public void setSize(int iSize) {
        this.iSize = iSize;
    }

    public void setFace(Typeface sFace) {
        this.sFace = sFace;
    }
}
