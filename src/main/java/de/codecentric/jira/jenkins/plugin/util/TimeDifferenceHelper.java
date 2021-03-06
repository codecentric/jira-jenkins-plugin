/**
 * Copyright 2012 codecentric GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package de.codecentric.jira.jenkins.plugin.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import com.atlassian.jira.util.I18nHelper;

/**
 * Helper Class that is dealing with formatting time differences according the
 * I18NHelper passed into the constructor
 */

public class TimeDifferenceHelper {

	/** one second in milliseconds **/
	private static final long SECOND = 1000L;

	/** one minute in milliseconds **/
	private static final long MINUTE = SECOND * 60L;

	/** one hour in milliseconds **/
	private static final long HOUR = MINUTE * 60L;

	/** one day in milliseconds **/
	private static final long DAY = HOUR * 24L;

	/** one week in milliseconds **/
	private static final long WEEK = DAY * 7L;

	private final I18nHelper i18nHelper;

	public TimeDifferenceHelper(I18nHelper i18nHelper) {
		this.i18nHelper = i18nHelper;
	}

	/**
	 * @param startMillis
	 * @param endMillis
	 * @return a textual (not yet fully) localized representation of the time
	 *         difference
	 */
	public String getTimeDifferenceInWords(long startMillis, long endMillis) {
		long timeDiff = endMillis - startMillis;
		Date timestamp = new Date(startMillis);
		if (timeDiff < MINUTE) {
			// show seconds
			return formatPeriod1(timeDiff, SECOND, "Second");
		} else if (timeDiff < HOUR) {
			// show minutes and seconds
			return formatPeriod2(timeDiff, MINUTE, "Minute", SECOND, "Second");
		} else if (timeDiff < DAY) {
			// show hours and minutes
			return formatPeriod2(timeDiff, HOUR, "Hour", MINUTE, "Minute");
		} else if (timeDiff < WEEK) {
			// show days and hours
			return formatPeriod2(timeDiff, DAY, "Day", HOUR, "Hour");
		} else
		/* dateAndTime + 1 month >= currentTime */
		if (DateUtils.addMonths(timestamp, 1).after(new Date(endMillis))) {
			return formatPeriod2(timeDiff, WEEK, "Week", DAY, "Day");
		} else
		/* dateAndTime + 1 year >= currentTime */
		if (DateUtils.addYears(timestamp, 1).after(new Date(endMillis))) {
			if (DateUtils.addMonths(timestamp, 2).after(new Date(endMillis))) {
				// not more than two months, use singular
				startMillis = DateUtils.addMonths(timestamp, 1).getTime();
				timeDiff = endMillis - startMillis;
				String days = formatPeriod1(timeDiff, DAY, "Day");
				return "1 " + getPlural(1, "Month") + (!StringUtils.isEmpty(days) ? " " + days : "");
			}
			// show months and day(s)
			String monthsString = formatMonths(startMillis, endMillis);
			startMillis = DateUtils.addMonths(timestamp, countMonths(startMillis, endMillis)).getTime();
			timeDiff = endMillis - startMillis;
			String days = formatPeriod1(timeDiff, DAY, "Day");
			return monthsString + (!StringUtils.isEmpty(days) ? " " + days : "");
		} else {
			// show year and months
			if (DateUtils.addYears(timestamp, 2).after(new Date(endMillis))) {
				// not more than two years, use singular
				startMillis = DateUtils.addYears(timestamp, 1).getTime();
				String monthsString = formatMonths(startMillis, endMillis);
				return "1 " + getPlural(1, "Year") + (!StringUtils.isEmpty(monthsString) ? " " + monthsString : "");
			}
			// show years and months
			String yearString = formatYears(startMillis, endMillis);
			String monthsString = formatMonths(startMillis, endMillis);
			return yearString + (!StringUtils.isEmpty(monthsString) ? " " + monthsString : "");
		}
	}

	private String formatMonths(long startMillis, long endMillis) {
		int months = countMonths(startMillis, endMillis);
		return months == 0 ? "" : months + " " + getPlural(months, "Month");
	}

	private int countMonths(long startMillis, long endMillis) {
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis(startMillis);
		Calendar end = Calendar.getInstance();
		end.setTimeInMillis(endMillis);
		int month;
		if(start.get(Calendar.DAY_OF_MONTH)>end.get(Calendar.DAY_OF_MONTH)){
			month = end.get(Calendar.MONTH) - start.get(Calendar.MONTH) - 1;
		}else{
			month = end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
		}
		if(month < 0){
			return month + 12;
		}else{
			return month;
		}
	}

	private String formatYears(long startMillis, long endMillis) {
		int years = countYears(startMillis, endMillis);
		return years == 0 ? "" : years + " " + getPlural(years, "Year");
	}

	private int countYears(long startMillis, long endMillis) {
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis(startMillis);
		Calendar end = Calendar.getInstance();
		end.setTimeInMillis(endMillis);
		int years; 
		if(start.get(Calendar.MONTH)>end.get(Calendar.MONTH)){
			years = end.get(Calendar.YEAR) - start.get(Calendar.YEAR) - 1;
		}else{
			years = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
		}
		return years;
	}

	private String formatPeriod1(long timeDiff, long bigGranAmount, String bigGranIdentifier) {
		return formatPeriod2(timeDiff, bigGranAmount, bigGranIdentifier, Long.MAX_VALUE, null);
	}

	private String formatPeriod2(long timeDiff, long bigGranAmount, String bigGranIdentifier, long smallGranAmount,
			String smallGranIdentifier) {
		int bigGranularity = (int) (timeDiff / bigGranAmount);
		String bigIdent = getPlural(bigGranularity, bigGranIdentifier);
		int smallGranularity = (int) ((timeDiff - (bigGranularity * bigGranAmount)) / smallGranAmount);
		// show days and hours
		if (smallGranularity == 0) {
			if (bigGranularity == 0) {
				return "";
			}
			return String.format("%d %s", bigGranularity, bigIdent);
		} else {
			String smallIdent = getPlural(smallGranularity, smallGranIdentifier);
			return String.format("%d %s %d %s", bigGranularity, bigIdent, smallGranularity, smallIdent);
		}
	}

	private String getPlural(int bigGranularity, String bigGranIdentifier) {
		if (bigGranIdentifier != null) {
			bigGranIdentifier = bigGranIdentifier.toLowerCase();
		}
		if (bigGranularity == 1) {
			return i18nHelper.getText("timeperiod." + bigGranIdentifier + ".singular");
		} else {
			return i18nHelper.getText("timeperiod." + bigGranIdentifier + ".plural");
		}
	}

}
