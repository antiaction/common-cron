/*
 * Cron Schedule, abstract schedule class with common fields and methods.
 * Copyright (C) 2001  Nicholas Clarke
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

/*
 * History:
 *
 * 11-May-2000 : Last recorded change.
 * 17-Feb-2001 : Removed all the bsd crontab specific code.
 * 24-Feb-2001 : Added isEmpty().
 * 10-Nov-2001 : Package change again.
 *               Renamed jmonthToInt() -> monthToInt().
 *               Renamed weekdayToInt() -> weekdayToInt().
 *               toString() uses StringBuffer. Added array name.
 *               Renamed reset() -> clear(),
 *               Class renamed and turned abstract.
 *               Removed isValid(), isEmpty() and calString().
 *               Renamed class and interface. :)
 *               Removed command field and get/set methods.
 * 11-Nov-2001 : Added single Calendar and Date object for schedule building.
 *               getEvents() changed, uses an array instead of an arraylist of Calendars.
 *               Added getEvents() signature with long parameter.
 * 12-Nov-2001 : Renamed getEvents() -> getScheduleArray(). Javadoc.
 *             : Reimplemented old getEvents() as getScheduleList().
 *                Using List and without creating new Calendar objects. Returns Longs.
 */

package com.antiaction.common.cron;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

/**
 * Abstract class with common fields and methods for a basic scheduling
 * implemention. Extending classes will have to initialize these fields
 * from some form of chosen configuration.
 *
 * @version 2.00
 * @author Nicholas Clarke <nclarke@diku.dk>
 */
public abstract class CronScheduleAbstract implements CronSchedule {

	/** Minute array from 0-59. */
	protected byte[] minute = new byte[60];
	/** Hour array from 0-23. */
	protected byte[] hour = new byte[24];
	/** Day of month array from 0-30. */
	protected byte[] dayofmonth = new byte[31];
	/** Month array. */
	protected byte[] month = new byte[12];
	/** Day of Week array from 0-7 (Sun-Sun). */
	protected byte[] dayofweek = new byte[8];

	/** Minute all. */
	protected boolean minuteAll;
	/** Hour all. */
	protected boolean hourAll;
	/** Day of month all. */
	protected boolean dayofmonthAll;
	/** Month all. */
	protected boolean monthAll;
	/** Day of week all. */
	protected boolean dayofweekAll;

	/** GregorianCalendar used for building the day schedule array. */
	protected GregorianCalendar dayCal;
	/** Date associated with Calendar. */
	protected Date dayDate;

	/** Avoid fill cast, bite me. */
	private static byte byteZero = 0;

	/**
	 * Creates and initializes an empty schedule.
	 */
	public CronScheduleAbstract() {
		clear();
	}

	/**
	 * Clears all internal fields, leaving an empty schedule.
	 */
	public void clear() {
		Arrays.fill(minute, byteZero);
		Arrays.fill(hour, byteZero);
		Arrays.fill(dayofmonth, byteZero);
		Arrays.fill(month, byteZero);
		Arrays.fill(dayofweek, byteZero);
		minuteAll = false;
		hourAll = false;
		dayofmonthAll = false;
		monthAll = false;
		dayofweekAll = false;
		dayCal = new GregorianCalendar();
		dayDate = dayCal.getTime();
	}

	/**
	 * Given a <CODE>Calendar</CODE> returns a schedule byte array containing one
	 * element per minute. One indicating an event.
	 * @param cal <CODE>Calendar</CODE>.
	 * @return schedule array.
	 */
	public byte[] getScheduleArray(Calendar cal) {
		return getScheduleArray( cal.getTime().getTime() );
	}

	/**
	 * Given a date as long returns a schedule byte array containing one
	 * element per minute. One indicating an event.
	 * @param l date as long.
	 * @return schedule array.
	 */
	public byte[] getScheduleArray(long l) {
		int dateIndex, monthIndex, weekdayIndex;
		byte[] tmpArray;
		int daySwitch;
		boolean b;
		int idx;

	// Setup

		dayDate.setTime( l );
		dayCal.setTime( dayDate );

		dateIndex = dayCal.get(Calendar.DATE) - 1;
		monthIndex = monthToInt( dayCal.get(Calendar.MONTH) );
		weekdayIndex = weekdayToInt( dayCal.get(Calendar.DAY_OF_WEEK ) );

		tmpArray = new byte[24*60];
		Arrays.fill(tmpArray, byteZero);

	// Valid

		if ( (dateIndex == -1) || (monthIndex == -1) || (weekdayIndex == -1) )
			return tmpArray;

	// Month

		if ( monthAll || (month[monthIndex] != 0) ) {

		// Day of Month and/or Week

			daySwitch = 0;
			if ( dayofmonthAll ) daySwitch = daySwitch | 1;
			if ( dayofweekAll )	daySwitch = daySwitch | 2;

			switch( daySwitch ) {
				case 0:	// m.day - w.day -
					if ( (dayofmonth[dateIndex] != 0) || (dayofweek[weekdayIndex] != 0) )
						b = true;
					else
						b = false;
					break;
				case 1:	// m.day * w.day -
					if ( dayofweek[weekdayIndex] != 0 )
						b = true;
					else
						b = false;
					break;
				case 2:	// m.day - w.day *
					if ( dayofmonth[dateIndex] != 0 )
						b = true;
					else
						b = false;
					break;
				case 3:	// m.day * w.day *
					b = true;
					break;
				default:
					b = false;
			}
			if ( b ) {
				idx = 0;
				for(int h=0; h<24; ++h) {
					for(int m=0; m<60; ++m) {
						if ( (hour[h] != 0) && (minute[m] != 0) ) {
							tmpArray[idx] = 1;
						}
						++idx;
					}
				}
			}
		}

		return tmpArray;
	}

	/**
	 * Given a <CODE>Calendar</CODE> returns a schedule list of dates as long.
	 * @param cal <CODE>Calendar</CODE>.
	 * @return schedule list of dates as <CODE>Long</CODE>.
	 */
	public List getScheduleList(Calendar cal) {
		return getScheduleList( cal.getTime().getTime() );
	}

	/**
	 * Given a date as long returns a schedule list of dates as long.
	 * @param l date as long.
	 * @return schedule list of dates as <CODE>Long</CODE>.
	 */
	public List getScheduleList(long l) {
		int dateIndex, monthIndex, weekdayIndex;
		ArrayList tmpArray;
		int daySwitch;
		boolean b;

	// Setup

		dayDate.setTime( l );
		dayCal.setTime( dayDate );

		dateIndex = dayCal.get(Calendar.DATE) - 1;
		monthIndex = monthToInt( dayCal.get(Calendar.MONTH) );
		weekdayIndex = weekdayToInt( dayCal.get(Calendar.DAY_OF_WEEK ) );

		tmpArray = new ArrayList();

	// Valid

		if ( (dateIndex == -1) || (monthIndex == -1) || (weekdayIndex == -1) )
			return tmpArray;

	// Month

		if ( monthAll || (month[monthIndex] != 0) ) {

		// Day of Month and/or Week

			daySwitch = 0;
			if ( dayofmonthAll ) daySwitch = daySwitch | 1;
			if ( dayofweekAll )	daySwitch = daySwitch | 2;

			switch( daySwitch ) {
				case 0:	// m.day - w.day -
					if ( (dayofmonth[dateIndex] != 0) || (dayofweek[weekdayIndex] != 0) )
						b = true;
					else
						b = false;
					break;
				case 1:	// m.day * w.day -
					if ( dayofweek[weekdayIndex] != 0 )
						b = true;
					else
						b = false;
					break;
				case 2:	// m.day - w.day *
					if ( dayofmonth[dateIndex] != 0 )
						b = true;
					else
						b = false;
					break;
				case 3:	// m.day * w.day *
					b = true;
					break;
				default:
					b = false;
			}
			if ( b ) {
				dayCal.set( Calendar.SECOND, 0 );
				dayCal.set( Calendar.MILLISECOND, 0 );
				for(int h=0; h<24; ++h) {
					dayCal.set( Calendar.HOUR_OF_DAY, h );
					for(int m=0; m<60; ++m) {
						if ( (hour[h] != 0) && (minute[m] != 0) ) {
							dayCal.set( Calendar.MINUTE, m );
							tmpArray.add( new Long( dayCal.getTime().getTime() ) );
						}
					}
				}
			}
		}

		return tmpArray;
	}

	/**
	 * Convert <CODE>Calendar</CODE> month to internal representation
	 * @param m <CODE>Calendar</CODE> month.
	 * @return internal representation of month index.
	 */
	public static int monthToInt(int m) {
		int i;
		switch (m) {
			case Calendar.JANUARY:
				i = 0;
				break;
			case Calendar.FEBRUARY:
				i = 1;
				break;
			case Calendar.MARCH:
				i = 2;
				break;
			case Calendar.APRIL:
				i = 3;
				break;
			case Calendar.MAY:
				i = 4;
				break;
			case Calendar.JUNE:
				i = 5;
				break;
			case Calendar.JULY:
				i = 6;
				break;
			case Calendar.AUGUST:
				i = 7;
				break;
			case Calendar.OCTOBER:
				i = 8;
				break;
			case Calendar.SEPTEMBER:
				i = 9;
				break;
			case Calendar.NOVEMBER:
				i = 10;
				break;
			case Calendar.DECEMBER:
			case Calendar.UNDECIMBER:
				i = 11;
				break;
			default:
				i = -1;
		}
		return i;
	}

	/**
	 * Convert <CODE>Calendar</CODE> DayOfWeek to internal representation.
	 * @param d <CODE>Calendar</CODE> DayOfWeek.
	 * @return intertal representation of the DayOfWeek index.
	 */
	public static int weekdayToInt(int d) {
		int i;
		switch (d) {
			case Calendar.SUNDAY:
				i = 0;
				break;
			case Calendar.MONDAY:
				i = 1;
				break;
			case Calendar.TUESDAY:
				i = 2;
				break;
			case Calendar.WEDNESDAY:
				i = 3;
				break;
			case Calendar.THURSDAY:
				i = 4;
				break;
			case Calendar.FRIDAY:
				i = 5;
				break;
			case Calendar.SATURDAY:
				i = 6;
				break;
			default:
				i = -1;
		}
		return i;
	}

	/**
	 * Returns a string representation of the cron schedule data.
	 * Used mainly for debugging purposes.
	 * @return a string representation of the cron schedule data.
	 */
	public String toString() {
		StringBuffer tmpStrB = new StringBuffer();
		tmpStrB.append("Minute[]=");
		for(int i=0; i<60; i++) {
			tmpStrB.append( minute[i] );
		}
		tmpStrB.append("\n");
		tmpStrB.append("Hour[]=");
		for(int i=0; i<24; i++) {
			tmpStrB.append( hour[i] );
		}
		tmpStrB.append("\n");
		tmpStrB.append("DayOfMonth[]=");
		for(int i=0; i<31; i++) {
			tmpStrB.append( dayofmonth[i] );
		}
		tmpStrB.append("\n");
		tmpStrB.append("Month[]=");
		for(int i=0; i<12; i++) {
			tmpStrB.append( month[i] );
		}
		tmpStrB.append("\n");
		tmpStrB.append("DayOfWeek[]=");
		for(int i=0; i<7; i++) {
			tmpStrB.append( dayofweek[i] );
		}
		return tmpStrB.toString();
	}

}
