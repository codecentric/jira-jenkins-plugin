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

import static de.codecentric.jira.jenkins.plugin.util.URLEncoder.encodeForURL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;

import de.codecentric.jira.jenkins.plugin.conditions.IsPriorToJiraVersion;
import de.codecentric.jira.jenkins.plugin.model.JenkinsServer;
import de.codecentric.jira.jenkins.plugin.model.ServerList;
import de.codecentric.jira.jenkins.plugin.util.NewUser;
import de.codecentric.jira.jenkins.plugin.util.OldUser;

/**
 *	This class is used to show a graphic from the Jenkins Server.
 */

public class ChartServlet extends HttpServlet {
	
	private static final long serialVersionUID = 3395705317898265220L;
	private static final String TEMPLATE_PATH = "/templates/chart.vm";
    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext authenticationContext;
    private final boolean old;
    
    private ServerList serverList;
 
    public ChartServlet(TemplateRenderer templateRenderer, JiraAuthenticationContext authenticationContext, PluginSettingsFactory settingsFactory, ApplicationProperties applicationProperties) {
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.serverList = new ServerList(settingsFactory);
        
        //test if jiraversion < 4.3
        IsPriorToJiraVersion isPrior = new IsPriorToJiraVersion(applicationProperties);
        isPrior.setmaxMajorVersion(4);
        isPrior.setmaxMinorVersion(3);
        this.old = isPrior.shouldDisplay(null);  
    }
    
    /**
     * This function takes post data and calls doGet()
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }
    
    /**
     * This function takes get data and renders the template
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
    	Map<String, Object> velocityValues = new HashMap<String, Object>();
    	
    	I18nHelper i18nHelper;
    	if(old){
    		i18nHelper = OldUser.getI18nHelper(authenticationContext);
    	}else{
    		i18nHelper = NewUser.getI18nHelper(authenticationContext);
    	}

		String urlJenkinsServer = req.getParameter("jenkinsUrl");
		String jenkinsJob = req.getParameter("job");
		String trendPropKey = "gadget.chart.trend." + req.getParameter("trend");
		String type = "";
		int hight = Integer.parseInt(req.getParameter("hight"));
		int width = Integer.parseInt(req.getParameter("width"));
		
		String trendURL = i18nHelper.getText(trendPropKey + ".key");
		String trendTitle = i18nHelper.getText(trendPropKey + ".description");
		
		//check if urlJenkinsServer equals Server.name
		JenkinsServer server = serverList.find(urlJenkinsServer);
		if(server!=null){
			urlJenkinsServer = server.getUrl();
		}
		
		if (urlJenkinsServer.lastIndexOf('/') < urlJenkinsServer.length()-1) {
			urlJenkinsServer += "/";
		}

		String trendUrl = urlJenkinsServer + "job/" + encodeForURL(jenkinsJob) + "/" + i18nHelper.getText(trendURL);

		if (StringUtils.isNotEmpty(type)) {
			urlJenkinsServer += "?" + type;
		}

		velocityValues.put("serverList", serverList.getServerList());
		velocityValues.put("jenkinsUrl", urlJenkinsServer);
		velocityValues.put("job", jenkinsJob);
		velocityValues.put("trendUrl", trendUrl);
		velocityValues.put("trend", trendURL);
		velocityValues.put("title", trendTitle);
		velocityValues.put("hight", hight);
		velocityValues.put("width", width);
		
		resp.setContentType("text/html;charset=utf-8");  
        templateRenderer.render(TEMPLATE_PATH, velocityValues, resp.getWriter());  
    }    
}
