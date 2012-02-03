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
package de.codecentric.jira.jenkins.plugin.model;

import java.util.Date;

import com.atlassian.jira.util.I18nHelper;

import de.codecentric.jira.jenkins.plugin.util.TimeDifferenceHelper;

/**
 * This class represents a Build from the Jenkins Server.
 */
public class JenkinsBuild {

	/**
	 * build representing unknown state build which can be used when
	 * constructing a build fails
	 */
	public static final JenkinsBuild UNKNOWN = new JenkinsBuild("UNKNOWN", "./.", "grey", ".", new Date());

	private String name;
	private String number;
	private String color;
	private String url;
	private Date timestamp;

	private TimeDifferenceHelper timeDifferenceHelper;

	public JenkinsBuild() {
	}

	/**
	 * Sets the values and convert a timestamp in a more user friendly time
	 * format.
	 * 
	 * @param name
	 *            Name of the build
	 * @param number
	 *            Build ID
	 * @param color
	 *            color used to display this build. Consider calling set Result
	 * @param url
	 *            URL of the build on the Jenkins Server.
	 * @param timestamp
	 *            A timestamp when the build started.
	 */
	public JenkinsBuild(String name, String number, String color, String url, Date timestamp) {
		setName(name);
		setNumber(number);
		setColor(color);
		setUrl(url);
		setTimestamp(timestamp);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getNumber() {
		return number;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	/**
	 * sets the build color by proving a BuildResult
	 * 
	 * @param result
	 *            Buildresult used to determine color
	 */
	public void setResult(BuildResult result) {
		switch (result) {
		case Stabil:
		case SUCCESS:
			color = "blue";
			break;
		case ABORTED:
		case NOT_BUILT:
			color = "grey";
			break;
		case FAILURE:
			color = "red";
			break;
		case UNSTABLE:
			color = "yellow";
			break;
		default:
			color = "";
		}
	}

	public void setI18nHelper(I18nHelper i18nHelper) {
		this.timeDifferenceHelper = new TimeDifferenceHelper(i18nHelper);
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = new Date(timestamp.getTime());
	}

	/**
	 * Get the time when the build started.
	 * 
	 * @return dateAndTime Time when the build started.
	 */
	public Date getTimestamp() {
		return new Date(timestamp.getTime());
	}

	/**
	 * @return String representation of the time elapsed from build time to now
	 */
	public String getTimestampFromNow() {
		return getTimestampFromEndtime(System.currentTimeMillis());
	}

	/**
	 * @param endMillis
	 *            Timestamp to calculate elapsed time to
	 * 
	 * @return String representation of the time elapsed from build time to the
	 *         given timestamp
	 */
	public String getTimestampFromEndtime(long endMillis) {
		long startMillis = timestamp.getTime();
		return timeDifferenceHelper.getTimeDifferenceInWords(startMillis, endMillis);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JenkinsBuild other = (JenkinsBuild) obj;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}
