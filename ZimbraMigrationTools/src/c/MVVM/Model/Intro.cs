/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2014, 2015, 2016 Synacor, Inc.
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
 */
using CssLib;


namespace MVVM.Model
{
    using System;

    public class Intro
    {
        internal Intro()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock("Intro.Intro"))
            {
                Populate();
            }
        }

        public string BuildNum
        {
            get;
            set;
        }

        public string WelcomeMsg
        {
            get;
            set;
        }

        public bool rbServerMigration
        {
            get;
            set;
        }

        public bool rbUserMigration
        {
            get;
            set;
        }

        public bool rbDesktopMigration
        {
            get;
            set;
        }
        public string InstallDir
        {
            get;
            set;
        }

        public Intro Populate()
        {
            this.BuildNum = new BuildNum().BUILD_NUM;
            this.InstallDir = Environment.CurrentDirectory;
            this.WelcomeMsg = "This application will guide you through the process of migrating from Microsoft products to Zimbra. Select a Migration type below:";
            return this;
        }
    }
}
