/*
 * Cron Schedule, common interface.
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
 * 10-Nov-2001 : First implementation.
 *               Moved some static fields from CronEvent.
 */

package com.antiaction.common.cron;

/**
 * Interface for cron events.
 *
 * @version 2.00
 * @author Nicholas Clarke <nclarke@diku.dk>
 */
public interface Schedule {

	/** Month string array. */
	public static String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
	/** Weekday string array. */
	public static String[] days = {"sun", "mon", "tue", "wed", "thu", "fri", "sat"};

	// ...

	/**
	 * Returns a boolean value as to wether this cron event is valid.
	 * @return a boolean value as to wether this cron event is valid.
	 */
//	public boolean isValid() {
//		return valid;
//	}

	/**
	 * Tests if event is empty.
	 * @return true if this event is empty.
	 */
//	public boolean isEmpty() {
//		return false;
//	}

}
