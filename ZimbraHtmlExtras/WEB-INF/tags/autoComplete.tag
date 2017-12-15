<%--
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2007, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
--%>
<%@ tag body-content="scriptless" %>
<%@ taglib prefix="zm" uri="com.zimbra.zm" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<script type="text/javascript" src="http://yui.yahooapis.com/2.2.2/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.2.2/build/connection/connection-min.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.2.2/build/autocomplete/autocomplete-min.js"></script>

<script type="text/javascript">
    var zhEncode = function(s) {return s == null ? '' : s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");}
    var zhStartsWith = function(str,query) { return str == null ? false : str.toLowerCase().indexOf(query.toLowerCase()) == 0; }
    var zhFmt = function(str,query,bold) {
        return bold ?
               ["<span class='ZhACB'>",zhEncode(str.substring(0, query.length)), "</span>", zhEncode(str.substring(query.length))].join("")
                : zhEncode(str);
    }
    var myacformat = function(aResultItem, query) {
        var sResult = aResultItem[0];
        var e = aResultItem[1];
        var f = aResultItem[2];
        var l = aResultItem[3];
        var g = aResultItem[4];
        var fs = zhStartsWith(f, query);
        var ls = fs ? false : zhStartsWith(l, query);
        var es = fs || ls ? false : zhStartsWith(e, query);
        var fls, fq, lq;

        if (!(es|ls|fs)) {
            var fl = f + " " + l;
            fls = zhStartsWith(fl, query);
            if (fls) {
                fs = true;
                fq = f;
                ls = true;
                lq = query.substring(fq.length+1);
            }
        }

        if(sResult) {
            return [
                    "\"",
                    zhFmt(f, fls ? fq : query, fs),
                    " ",
                    zhFmt(l, fls ? lq : query, ls),
                    "\"",
                    " &lt;",
                    zhFmt(e,query,es),
                    "&gt;"].join("");
        }
        else {
            return "";
        }
    };
    var myDataSource = new YAHOO.widget.DS_XHR("/zimbra/h/ac", ["Result","m","e","f","l","g"]);
    var initAuto = function(field,container) {
        var ac = new YAHOO.widget.AutoComplete(field, container, myDataSource);
        ac.delimChar = [",",";"];
        ac.queryDelay = 0.25;
        //ac.useShadow = true;
        ac.formatResult = myacformat;
        ac.queryMatchContains = true;
        ac.maxResultsDisplayed = 20;
        ac.allowBrowserAutocomplete = false;
    };

    <jsp:doBody/>

</script>
