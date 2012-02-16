package de.codecentric.jira.jenkins.plugin.servlet;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import de.codecentric.jira.jenkins.plugin.conditions.IsPriorToJiraVersion;
import de.codecentric.jira.jenkins.plugin.model.JenkinsServer;
import de.codecentric.jira.jenkins.plugin.model.ServerList;
import de.codecentric.jira.jenkins.plugin.util.NewUser;
import de.codecentric.jira.jenkins.plugin.util.OldUser;

public class JenkinsInstancesServlet extends HttpServlet {
    private static final String TEMPLATE_PATH = "/templates/config.vm";
    private final JiraAuthenticationContext authenticationContext;
    private final TemplateRenderer templateRenderer;
    private final ServerList server;
    private final UserManager userManager;
    private final boolean old;
	
	public JenkinsInstancesServlet(TemplateRenderer templateRenderer, UserManager userManager, JiraAuthenticationContext authenticationContext, PluginSettingsFactory settingsFactory, ApplicationProperties applicationProperties) {
        this.templateRenderer = templateRenderer;  
        this.authenticationContext = authenticationContext;
        this.server = new ServerList(settingsFactory);
        this.userManager = userManager;
        
        //test if jiraversion < 4.3
        IsPriorToJiraVersion isPrior = new IsPriorToJiraVersion(applicationProperties);
        isPrior.setmaxMajorVersion(4);
        isPrior.setmaxMinorVersion(3);
        this.old = isPrior.shouldDisplay(null);
    }
	
	/**
     * Save Entity Server to db.
     * Requires name and url for Server.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	if(old){
    		if (OldUser.checkAdminOld(authenticationContext))
            {
            	response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
    	}else{
    		if (NewUser.checkAdminNew(userManager, request))
            {
            	response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
    	}
    	String name = request.getParameter("name");
    	String url = request.getParameter("url");
    	String mode = request.getParameter("mode");
    	if(mode.equals("add")){
    		if(!name.equals("") && !url.equals("")){
    			server.add(name, url);
    		}	
    	}else if(mode.equals("del")){
    		if(!name.equals("")){
    			server.del(name);
    		}
    	}
 
        response.sendRedirect(request.getContextPath() + "/plugins/servlet/jenkinsinstances");
    }
    
    /**
     * This function renders the template
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
    	if(old){
    		if (OldUser.checkAdminOld(authenticationContext))
            {
            	resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
    	}else{
    		if (NewUser.checkAdminNew(userManager, req))
            {
            	resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
    	}
    	
    	Map<String, Object> velocityValues = new HashMap<String, Object>();
    	
    	List<JenkinsServer> serverList = server.getServerList();
    	
		velocityValues.put("context", this.getServletContext());
		velocityValues.put("jenkinsinstances", this);
		velocityValues.put("serverList", serverList);
		
		resp.setContentType("text/html;charset=utf-8"); 
		templateRenderer.render(TEMPLATE_PATH, velocityValues, resp.getWriter());	
    }
}
