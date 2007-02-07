/*
 * Written by Nicholas Clarke (nclarke@diku.dk)
 *
 * History:
 * 12-May-2000 : Last recorded change.
 * 24-Feb-2001 : Extends CronEvent.
 * 25-Feb-2001 : addEvent changed.
 *
 */

package com.antiaction.common.cron;

import java.util.*;

/**
 * Class for maintaining a group of cron events as one.
 *
 * @version 1.00
 * @author Nicholas Clarke
 */
public class CronEventGroup extends CronEvent {

	/**
	 * ArrayList of cron events.
	 */
	protected ArrayList eventsList;

	/**
	 * Creates and initializes an empty cron group.
	 * @see dk.periskop.cron.CronEvent#parseEventString(String)
	 * @see #addEvent(String)
	 */
	public CronEventGroup() {
		eventsList = new ArrayList();
	}

	/**
	 * Resets internal state.
	 */
	public void reset() {
		super.reset();
		eventsList = new ArrayList();
		valid = true;
	}

	/**
	 * Tests if this groups list has no elements.
	 * @return true if this groups list has no elements; false otherwise.
	 */
	public boolean isEmpty() {
		return eventsList.isEmpty();
	}

	/**
	 * Parses, validates and adds a cron event to this group if it is valid.
	 * @param str a string representation of the event.
	 * @return a boolean value as to wether the cron event was valid.
	 * @see dk.periskop.cron.CronEvent#parseEventString(String)
	 */
	public boolean addEvent(CronEvent event) {
		if ( !event.isValid() )
			return false;
		eventsList.add(event);
		return true;
	}

	/**
	 * Returns a copy of the list of cron events managed by this group.
	 * @return a copy of the list of cron events managed by this group.
	 */
	public ArrayList getEventsList() {
		return (ArrayList)eventsList.clone();
	}

	/**
	 * Returns a copy of the list of cron events managed by this group.
	 * @return a copy of the list of cron events managed by this group.
	 */
	public void setEventsList(ArrayList eL) {
		eventsList = (ArrayList)eL.clone();
	}

	/**
	 * Computes and returns an ArrayList of DateTime objects, each DateTime
	 * object is equal to an event due on the date specified.  Mergesort is
	 * used to merge the different events managed by this group. The DateTime
	 * objects are returned in ascending order, from first to last event.
	 * @param refDate the reference date used to select the event(s) on that specific date.
	 * @return arrayList of datetime objects equal to each of the events that are due for the specified date.
	 */
	public ArrayList getEvents(Calendar refDate) {
		ArrayList retarray;
		ArrayList arrayptr1;
		ArrayList arrayptr2;
		Calendar tmpdate1;
		Calendar tmpdate2;
		long lastEventLong;
		long tmpLong1;
		long tmpLong2;
		if ( eventsList.isEmpty() ) {
			retarray = new ArrayList();
		}
		else {
			int i = 0;
			retarray = (ArrayList)((CronEvent)eventsList.get(i)).getEvents(refDate).clone();
			for(i = 1; i<eventsList.size(); i++) {
				arrayptr1 = retarray;
				arrayptr2 = ((CronEvent)eventsList.get(i)).getEvents(refDate);
				if ( arrayptr1.isEmpty() ) {
					retarray = (ArrayList)arrayptr2.clone();
				}
				else if ( arrayptr2.isEmpty() ) {
					retarray = (ArrayList)arrayptr1.clone();
				}
				else {
					lastEventLong = 0;
					retarray = new ArrayList();
					while ( !arrayptr1.isEmpty() || !arrayptr2.isEmpty() ) {
						if ( arrayptr1.isEmpty() ) {
							tmpdate2 = (Calendar)arrayptr2.get(0);
							arrayptr2.remove(0);
							tmpLong2 = tmpdate2.getTime().getTime();
							if ( tmpLong2 > lastEventLong ) {
								retarray.add(tmpdate2);
								lastEventLong = tmpLong2;
							}
						}
						else if ( arrayptr2.isEmpty() ) {
							tmpdate1 = (Calendar)arrayptr1.get(0);
							arrayptr1.remove(0);
							tmpLong1 = tmpdate1.getTime().getTime();
							if ( tmpLong1 > lastEventLong ) {
								retarray.add(tmpdate1);
								lastEventLong = tmpLong1;
							}
						}
						else {
							tmpdate1 = (Calendar)arrayptr1.get(0);
							tmpdate2 = (Calendar)arrayptr2.get(0);
							tmpLong1 = tmpdate1.getTime().getTime();
							tmpLong2 = tmpdate2.getTime().getTime();
							if ( tmpLong1 <= tmpLong2 ) {
								arrayptr1.remove(0);
								if ( tmpLong1 > lastEventLong ) {
									retarray.add(tmpdate1);
									lastEventLong = tmpLong1;
								}
							}
							else {
								arrayptr2.remove(0);
								if ( tmpLong2 > lastEventLong ) {
									retarray.add(tmpdate2);
									lastEventLong = tmpLong2;
								}
							}
						}
					}
				}
			}
		}
		return retarray;
	}

}
