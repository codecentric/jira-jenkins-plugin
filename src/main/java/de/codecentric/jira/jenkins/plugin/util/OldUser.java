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

import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.opensymphony.user.User;

/**
 *	User specific actions for jiraversions prior to jira 4.3
 */
public class OldUser {

	/**
	 * Get I18nHelper if jiraversion is <4.3
	 */
	public static I18nHelper getI18nHelper(JiraAuthenticationContext authenticationContext){
		User user = authenticationContext.getUser();
		return new I18nBean(user);
	}
	
	/**
	 * Check if the user is a member of the group "jira-administrators"
	 */
	public static boolean checkAdminOld(JiraAuthenticationContext authenticationContext){
		String username = authenticationContext.getUser().getFullName();
        if (username != null && (!authenticationContext.getUser().inGroup("jira-administrators")))
        {
        	return true;
        }else{
        	return false;
        }
	}
}
