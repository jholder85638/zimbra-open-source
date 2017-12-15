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


if (window.LmAppointmentView) {
	var XM = new XModel(LmAppointmentView.appointmentModel);
	var apptInstance = 
		{
				id : "",
				uid : -1,
				type : null,
				name : "Name",
				startDate : new Date(),
				endDate : new Date(new Date().getTime() + (30*60*1000)),
				transparency : "FR",
				allDayEvent : '0',
				exception : false,
				recurring : false,
				alarm : false,
				otherAttendees : false,
				location : "location",
				notes : null,
				repeatType : "NON",
//				repeatDisplay : null,
				repeatCustom : 0,
				repeatCustomCount : 1,
				repeatCustomType : 'O', // (S)pecific, (O)rdinal
//				repeatCustomOrdinal : '1',
//				repeatCustomDayOfWeek : 'DAY', //(d|wd|we)|((Su|Mo|Tu|We|Th|Fr|Sa
//				repeatWeeklyDays : 'SUNDAY', //Su|Mo|Tu|We|Th|Fr|Sa
//				repeatMonthlyDayList : '1',
//				repeatYearlyMonthsList : '1',
//				repeatEnd : null,
//				repeatEndDate : new Date(),
				repeatEndCount : 1,
				repeatEndType : 'N'
	};
	var formAttr = LmAppointmentView.prototype.getAppointmentForm();
	registerForm("Appointment Edit", new XForm(formAttr, XM), {"Sample":apptInstance});
}
