/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2005, 2006 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * 
 * ***** END LICENSE BLOCK *****
 */


package com.zimbra.kabuki.tools.img;

import java.awt.image.BufferedImage;
import java.io.File;

public abstract class DecodedImage {
    protected String mFilename;
    protected File mInputDir;
    protected int mCombinedRow = -1;
    protected int mCombinedColumn = -1;
    protected String mPrefix;
    protected int mLayoutStyle;

    public abstract BufferedImage getBufferedImage();

    public abstract int getWidth();

    public abstract int getHeight();

    public void setCombinedRow(int x) {
        mCombinedRow = x;
    }

    public int getCombinedRow() {
        return mCombinedRow;
    }

    public void setCombinedColumn(int x) {
        mCombinedColumn = x;
    }

    public int getCombinedColumn() {
        return mCombinedColumn;
    }

    /*
     * Get a JavaScript definition for this piece of the combined image.
     * expects combinedFilename to be of the form "megaimage.gif".
     */
    public String getCssString(int combinedWidth,
                               int combinedHeight,
                               String combinedFilename,
                               boolean includeDisableCss) {
        String filename = mFilename.substring(mFilename.lastIndexOf(File.separator) + 1);
        String fileNameBase = filename.substring(0, filename.indexOf('.'));

        String bgImgStr = "background-image:url(\"" + mPrefix + combinedFilename + "?v=@jsVersion@\");";
        String bgPosStr = "background-position:" +
                ((mCombinedColumn == 0) ? "" : "-") + mCombinedColumn + "px " +
                ((mCombinedRow == 0) ? "" : "-") + mCombinedRow + "px;";
        // NOTE: Images that are explicitly laid out horizontally are used as
        //		 vertical borders and should y-repeat. Likewise, images laid
        //		 out vertically are used for horizontal borders and should
        //		 x-repeat. All other images should be set no-repeat, unless
        //		 explicitly set as a repeat layout.
        String bgRptStr = "no-repeat";
        switch (mLayoutStyle) {
            case ImageMerge.HORIZ_LAYOUT:
                bgRptStr = "repeat-x";
                break;
            case ImageMerge.VERT_LAYOUT:
                bgRptStr = "repeat-y";
                break;
            case ImageMerge.TILE_LAYOUT:
                bgRptStr = "repeat";
                break;
        }
        bgRptStr = "background-repeat:" + bgRptStr + ";";

        String widthStr = mLayoutStyle != ImageMerge.HORIZ_LAYOUT && mLayoutStyle != ImageMerge.TILE_LAYOUT
                ? "width:" + getWidth() + "px;" : "";
        String heightStr = mLayoutStyle != ImageMerge.VERT_LAYOUT && mLayoutStyle != ImageMerge.TILE_LAYOUT
                ? "height:" + getHeight() + "px;" : "";

        String className = ".Img" + fileNameBase;
        String backgroundStr = bgImgStr + bgPosStr + bgRptStr;
        String sizeStr = widthStr + heightStr;
        String otherStr = "overflow:hidden;";
        if (includeDisableCss) {
            return className + "," + className + "Dis" + "{" + backgroundStr + sizeStr + otherStr + "}\n" + className
                    + "Dis" + "{opacity:.3;\n#IFDEF MSIE\nfilter:alpha(opacity=30);\n#ENDIF\n}";
        } else {
            return className + "{" + backgroundStr + sizeStr + otherStr + "}";
        }
    }

    public abstract void load() throws java.io.IOException, ImageMergeException;

    public String getFilename() {
        return mFilename;
    }
}