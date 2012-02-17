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

import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.sal.api.user.UserManager;

/**
 *	User specific actions for jiraversions 4.3 and later
 */
public class NewUser {
	
	/**
	 * Get I18nHelper if jiraversion is >=4.3
	 */
	public static I18nHelper getI18nHelper(JiraAuthenticationContext authenticationContext){
		User user = authenticationContext.getLoggedInUser();
		return new I18nBean(user);
	}
	
	/**
	 * Check if user is system-administrator or administrator
	 */
	public static boolean checkAdminNew(UserManager userManager, HttpServletRequest req){
		String username = userManager.getRemoteUsername(req);
	    if (username != null && (!userManager.isSystemAdmin(username) || !userManager.isAdmin(username)))
	    {	//user is not admin
        	return false;
        }else{
        	return true;
        }
	}
}
