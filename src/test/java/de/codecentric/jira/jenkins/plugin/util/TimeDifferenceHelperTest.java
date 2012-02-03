package de.codecentric.jira.jenkins.plugin.util;

import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.lang.NotImplementedException;

import com.atlassian.jira.util.I18nHelper;

public class TimeDifferenceHelperTest extends TestCase {

	private final ResourceBundle rb = ResourceBundle.getBundle("de.codecentric.jira.jenkins.plugin.properties.overview",
			Locale.ENGLISH);

	private TimeDifferenceHelper timeDifferenceHelper;

	@Override
	public void setUp() {
		timeDifferenceHelper = new TimeDifferenceHelper(new I18nHelperMock(rb));
	}

	public void testGetTimestampFromNow1Second() {
		assertEquals("1 Second", getStringPeriod(1, Calendar.SECOND));
	}

	public void testGetTimestampFromNow2Seconds() {
		assertEquals("2 Seconds", getStringPeriod(2, Calendar.SECOND));
	}

	public void testGetTimestampFromNow1Minute() {
		assertEquals("1 Minute", getStringPeriod(1, Calendar.MINUTE));
	}

	public void testGetTimestampFromNow1Minute2Seconds() {
		assertEquals("1 Minute 2 Seconds", getStringPeriod(1, Calendar.MINUTE, 2, Calendar.SECOND));
	}

	public void testGetTimestampFromNow2Minute2Seconds() {
		assertEquals("2 Minutes 2 Seconds", getStringPeriod(2, Calendar.MINUTE, 2, Calendar.SECOND));
	}

	public void testGetTimestampFromNow2Minutes() {
		assertEquals("2 Minutes", getStringPeriod(2, Calendar.MINUTE));
	}

	public void testGetTimestampFromNow1Hour() {
		assertEquals("1 Hour", getStringPeriod(1, Calendar.HOUR));
	}

	public void testGetTimestampFromNow15Hours() {
		assertEquals("15 Hours", getStringPeriod(15, Calendar.HOUR));
	}

	public void testGetTimestampFromNow15Hours48Minutes() {
		assertEquals("15 Hours 48 Minutes", getStringPeriod(15, Calendar.HOUR, 48, Calendar.MINUTE));
	}

	public void testGetTimestampFromNow1Day() {
		assertEquals("1 Day", getStringPeriod(1, Calendar.DAY_OF_WEEK));
	}

	public void testGetTimestampFromNow1Week() {
		assertEquals("1 Week", getStringPeriod(7, Calendar.DAY_OF_WEEK));
	}

	public void testGetTimestampFromNow1Month() {
		assertEquals("1 Month", getStringPeriod(1, Calendar.MONTH));
	}

	public void testGetTimestampFromNow1Month1Day() {
		assertEquals("1 Month 1 Day", getStringPeriod(1, Calendar.MONTH, 1, Calendar.DAY_OF_MONTH));
	}

	public void testGetTimestampFromNow1Month2Days() {
		assertEquals("1 Month 2 Days", getStringPeriod(1, Calendar.MONTH, 2, Calendar.DAY_OF_MONTH));
	}

	public void testGetTimestampFromNow2Months1Day() {
		assertEquals("2 Months 1 Day", getStringPeriod(2, Calendar.MONTH, 1, Calendar.DAY_OF_MONTH));
	}

	public void testGetTimestampFromNow2Months2Days() {
		assertEquals("2 Months 2 Days", getStringPeriod(2, Calendar.MONTH, 2, Calendar.DAY_OF_MONTH));
	}

	public void testGetTimestampFromNow1Year() {
		assertEquals("1 Year", getStringPeriod(1, Calendar.YEAR));
	}

	public void testGetTimestampFromNow1Year1Month() {
		assertEquals("1 Year 1 Month", getStringPeriod(1, Calendar.YEAR, 1, Calendar.MONTH));
	}

	public void testGetTimestampFromNow1Year2Months() {
		assertEquals("1 Year 2 Months", getStringPeriod(1, Calendar.YEAR, 2, Calendar.MONTH));
	}

	public void testGetTimestampFromNow2Years() {
		assertEquals("2 Years", getStringPeriod(2, Calendar.YEAR));
	}

	public void testGetTimestampFromNow2Year1Month() {
		assertEquals("2 Years 1 Month", getStringPeriod(2, Calendar.YEAR, 1, Calendar.MONTH));
	}

	public void testGetTimestampFromNow2Year2Months() {
		assertEquals("2 Years 2 Months", getStringPeriod(2, Calendar.YEAR, 2, Calendar.MONTH));
	}

	private String getStringPeriod(int amount, int calendarField, int amount2, int calendarField2) {
		Calendar startDate = defaultDate();
		Calendar endDate = defaultDate();
		endDate.add(calendarField, amount);
		endDate.add(calendarField2, amount2);
		return getPeriodAsString(startDate, endDate);
	}

	private String getStringPeriod(int amount, int calendarField) {
		Calendar startDate = defaultDate();
		Calendar endDate = defaultDate();
		endDate.add(calendarField, amount);
		return getPeriodAsString(startDate, endDate);
	}

	private String getPeriodAsString(Calendar startDate, Calendar endDate) {
		long startMillis = startDate.getTimeInMillis();
		long endMillis = endDate.getTimeInMillis();
		return timeDifferenceHelper.getTimeDifferenceInWords(startMillis, endMillis);
	}

	private Calendar defaultDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
		return calendar;
	}

	private final class I18nHelperMock implements I18nHelper {
		private final ResourceBundle rb;

		private I18nHelperMock(ResourceBundle rb) {
			this.rb = rb;
		}

		/* only method required for this test */
		public String getText(String s) {
			return rb.getString(s);
		}

		public ResourceBundle getDefaultResourceBundle() {
			throw new NotImplementedException();
		}

		public Locale getLocale() {
			throw new NotImplementedException();
		}

		public String getText(String s, String s1) {
			throw new NotImplementedException();
		}

		public String getText(String s, Object obj) {
			throw new NotImplementedException();
		}

		public String getText(String s, String s1, String s2) {
			throw new NotImplementedException();
		}

		public String getText(String s, String s1, String s2, String s3) {
			throw new NotImplementedException();
		}

		public String getText(String s, String s1, String s2, String s3, String s4) {
			throw new NotImplementedException();
		}

		public String getText(String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7,
				String s8, String s9) {
			throw new NotImplementedException();
		}

		@Override
		public Set<String> getKeysForPrefix(String arg0) {
			throw new NotImplementedException();
		}

		@Override
		public String getText(String arg0, Object arg1, Object arg2, Object arg3) {
			throw new NotImplementedException();
		}

		@Override
		public String getText(String arg0, Object arg1, Object arg2,
				Object arg3, Object arg4) {
			throw new NotImplementedException();
		}

		@Override
		public String getText(String arg0, Object arg1, Object arg2,
				Object arg3, Object arg4, Object arg5) {
			throw new NotImplementedException();
		}

		@Override
		public String getText(String arg0, Object arg1, Object arg2,
				Object arg3, Object arg4, Object arg5, Object arg6) {
			throw new NotImplementedException();
		}

		@Override
		public String getText(String arg0, Object arg1, Object arg2,
				Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
			throw new NotImplementedException();
		}

		@Override
		public String getText(String arg0, String arg1, String arg2,
				String arg3, String arg4, String arg5, String arg6, String arg7) {
			throw new NotImplementedException();
		}

		@Override
		public String getText(String arg0, Object arg1, Object arg2,
				Object arg3, Object arg4, Object arg5, Object arg6,
				Object arg7, Object arg8) {
			throw new NotImplementedException();
		}

		@Override
		public String getUnescapedText(String arg0) {
			throw new NotImplementedException();
		}
	}

}