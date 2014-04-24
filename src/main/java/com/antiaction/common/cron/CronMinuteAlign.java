/*
 * CronMinuteAlign, sleeps until the next full minute.
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
 * 14-May-2000 : Last recorded change.
 * 25-Jan-2001 : Tracking of event day, turnover method.
 * 26-Jan-2001 : Return type changed from boolean to datetime.
 * 16-Feb-2001 : Changed custom DateTime type into Calendar.
 * 17-Feb-2001 : Minor changes.
 * 02-Nov-2001 : Package change again.
 * 03-Nov-2001 : Removed constructor with debug parameter.
 * 04-Nov-2001 : nextMinute() rewritten so as not to use clones or instantiations
 *               of further Calender object.
 *               Renamed calString() -> toString().
 *               getCron* return time as long instead of Calendar clones.
 *               Renamed nextMinute() -> sleep().
 * 05-Nov-2001 : calDayTurnOver() removed.
 *               getCronTurn() rewritten so it doesnt clone a calendar object.
 *               Renamed getCronTurn() -> getDayTurnOver().
 *               Added some static temporary objects.
 *               Added toString(long) static method.
 *               Recoded toString(Calendar) to use StringBuffer and less no extra objects.
 * 07-Nov-2001 : Minor beautifying.
 * 10-Nov-2001 : Changed imports.
 */

package com.antiaction.common.cron;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Small class implementation of a minute based event handler. This class
 * can be used to run code at intervals of one minute. The class is self
 * aligning and only returns on the minute (hh:mm:00) or when interrupted.
 *
 * @version 2.00
 * @author Nicholas Clarke <nclarke@diku.dk>
 */
public class CronMinuteAlign {

	/** Current alignment datestamp. */
	protected GregorianCalendar cronCurr;
	/** Temporary Date. */
	protected Date currDate;
	/** Previous events datestamp. */
	protected Calendar cronPrev;
	/** Next events datestamp. */
	protected Calendar cronNext;
	/** Milliseconds to sleep until the next event. */
	protected long cronSleep = 0;
	/** Temporary Calendar. */
	protected Calendar tmpCal;

	/** Calendar day of previous event. */
	protected int prevEventYearDay;
	/** Calendar day of next event. */
	protected int currEventYearDay;

	/** Output debug information on/off. */
	protected boolean debug = false;

	/** Temporary Date. */
	protected static Date staticDate;
	/** Temporary Calendar. */
	protected static Calendar staticCal;

	static {
		staticDate = new Date();
		staticCal = new GregorianCalendar();
	}

	/**
	 * Creates and initializes a small minute based event handler.
	 */
	public CronMinuteAlign() {
		cronCurr = new GregorianCalendar();
		currDate = cronCurr.getTime();
		cronPrev = (Calendar)cronCurr.clone();
		cronNext = (Calendar)cronCurr.clone();
		prevEventYearDay = cronCurr.get( Calendar.DAY_OF_YEAR );
		currEventYearDay = cronCurr.get( Calendar.DAY_OF_YEAR );
		tmpCal = new GregorianCalendar();
	}

	/**
	 * Toggle debug status.
	 * @param b boolean.
	 */
	public void setDebug(boolean b) {
		debug = b;
	}

	/**
	 * Returns clone of the previous cron datestamp as a long.
	 * @return clone of the previous cron datestamp as a long.
	 */
	public long getCronPrev() {
		return cronPrev.getTime().getTime();
	}

	/**
	 * Returns clone of the current cron datestamp as a long.
	 * @return clone of the current cron datestamp as a long.
	 */
	public long getCronCurr() {
		return cronCurr.getTime().getTime();
	}

	/**
	 * Returns clone of the next cron datestamp as a long.
	 * @return clone of the next cron datestamp as a long.
	 */
	public long getCronNext() {
		return cronNext.getTime().getTime();
	}

	/**
	 * Sleeps until the next hh:mm:00 or when interrupted.
	 * @return the current time as a long.
	 */
	public long sleep() {
		Date tmpDate;

	// Current

		currDate.setTime( System.currentTimeMillis() );
		cronCurr.setTime( currDate );

	// Prev + 1

		tmpDate = cronPrev.getTime();
		cronNext.setTime( tmpDate );
		cronNext.set( Calendar.MILLISECOND, 0 );
		cronNext.set( Calendar.SECOND, 0 );
		cronNext.add( Calendar.MINUTE, 1 );

		cronSleep = cronNext.getTime().getTime() - cronCurr.getTime().getTime();
		if ( cronSleep <= 0) {

		// Curr + 1

			tmpDate = cronCurr.getTime();
			cronNext.setTime( tmpDate );
			cronNext.set( Calendar.MILLISECOND, 0 );
			cronNext.set( Calendar.SECOND, 0 );
			cronNext.add( Calendar.MINUTE, 1 );
			cronSleep = cronNext.getTime().getTime() - cronCurr.getTime().getTime();
		}

	// Debug

		if ( debug ) {
			System.out.println( "prev: " + toString(cronPrev) + " - " + cronPrev.getTime().getTime() );
			System.out.println( "curr: " + toString(cronCurr) + " - " + cronCurr.getTime().getTime() );
			System.out.println( "next: " + toString(cronNext) + " - " + cronNext.getTime().getTime() );
		}

	// Sleep

		try {
			Thread.sleep(cronSleep);
		}
		catch (InterruptedException e) {
			tmpDate.setTime( System.currentTimeMillis() );
			cronNext.setTime( tmpDate );
		}

	// Update Next

		tmpDate = cronNext.getTime();
		cronPrev.setTime( tmpDate );
		currEventYearDay = cronNext.get( Calendar.DAY_OF_YEAR );

	// Return

		return cronNext.getTime().getTime();
	}

	/**
	 * Returns a Calendar object with the date as a long of the current
	 * calendar day but with the time set to midnight.
	 * @return turnover date.
	 */
	public long getDayTurnOver() {
		Date tmpDate;
		tmpDate = cronNext.getTime();
		tmpCal.setTime( tmpDate );
		tmpCal.set( Calendar.MILLISECOND, 0 );
		tmpCal.set( Calendar.SECOND, 0 );
		tmpCal.set( Calendar.MINUTE, 0 );
		tmpCal.set( Calendar.HOUR_OF_DAY, 0 );
		tmpCal.roll( Calendar.DAY_OF_YEAR, true );
		return tmpCal.getTime().getTime();
	}

	/**
	 * Returns a boolean indicating day turnover.
	 * @return a boolean indicating day turnover.
	 */
	public boolean isDayTurnOver() {
		if ( currEventYearDay != prevEventYearDay ) {
			prevEventYearDay = currEventYearDay;
			return true;
		}
		return false;
	}

	public static String toString(long l) {
		staticDate.setTime( l );
		staticCal.setTime( staticDate );
		return toString( staticCal );
	}

	/**
	 * Returns date from a Calendar object as a string, mostly for
	 * debugging purposes. (1972-07-12 14:40:00.123)
	 * @return date from a Calendar object as a string.
	 */
	public static String toString(Calendar cal) {
		StringBuffer tmpStrB = new StringBuffer();
		String strYear, strMonth, strDay;
		String strHour, strMin, strSec, strMilli;
		strYear = "0000" + cal.get( Calendar.YEAR );
		strMonth = "00" + ( cal.get( Calendar.MONTH ) + ( 1 - Calendar.JANUARY ) );
		strDay = "00" + cal.get( Calendar.DAY_OF_MONTH );
		strHour = "00" + cal.get( Calendar.HOUR_OF_DAY );
		strMin = "00" + cal.get( Calendar.MINUTE );
		strSec = "00" + cal.get( Calendar.SECOND );
		strMilli = "000" + cal.get( Calendar.MILLISECOND );

		tmpStrB.append( strYear.substring( strYear.length() - 4, strYear.length() ) );
		tmpStrB.append("-");
		tmpStrB.append( strMonth.substring( strMonth.length() - 2, strMonth.length() ) );
		tmpStrB.append("-");
		tmpStrB.append( strDay.substring( strDay.length() - 2, strDay.length() ) );
		tmpStrB.append(" ");
		tmpStrB.append( strHour.substring( strHour.length() - 2, strHour.length() ) );
		tmpStrB.append(":");
		tmpStrB.append( strMin.substring( strMin.length() - 2, strMin.length() ) );
		tmpStrB.append(":");
		tmpStrB.append( strSec.substring( strSec.length() - 2, strSec.length() ) );
		tmpStrB.append(".");
		tmpStrB.append( strMilli.substring( strMilli.length() - 3, strMilli.length() ) );

		return tmpStrB.toString();
	}

}
