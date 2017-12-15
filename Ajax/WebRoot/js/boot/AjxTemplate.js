/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2006 Zimbra, Inc.
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
function AjxTemplate() {}

//
// Data
//

AjxTemplate._templates = {};

//
// Public functions
//

AjxTemplate.register = function(name, func, params) {
    AjxPackage.define(name.replace(/#.*$/,""));
    AjxTemplate._templates[name] = { name: name, func: func, params: params || {} };
};

AjxTemplate.getTemplate = function(name) {
    var template = AjxTemplate._templates[name];
    return template && template.func;
};

AjxTemplate.getParams = function(name) {
    var template = AjxTemplate._templates[name];
    return template && template.params;
};

AjxTemplate.expand = function(name, data, buffer) {
    var pkg = name.replace(/#.*$/, "");
    var id = name.replace(/^[^#]*#?/, "");
    if (id) {
        name = [pkg, id].join("#");
    }

    AjxPackage.require(pkg);

    var hasBuffer = Boolean(buffer);
    buffer = buffer || [];
    var func = AjxTemplate.getTemplate(name);
    if (func) {
    	try {
            var params = AjxTemplate.getParams(name);
            func(name, params, data, buffer);
	    } catch (e) {
	    	buffer.push(this.__formatError(name, e));
	    }
    } else {
    	buffer.push(this.__formatError(name, "template not found"));
    }

    return hasBuffer ? buffer.length : buffer.join("");
};

// set innerHTML of a DOM element with the results of a template expansion
// TODO: have some sort of actual error reporting
AjxTemplate.setContent = function(element, name, data) {
	if (typeof element == "string") {
		element = document.getElementById(element);
	}
	if (element == null) return;
	var html = AjxTemplate.expand(name, data);
	element.innerHTML = html;
}


// temporary API for handling logic errors in templates
//	may change to more robust solution later
AjxTemplate.__formatError = function(templateName, error) {
	return "Error in template '" + templateName + "': " + error;	
}