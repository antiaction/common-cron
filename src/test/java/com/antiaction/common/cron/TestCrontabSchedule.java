/*
 * Created on 03/09/2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package com.antiaction.common.cron;

import java.util.Calendar;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestCrontabSchedule extends TestCase {

	public void testCrontabSchedule() {
		Assert.assertEquals( 0, ScheduleAbstract.weekdayToInt( Calendar.SUNDAY ) );
		Assert.assertEquals( 1, ScheduleAbstract.weekdayToInt( Calendar.MONDAY ) );
		Assert.assertEquals( 2, ScheduleAbstract.weekdayToInt( Calendar.TUESDAY ) );
		Assert.assertEquals( 3, ScheduleAbstract.weekdayToInt( Calendar.WEDNESDAY ) );
		Assert.assertEquals( 4, ScheduleAbstract.weekdayToInt( Calendar.THURSDAY ) );
		Assert.assertEquals( 5, ScheduleAbstract.weekdayToInt( Calendar.FRIDAY ) );
		Assert.assertEquals( 6, ScheduleAbstract.weekdayToInt( Calendar.SATURDAY ) );

		Assert.assertEquals( 0, ScheduleAbstract.monthToInt( Calendar.JANUARY ) );
		Assert.assertEquals( 1, ScheduleAbstract.monthToInt( Calendar.FEBRUARY ) );
		Assert.assertEquals( 2, ScheduleAbstract.monthToInt( Calendar.MARCH ) );
		Assert.assertEquals( 3, ScheduleAbstract.monthToInt( Calendar.APRIL ) );
		Assert.assertEquals( 4, ScheduleAbstract.monthToInt( Calendar.MAY ) );
		Assert.assertEquals( 5, ScheduleAbstract.monthToInt( Calendar.JUNE ) );
		Assert.assertEquals( 6, ScheduleAbstract.monthToInt( Calendar.JULY ) );
		Assert.assertEquals( 7, ScheduleAbstract.monthToInt( Calendar.AUGUST ) );
		Assert.assertEquals( 8, ScheduleAbstract.monthToInt( Calendar.OCTOBER ) );
		Assert.assertEquals( 9, ScheduleAbstract.monthToInt( Calendar.SEPTEMBER ) );
		Assert.assertEquals( 10, ScheduleAbstract.monthToInt( Calendar.NOVEMBER ) );
		Assert.assertEquals( 11, ScheduleAbstract.monthToInt( Calendar.DECEMBER ) );
		Assert.assertEquals( 11, ScheduleAbstract.monthToInt( Calendar.UNDECIMBER ) );

		ScheduleAbstract cronSchedule = new ScheduleAbstract() {
		};

        ScheduleAbstract emailSchedule = CrontabSchedule.crontabFactory("*/10 * * * *");

        ScheduleAbstract lookupSchedule = CrontabSchedule.crontabFactory("0 0 * * *");

        List emailSchedules = emailSchedule.getScheduleList(System.currentTimeMillis());
        for (int i=0; i<emailSchedules.size(); ++i) {
        	System.out.println(emailSchedules.get(i));
        }

        List lookupSchedules = lookupSchedule.getScheduleList(System.currentTimeMillis());
        for (int i=0; i<lookupSchedules.size(); ++i) {
        	System.out.println(lookupSchedules.get(i));
        }
	}

}
