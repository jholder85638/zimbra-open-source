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


package com.zimbra.kabuki.tools.i18n;

import java.io.File;
import org.apache.tools.ant.*;

public class GenerateDataTask
	extends Task {

	//
	// Data
	//

	// required

	private String dirname = null;
	private String basename = "I18nMsg";

	//
	// Public methods
	//

	// required

	public void setDestDir(String dirname) {
		this.dirname = dirname;
	}

	public void setBaseName(String basename) {
		this.basename = basename;
	}

	//
	// Task methods
	//

	public void execute() throws BuildException {

		// check required arguments
		if (dirname == null) {
			throw new BuildException("destination directory required -- use destdir attribute");
		}
		File dir = new File(dirname);
		if (!dir.exists()) {
			throw new BuildException("destination directory doesn't exist");
		}
		if (!dir.isDirectory()) {
			throw new BuildException("destination must be a directory");
		}

		// build argument list
		String[] argv = { "-d", dirname, "-b", basename };

		// run program
		try {
			System.out.print("GenerateData");
			for (String arg : argv) {
				System.out.print(' ');
				System.out.print(arg);
			}
			System.out.println();
			GenerateData.main(argv);
		}
		catch (Exception e) {
			throw new BuildException(e);
		}

	} // execute()

} // class GenerateDataTask