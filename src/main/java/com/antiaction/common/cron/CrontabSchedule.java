/*
 * Crontab Schedule, crontab entry implementation.
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
 * 17-Feb-2001 : Altered to extend CronEvent and only implemented BSD crontab specific code.
 *             : Dont think I'm gonna mess too much with this code. Scary, but effective!
 * 18-Feb-2001 : Init routine partially recoded.
 *             : Expanded to actually permit a command besides the event data.
 * 24-Feb-2001 : Added isEmpty().
 * 12-Nov-2001 : Package change again.
 *               Class renamed. isEmpty() removed.
 * 13-Nov-2001 : Removed getCrontab().
 */

package com.antiaction.common.cron;

/**
 * Class for parsing crontabs as found among other places in OpenBSD.
 * The format is fairly obfuscated but once mastered simple and powerful.<BR>
 * <BR>
 * RTFM
 *
 * @version 2.00
 * @author Nicholas Clarke <nclarke@diku.dk>
 */
public class CrontabSchedule extends ScheduleAbstract {

	/** Array of strings, one for each substring of a crontab. */
	private String[] fields = new String[6];
	/** Contrains the optional command string associated with a crontab shedule. */
	private String command = "";

	/**
	 * Creates and initializes an empty cron schedule.
	 * @see #crontabFactory(String)
	 */
	public CrontabSchedule() {
		super();
	}

	/**
	 * Bugger.
	 * @param str a string representation of the crontab schedule.
	 */
	public static ScheduleAbstract crontabFactory(String crontabStr) {
		CrontabSchedule crontabSchedule = new CrontabSchedule();
		crontabSchedule.parseString(crontabStr);
		return crontabSchedule;
	}

	/**
	 * Convert and validate a string with the requested schedule.
	 * @param str a string representation of the schedule.
	 * @return a boolean value as to wether the cron schedule was valid.
	 */
	private boolean parseString(String str) {
		int index;
		int currIndx;
		int prevIndx;
		boolean b;

		index = 0;
		currIndx = 0;
		prevIndx = currIndx;
		b = true;

		while ( b ) {
			if ( currIndx < str.length() ) {
				while ( (currIndx < str.length()) && ((str.charAt(currIndx) == ' ') || (str.charAt(currIndx) == '\t')) ) {
					currIndx++;
				}
				prevIndx = currIndx;
				while ( (currIndx < str.length()) && ((str.charAt(currIndx) != ' ') && (str.charAt(currIndx) != '\t')) ) {
					currIndx++;
				}
				if ( currIndx-prevIndx == 0 ) {
					return false;
				}
				fields[index++] = str.substring(prevIndx, currIndx);
			}
			else {
				b = false;
			}
			if ( index == 5 ) {
				while ( (currIndx < str.length()) && ((str.charAt(currIndx) == ' ') || (str.charAt(currIndx) == '\t')) ) {
					currIndx++;
				}
				b = false;
			}
		}

		if ( index < 5 )
			return false;

		fields[index++] = str.substring(currIndx, str.length());

		if ( !parseMinute(fields[0]) )
			return false;
		if ( !parseHour(fields[1]) )
			return false;
		if ( !parseDayOfMonth(fields[2]) )
			return false;
		if ( !parseMonth(fields[3]) )
			return false;
		if ( !parseDayOfWeek(fields[4]) )
			return false;

		dayofweek[0] = (byte)(dayofweek[0] | dayofweek[7]);
		dayofweek[7] = (byte)(dayofweek[7] | dayofweek[0]);

		command = fields[5];

		return true;
	}

	/**
	 * Parse Wildcard Range * / range
	 */
	private boolean parseWildRange(String str, byte[] destarray, int min, int max) {
		int range = 1;
		if ( str.length() == 0 )
			return false;
		try {
			range = Integer.parseInt(str);
		} catch(NumberFormatException e) {
			return false;
		}
		if ( range <= 0 ) {
			return false;
		}
		boolean b = true;
		int index = min;
		while ( b ) {
			destarray[index-min] = 1;
			index += range;
			if ( index > max )
				b = false;
		}
		return true;
	}

	/**
	 * Parse Field
	 */
	private boolean parseField(String str, byte[] destarray, int min, int max, int range) {
		int indexfrom = 0;
		int indexto = 0;
		int minusindex = -1;
		minusindex = str.indexOf('-');
		if ( minusindex == -1 ) {
			if ( range != -1)
				return false;
			try {
				indexfrom = Integer.parseInt(str);
			} catch(NumberFormatException e) {
				return false;
			}
			if ( (indexfrom < min) || (indexfrom > max) )
				return false;
			destarray[indexfrom - min] = 1;
		}
		else {
			try {
				indexfrom = Integer.parseInt(str.substring(0, minusindex));
				indexto = Integer.parseInt(str.substring(minusindex+1, str.length()));
			} catch(NumberFormatException e) {
				return false;
			}
			if ( (indexfrom < min) || (indexto > max) || (indexfrom > indexto) )
				return false;
			if ( range == -1 ) {
				for(int i=indexfrom; i<=indexto; i++)
					destarray[i-min] = 1;
			}
			else {
				boolean b = true;
				int index = indexfrom;
				while ( b ) {
					destarray[index-min] = 1;
					index += range;
					if ( index > indexto )
						b = false;
				}
			}
		}
		return true;
	}

	/**
	 * Parse List Entry
	 */
	private boolean parseEntry(String str, byte[] destarray, int min, int max) {
		int rangeindex = -1;
		int range = 1;
		rangeindex = str.indexOf('/');
		if ( rangeindex == -1 ) {
			if ( !parseField(str.substring(0, str.length()), destarray, min, max, -1) )
				return false;
		}
		else {
			try {
				range = Integer.parseInt(str.substring(rangeindex+1, str.length()));
			} catch(NumberFormatException e) {
				return false;
			}
			if ( range <= 0 )
				return false;
			if ( !parseField(str.substring(0, rangeindex), destarray, min, max, range) )
				return false;
		}
		return true;
	}

	/**
	 * Parse Comma Separated List
	 */
	private boolean parseList(String str, byte[] destarray, int min, int max) {
		int indexprev = 0;
		int index;
		boolean b = true;
		while ( b ) {
			index = str.indexOf(",", indexprev);
			if ( index == -1 )
				index = str.length();
			if ( index <= indexprev ) {
				b = false;
			}
			else {
				if ( !parseEntry(str.substring(indexprev, index), destarray, min, max) )
					return false;
				index++;
				indexprev = index;
			}
		}
		return true;
	}

	/**
	 * Parse Month String Literal (jan feb mar apr may jun jul aug sep oct nov dec)
	 */
	private boolean parseMonthString(String str, byte[] destarray) {
		for(int i=0; i<months.length; i++) {
			if ( str.compareToIgnoreCase(months[i]) == 0 ) {
				month[i] = 1;
				return true;
			}
		}
		return false;
	}

	/**
	 * Parse Week Day String Literal (sun mon tue wed thu fri sat)
	 */
	private boolean parseDayString(String str, byte[] destarray) {
		for(int i=0; i<days.length; i++) {
			if ( str.compareToIgnoreCase(days[i]) == 0 ) {
				dayofweek[i] = 1;
				return true;
			}
		}
		return false;
	}

	/**
	 * Parse Minute
	 */
	private boolean parseMinute(String str) {
		if ( str.length() <= 0 )
			return false;
		if ( str.startsWith("*") ) {
			if ( str.indexOf('/',1) != 1 ) {
				for(int i=0; i<60; i++)
					minute[i] = 1;
				minuteAll = true;
				return true;
			}
			else {
				return parseWildRange(str.substring(2, str.length()), minute, 0, 59);
			}
		}
		else
		{
			return parseList(str, minute, 0, 59);
		}
	}

	/**
	 * Parse Hour
	 */
	private boolean parseHour(String str) {
		if ( str.length() <= 0 )
			return false;
		if ( str.startsWith("*") ) {
			if ( str.indexOf('/',1) != 1 ) {
				for(int i=0; i<24; i++)
					hour[i] = 1;
				hourAll = true;
				return true;
			}
			else {
				return parseWildRange(str.substring(2, str.length()), hour, 0, 23);
			}
		}
		else
		{
			return parseList(str, hour, 0, 23);
		}
	}

	/**
	 * Parse Day of Month
	 */
	private boolean parseDayOfMonth(String str) {
		if ( str.length() <= 0 )
			return false;
		if ( str.startsWith("*") ) {
			if ( str.indexOf('/',1) != 1 ) {
				for(int i=0; i<31; i++)
					dayofmonth[i] = 1;
				dayofmonthAll = true;
				return true;
			}
			else {
				return parseWildRange(str.substring(2, str.length()), dayofmonth, 1, 31);
			}
		}
		else
		{
			return parseList(str, dayofmonth, 1, 31);
		}
	}

	/**
	 * Parse Month
	 */
	private boolean parseMonth(String str) {
		if ( str.length() <= 0 )
			return false;
		if ( str.startsWith("*") ) {
			if ( str.indexOf('/',1) != 1 ) {
				for(int i=0; i<12; i++)
					month[i] = 1;
				monthAll = true;
				return true;
			}
			else {
				return parseWildRange(str.substring(2, str.length()), month, 1, 12);
			}
		}
		else
		{
			if ( Character.isLetter(str.charAt(0)) ) {
				return parseMonthString(str, month);
			}
			else {
				return parseList(str, month, 1, 12);
			}
		}
	}

	/**
	 * Parse Day of Week.
	 */
	private boolean parseDayOfWeek(String str) {
		if ( str.length() <= 0 )
			return false;
		if ( str.startsWith("*") ) {
			if ( str.indexOf('/',1) != 1 ) {
				for(int i=0; i<7; i++)
					dayofweek[i] = 1;
				dayofweekAll = true;
				return true;
			}
			else {
				return parseWildRange(str.substring(2, str.length()), dayofweek, 0, 7);
			}
		}
		else
		{
			if ( Character.isLetter(str.charAt(0)) ) {
				return parseDayString(str, dayofweek);
			}
			else {
				return parseList(str, dayofweek, 0, 7);
			}
		}
	}

}
