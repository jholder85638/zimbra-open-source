/*
 * ***** BEGIN LICENSE BLOCK *****
 *
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2012, 2013, 2014, 2016 Synacor, Inc.
 *
 * The contents of this file are subject to the Common Public Attribution License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at: https://www.zimbra.com/license
 * The License is based on the Mozilla Public License Version 1.1 but Sections 14 and 15
 * have been added to cover use of software over a computer network and provide for limited attribution
 * for the Original Developer. In addition, Exhibit A has been modified to be consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 * The Original Code is Zimbra Open Source Web Client.
 * The Initial Developer of the Original Code is Zimbra, Inc.  All rights to the Original Code were
 * transferred by Zimbra, Inc. to Synacor, Inc. on September 14, 2015.
 *
 * All portions of the code are Copyright (C) 2012, 2013, 2014, 2016 Synacor, Inc. All Rights Reserved.
 *
 * ***** END LICENSE BLOCK *****
 */
// Copyright (c) 2002-2012 Quadralay Corporation.  All rights reserved.
//

function  WebWorks_WriteArrow(ParamBaseURL,
                              ParamID,
                              bParamExpanded)
{
  var  VarIMGSrc;

  if ((WWHFrame != null) &&
      ( ! WWHFrame.WWHHelp.mbAccessible) &&
      ((typeof(document.all) != "undefined") ||
       (typeof(document.getElementById) != "undefined")))
  {
    document.write(" <a href=\"javascript:WebWorks_ToggleDIV('" + ParamID + "');\">");

    if ((bParamExpanded) ||
        ((typeof(WWHFrame.WWHHighlightWords) != "undefined") &&
         (WWHFrame.WWHHighlightWords != null) &&
         (WWHFrame.WWHHighlightWords.mWordList != null)))
    {
      VarIMGSrc = ParamBaseURL + "images/expanded.gif";
    }
    else
    {
      VarIMGSrc = ParamBaseURL + "images/collapse.gif";
    }

    document.write("<img id=\"" + ParamID + "_arrow\" src=\"" + VarIMGSrc + "\" border=\"0\">");
    document.write("</a>");
  }
}

function  WebWorks_WriteDIVOpen(ParamID,
                                bParamExpanded)
{
  if ((WWHFrame != null) &&
      ( ! WWHFrame.WWHHelp.mbAccessible) &&
      ((typeof(document.all) != "undefined") ||
       (typeof(document.getElementById) != "undefined")))
  {
    if ((bParamExpanded) ||
        ((typeof(WWHFrame.WWHHighlightWords) != "undefined") &&
         (WWHFrame.WWHHighlightWords != null) &&
         (WWHFrame.WWHHighlightWords.mWordList != null)))
    {
      document.write("<div id=\"" + ParamID + "\" style=\"visibility: visible; display: block;\">");
    }
    else
    {
      document.write("<div id=\"" + ParamID + "\" style=\"visibility: hidden; display: none;\">");
    }
  }
}

function  WebWorks_WriteDIVClose()
{
  if ((WWHFrame != null) &&
      ( ! WWHFrame.WWHHelp.mbAccessible) &&
      ((typeof(document.all) != "undefined") ||
       (typeof(document.getElementById) != "undefined")))
  {
    document.write("</div>");
  }
}

function  WebWorks_ToggleDIV(ParamBaseURL,
                             ParamID)
{
  var  VarImageID;
  var  VarIMG;
  var  VarDIV;


  VarImageID = ParamID + "_arrow";

  if (typeof(document.all) != "undefined")
  {
    // Reference image
    //
    VarIMG = document.all[VarImageID];
    if ((typeof(VarIMG) != "undefined") &&
        (VarIMG != null))
    {
      // Nothing to do
    }
    else
    {
      VarIMG = null;
    }

    // Reference DIV tag
    //
    VarDIV = document.all[ParamID];
    if ((typeof(VarDIV) != "undefined") &&
        (VarDIV != null))
    {
      if (VarDIV.style.display == "block")
      {
        if (VarIMG != null)
        {
          VarIMG.src = ParamBaseURL + "images/collapse.gif";
        }

        VarDIV.style.visibility = "hidden";
        VarDIV.style.display = "none";
      }
      else
      {
        if (VarIMG != null)
        {
          VarIMG.src = ParamBaseURL + "images/expanded.gif";
        }

        VarDIV.style.visibility = "visible";
        VarDIV.style.display = "block";
      }
    }
  }
  else if (typeof(document.getElementById) != "undefined")
  {
    // Reference image
    //
    VarIMG = document[VarImageID];
    if ((typeof(VarIMG) != "undefined") &&
        (VarIMG != null))
    {
      // Nothing to do
    }
    else
    {
      VarIMG = null;
    }

    // Reference DIV tag
    //
    VarDIV = document.getElementById(ParamID);
    if ((typeof(VarDIV) != "undefined") &&
        (VarDIV != null))
    {
      if (VarDIV.style.display == "block")
      {
        if (VarIMG != null)
        {
          VarIMG.src = ParamBaseURL + "images/collapse.gif";
        }

        VarDIV.style.visibility = "hidden";
        VarDIV.style.display = "none";
      }
      else
      {
        if (VarIMG != null)
        {
          VarIMG.src = ParamBaseURL + "images/expanded.gif";
        }

        VarDIV.style.visibility = "visible";
        VarDIV.style.display = "block";
      }
    }
  }
}
