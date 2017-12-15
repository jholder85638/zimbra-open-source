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


function AjxEnv() {
}

AjxEnv._inited = false;

AjxEnv.DEFAULT_LOCALE = window.navigator.userLanguage || window.navigator.language || window.navigator.systemLanguage;

AjxEnv.reset = function () {
	AjxEnv.browserVersion = -1;
	AjxEnv.geckoDate = 0;
	AjxEnv.mozVersion = -1;
	AjxEnv.isMac = false;
	AjxEnv.isWindows = false;
	AjxEnv.isLinux = false;
	AjxEnv.isNav  = false;
	AjxEnv.isIE = false;
	AjxEnv.isNav4 = false;
	AjxEnv.trueNs = true;
	AjxEnv.isNav6 = false;
	AjxEnv.isNav6up = false;
	AjxEnv.isNav7 = false;
	AjxEnv.isIE3 = false;
	AjxEnv.isIE4 = false;
	AjxEnv.isIE4up = false;
	AjxEnv.isIE5 = false;
	AjxEnv.isIE5_5 = false;
	AjxEnv.isIE5up = false;
	AjxEnv.isIE5_5up = false;
	AjxEnv.isIE6  = false;
	AjxEnv.isIE6up = false;
	AjxEnv.isNormalResolution = false;
	AjxEnv.ieScaleFactor = 1;
	AjxEnv.isFirefox = false;
	AjxEnv.isFirefox1up = false;
	AjxEnv.isFirefox1_5up = false;
	AjxEnv.isFirefox3up = false;
	AjxEnv.isMozilla = false;
	AjxEnv.isMozilla1_4up = false;
	AjxEnv.isSafari = false;
	AjxEnv.isCamino = false;
	AjxEnv.isGeckoBased = false;
	AjxEnv.isOpera = false;
	AjxEnv.useTransparentPNGs = false;

	// screen resolution - ADD MORE RESOLUTION CHECKS AS NEEDED HERE:
	AjxEnv.is800x600orLower = screen.width <= 800 && screen.height <= 600;
};

AjxEnv.parseUA = function (userAgent) {
	var agt = userAgent.toLowerCase();
	var agtArr = agt.split(" ");
	var i = 0;
	var index = -1;
	var token = null;
	var isSpoofer = false;
	var isWebTv = false;
	var isHotJava = false;
	var beginsWithMozilla = false;
	var isCompatible = false;
	if (agtArr != null) {
		if ( (index = agtArr[0].search(/^\s*mozilla\//) )!= -1){
			beginsWithMozilla = true;
			AjxEnv.browserVersion = parseFloat(agtArr[0].substring(index + 8));
			AjxEnv.isNav = true;
		}
		for ( ; i < agtArr.length; ++i ){
			token = agtArr[i];
			if (token.indexOf('compatible') != -1 ) {
				isCompatible = true;
				AjxEnv.isNav = false;
			} else if ((token.indexOf('opera')) != -1) {
				AjxEnv.isOpera = true;
				AjxEnv.isNav = false;
				AjxEnv.browserVersion = parseFloat(agtArr[i+1]);
			} else if ((token.indexOf('spoofer')) != -1) {
				isSpoofer = true;
				AjxEnv.isNav = false;
			} else if ((token.indexOf('webtv')) != -1) {
				isWebTv = true;
				AjxEnv.isNav = false;
			} else if ((token.indexOf('hotjava')) != -1) {
				isHotJava = true;
				AjxEnv.isNav = false;
			} else if ((index = token.indexOf('msie')) != -1) {
				AjxEnv.isIE = true;
				AjxEnv.browserVersion = parseFloat(agtArr[i+1]);
			} else if ((index = token.indexOf('gecko/')) != -1) {
				AjxEnv.isGeckoBased = true;
				AjxEnv.geckoDate = parseFloat(token.substr(index + 6));
			} else if ((index = token.indexOf('rv:')) != -1) {
				AjxEnv.mozVersion = parseFloat(token.substr(index + 3));
				AjxEnv.browserVersion = AjxEnv.mozVersion;
			} else if ((index = token.indexOf('firefox/')) != -1) {
				AjxEnv.isFirefox = true;
				AjxEnv.browserVersion = parseFloat(token.substr(index + 8));
			} else if ((index = token.indexOf('camino/')) != -1) {
				AjxEnv.isCamino = true;
				AjxEnv.browserVersion = parseFloat(token.substr(index + 7));
			} else if ((index = token.indexOf('netscape6/')) != -1) {
				AjxEnv.trueNs = true;
				AjxEnv.browserVersion = parseFloat(token.substr(index + 10));
			} else if ((index = token.indexOf('netscape/')) != -1) {
				AjxEnv.trueNs = true;
				AjxEnv.browserVersion = parseFloat(token.substr(index + 9));
			} else if ((index = token.indexOf('safari/')) != -1) {
				AjxEnv.isSafari = true;
				AjxEnv.browserVersion = parseFloat(token.substr(index + 7));
			} else if (token.indexOf('windows') != -1) {
				AjxEnv.isWindows = true;
			} else if ((token.indexOf('macintosh') != -1) ||
					   (token.indexOf('mac_') != -1)) {
				AjxEnv.isMac = true;
			} else if (token.indexOf('linux') != -1) {
				AjxEnv.isLinux = true;
			}
		}
		// Note: Opera and WebTV spoof Navigator.  
		// We do strict client detection.
		AjxEnv.isNav  = (beginsWithMozilla && !isSpoofer && !isCompatible && 
						!AjxEnv.isOpera && !isWebTv && !isHotJava &&
						!AjxEnv.isSafari);

		AjxEnv.isIE = (AjxEnv.isIE && !AjxEnv.isOpera);

		AjxEnv.isNav4 = (AjxEnv.isNav && (AjxEnv.browserVersion  == 4) &&
						(!AjxEnv.isIE));
		AjxEnv.isNav6 = (AjxEnv.isNav && AjxEnv.trueNs && 
						(AjxEnv.browserVersion >= 6.0) && 
						(AjxEnv.browserVersion < 7.0));
		AjxEnv.isNav6up = (AjxEnv.isNav && AjxEnv.trueNs && 
						  (AjxEnv.browserVersion >= 6.0));
		AjxEnv.isNav7 = (AjxEnv.isNav && AjxEnv.trueNs && 
						(AjxEnv.browserVersion == 7.0));

		AjxEnv.isIE3 = (AjxEnv.isIE && (AjxEnv.browserVersion < 4));
		AjxEnv.isIE4 = (AjxEnv.isIE && (AjxEnv.browserVersion == 4) && 
					 (AjxEnv.browserVersion == 4.0));
		AjxEnv.isIE4up = (AjxEnv.isIE && (AjxEnv.browserVersion >= 4));
		AjxEnv.isIE5 = (AjxEnv.isIE && (AjxEnv.browserVersion == 4) && 
					 (AjxEnv.browserVersion == 5.0));
		AjxEnv.isIE5_5 = (AjxEnv.isIE && (AjxEnv.browserVersion == 4) && 
						 (AjxEnv.browserVersion == 5.5));
		AjxEnv.isIE5up = (AjxEnv.isIE && (AjxEnv.browserVersion >= 5.0));
		AjxEnv.isIE5_5up =(AjxEnv.isIE && (AjxEnv.browserVersion >= 5.5));
		AjxEnv.isIE6  = (AjxEnv.isIE && (AjxEnv.browserVersion == 6.0));
		AjxEnv.isIE6up = (AjxEnv.isIE && (AjxEnv.browserVersion >= 6.0));
		AjxEnv.isIE7  = (AjxEnv.isIE && (AjxEnv.browserVersion == 7.0));
		AjxEnv.isIE7up = (AjxEnv.isIE && (AjxEnv.browserVersion >= 7.0));

		AjxEnv.isMozilla = ((AjxEnv.isNav && AjxEnv.mozVersion && 
							AjxEnv.isGeckoBased && (AjxEnv.geckoDate != 0)));
		AjxEnv.isMozilla1_4up = (AjxEnv.isMozilla && (AjxEnv.mozVersion >= 1.4));
		AjxEnv.isFirefox = ((AjxEnv.isMozilla && AjxEnv.isFirefox));
		AjxEnv.isFirefox1up = (AjxEnv.isFirefox && AjxEnv.browserVersion >= 1.0);
		AjxEnv.isFirefox1_5up = (AjxEnv.isFirefox && AjxEnv.browserVersion >= 1.5);
		AjxEnv.isFirefox2_0up = (AjxEnv.isFirefox && AjxEnv.browserVersion >= 2.0);
		AjxEnv.isFirefox3up = (AjxEnv.isFirefox && AjxEnv.browserVersion >= 3.0);

		AjxEnv.browser = "";
		if (AjxEnv.isOpera) {
			AjxEnv.browser = "OPERA";
		} else if (AjxEnv.isSafari) {
			AjxEnv.browser = "SAF";
		} else if (AjxEnv.isCamino) {
			AjxEnv.browser = "CAM";
		} else if (isWebTv) {
			AjxEnv.browser = "WEBTV";
		} else if (isHotJava) {
			AjxEnv.browser = "HOTJAVA";
		} else if (AjxEnv.isFirefox3up) {
			AjxEnv.browser = "FF3.0";
		} else if (AjxEnv.isFirefox2_0up) {
			AjxEnv.browser = "FF2.0";
		} else if (AjxEnv.isFirefox1_5up) {
			AjxEnv.browser = "FF1.5";
		} else if (AjxEnv.isFirefox1up) {
			AjxEnv.browser = "FF1.0";
		} else if (AjxEnv.isFirefox) {
			AjxEnv.browser = "FF";
		} else if (AjxEnv.isNav7) {
			AjxEnv.browser = "NAV7";
		} else if (AjxEnv.isNav6) {
			AjxEnv.browser = "NAV6";
		} else if (AjxEnv.isNav4) {
			AjxEnv.browser = "NAV4";
		} else if (AjxEnv.isIE7) {
			AjxEnv.browser = "IE7";
		} else if (AjxEnv.isIE6) {
			AjxEnv.browser = "IE6";
		} else if (AjxEnv.isIE5) {
			AjxEnv.browser = "IE5";
		} else if (AjxEnv.isIE4) {
			AjxEnv.browser = "IE4";
		} else if (AjxEnv.isIE3) {
			AjxEnv.browser = "IE";
		}
		AjxEnv.platform = "";
		if (AjxEnv.isWindows) {
			AjxEnv.platform = "Win";
		} else if (AjxEnv.isMac) {
			AjxEnv.platform = "Mac";
		} else if (AjxEnv.isLinux) {
			AjxEnv.platform = "Linux";
		}
	}
	// setup some global setting we can check for high resolution
	if (AjxEnv.isIE){
		AjxEnv.isNormalResolution = true;
		AjxEnv.ieScaleFactor = screen.deviceXDPI / screen.logicalXDPI;
		if (AjxEnv.ieScaleFactor > 1) {
			AjxEnv.isNormalResolution = false;
		}
	}
	// show transparent PNGs on platforms that support them well
	//	(eg: all but IE and Linux)
	//	MOW: having trouble getting safari to render transparency for shadows, skipping there, too
	AjxEnv.useTransparentPNGs = !AjxEnv.isIE && !AjxEnv.isLinux && !AjxEnv.isSafari;
	AjxEnv._inited = !AjxEnv.isIE;

	// test for safari nightly
	// XXX: CHANGE ONCE OFFICIAL 420.x is released!
	var webkit = AjxEnv.isSafari ? AjxEnv.getWebkitVersion() : null;
	AjxEnv.isSafariNightly = webkit && webkit['is_nightly'];
};

// XXX: LAME code provided by the webkit dudes
AjxEnv.getWebkitVersion =
function() {
	var webkit_version;
	var regex = new RegExp("\\(.*\\) AppleWebKit/(.*) \\((.*)");
	var matches = regex.exec(navigator.userAgent);
	if (matches) {
		var version = matches[1];
		var bits = version.split(".");
		var is_nightly = (version[version.length - 1] == "+");
		var minor = is_nightly ? "+" : parseInt(bits[1]);
		// If minor is Not a Number (NaN) return an empty string
		if (isNaN(minor)) minor = "";

		webkit_version = { major:parseInt(bits[0]), minor:minor, is_nightly:is_nightly};
	}
	return {major: webkit_version['major'], minor: webkit_version['minor'], is_nightly: webkit_version['is_nightly']};
}

AjxEnv.reset();
AjxEnv.parseUA(navigator.userAgent);

// COMPATIBILITY

// Safari doesn't support string.replace(/regexp/, function);
if (AjxEnv.isSafari) {
	if (!String.prototype._AjxOldReplace) {
		String.prototype._AjxOldReplace = String.prototype.replace;
		String.prototype.replace = function(re, val) {
			if (typeof val != "function")
				return this._AjxOldReplace(re, val);
			else {
				// TODO: investigate if it's possible to use the array.join approach
				var str = this.slice(0), v, l, a;
				while (a = re.exec(str)) {
					v = val.apply(null, a);
					l = a[0].length;
					re.lastIndex -= l - v.length;
					str = str.substr(0, a.index) + v + str.substr(a.index + l);
					if (!re.global)
						break;
				}
				return str;
			}
		};
	}
}
