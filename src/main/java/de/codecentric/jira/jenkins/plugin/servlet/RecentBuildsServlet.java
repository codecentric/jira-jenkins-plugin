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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.opensymphony.user.User;

import de.codecentric.jira.jenkins.plugin.conditions.IsPriorToJiraVersion;
import de.codecentric.jira.jenkins.plugin.model.BuildResult;
import de.codecentric.jira.jenkins.plugin.model.JenkinsBuild;
import de.codecentric.jira.jenkins.plugin.model.JenkinsServer;
import de.codecentric.jira.jenkins.plugin.model.ServerList;
import de.codecentric.jira.jenkins.plugin.util.NewUser;
import de.codecentric.jira.jenkins.plugin.util.OldUser;
import de.codecentric.jira.jenkins.plugin.util.URLEncoder;

/**
 * This class gets information about jobs, views and builds from the Jenkins
 * Server and displays it. The data is collected over the Jenkins XML API.
 */
public class RecentBuildsServlet extends HttpServlet {
	
	public static final String RSS_ALL = "/rssAll";
	public static final String RSS_FAILS = "/rssFailed";
	public static final String RSS_LATEST = "/rssLatest";
	
    private static final String TEMPLATE_PATH = "/templates/recentbuilds.vm";
	private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext authenticationContext;
    private final boolean old;
    
    private ServerList serverList;
    private HttpClient client;
    private Credentials defaultcreds;
    
    public RecentBuildsServlet(TemplateRenderer templateRenderer, JiraAuthenticationContext authenticationContext, PluginSettingsFactory settingsFactory, ApplicationProperties applicationProperties) {
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.client = new HttpClient(new MultiThreadedHttpConnectionManager());
        this.serverList = new ServerList(settingsFactory);
    	  
        //test if jiraversion < 4.3
        IsPriorToJiraVersion isPrior = new IsPriorToJiraVersion(applicationProperties);
        isPrior.setmaxMajorVersion(4);
        isPrior.setmaxMinorVersion(3);
        this.old = isPrior.shouldDisplay(null);
        
    	client.getParams().setAuthenticationPreemptive(true);
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
		List<JenkinsBuild> builds = new ArrayList<JenkinsBuild>();

		I18nHelper i18nHelper;
    	if(old){
    		i18nHelper = OldUser.getI18nHelper(authenticationContext);
    	}else{
    		i18nHelper = NewUser.getI18nHelper(authenticationContext);
    	}

		try {
			String urlJenkinsServer = req.getParameter("jenkinsUrl");
			String view = req.getParameter("view");
			String job = req.getParameter("job");
			int maxBuilds = Integer.parseInt(req.getParameter("maxBuilds"));
			String userName = req.getParameter("userName");
			String password = req.getParameter("password");

			//check if urlJenkinsServer equals Server.name
			JenkinsServer server = serverList.find(urlJenkinsServer);
			if(server!=null){
				urlJenkinsServer = server.getUrl();
			}
			
			if (urlJenkinsServer.lastIndexOf('/') < urlJenkinsServer.length()-1) {
				urlJenkinsServer += "/";
			}
			
			Document buildRss;
			
			//Test if authorization is available
			if(userName=="" && password==""){
				buildRss = getBuildRss(urlJenkinsServer, view, job);
				velocityValues.put("user", "anonymous");
			}else{
				defaultcreds = new UsernamePasswordCredentials(userName, password);
		    	client.getState().setCredentials(new AuthScope(null, -1, AuthScope.ANY_REALM), defaultcreds);
		    	
		    	buildRss = getBuildRssAuth(urlJenkinsServer, view, job);
		    	
		    	velocityValues.put("user", userName);
			}
	    	
			builds = readBuilds(buildRss, maxBuilds, i18nHelper);
			
			velocityValues.put("serverList", serverList.getServerList());
			velocityValues.put("view", view);
			velocityValues.put("job", job);
			velocityValues.put("jenkinsUrl", urlJenkinsServer);
			velocityValues.put("builds", builds);
			velocityValues.put("context", this.getServletContext());
			

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (NumberFormatException e){
			e.printStackTrace();
		}
		
		resp.setContentType("text/html;charset=utf-8");
        templateRenderer.render(TEMPLATE_PATH, velocityValues, resp.getWriter());		
    }
    
    private List<JenkinsBuild> readBuilds(Document buildRss, int maxBuilds, I18nHelper i18nHelper) {
    	List<JenkinsBuild> builds = new ArrayList<JenkinsBuild>();
    	
    	//if jenkinsUrl is invalid but authorization was provided
    	if(buildRss==null){
    		return builds;
    	}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z");
		List<Element> elements = buildRss.getRootElement().elements("entry");
		Iterator<Element> it = elements.iterator();
		while (it.hasNext() && builds.size() < maxBuilds) {
			Element rssElement = it.next();
			JenkinsBuild build = new JenkinsBuild();
			build.setI18nHelper(i18nHelper);
			String title = rssElement.elementText("title");
			build.setName(title);
			String url = rssElement.element("link").attributeValue("href");
			build.setUrl(url);
			try {
				String publishedDate = rssElement.elementText("published");
				build.setTimestamp(df.parse(publishedDate.substring(0, publishedDate.length() - 1) + " UTC"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			build.setNumber(url.substring(url.lastIndexOf('/', url.length() - 2) + 1, url.length() - 1));
			String buildResult = title.substring(title.lastIndexOf('(') + 1, title.lastIndexOf(')'));

			if(buildResult.equals("Wieder normal")){
				buildResult = "Stabil";
			}else if(buildResult.substring(0, 6).equals("Defekt")){
				buildResult = "FAILURE";
			}
			build.setResult(BuildResult.valueOf(buildResult));
			builds.add(build);
		}

		return builds;
	}
    
    /**
	 * Gets BuldRss if authorization is required
	 * @return BuildRss
	 */
	private Document getBuildRssAuth(String urlJenkinsServer, String view, String job) throws MalformedURLException,
			DocumentException {
		String url;
		// was there a certain job specified?
		if (StringUtils.isNotEmpty(job)) {
			url = (urlJenkinsServer + "job/" + URLEncoder.encodeForURL(job) + RSS_ALL);
		} else if (StringUtils.isNotEmpty(view)) {
			url = (urlJenkinsServer + "view/" + URLEncoder.encodeForURL(view) + RSS_ALL);
		} else {
			url = (urlJenkinsServer + RSS_ALL);
		}
		
		PostMethod post = new PostMethod(url);
		Document buildRss = null;
		
		try{
			client.executeMethod(post);
			buildRss = new SAXReader().read(post.getResponseBodyAsStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			post.releaseConnection(); 
		}
		
		return buildRss;
	}
	
	/**
	 * Gets BuldRss if no authorization is required
	 * @return BuildRss
	 */
	private static Document getBuildRss(String urlJenkinsServer, String view, String job) throws MalformedURLException,
	DocumentException {
	URL url;
	// was there a certain job specified?
	if (StringUtils.isNotEmpty(job)) {
		url = new URL(urlJenkinsServer + "job/" + URLEncoder.encodeForURL(job) + RSS_ALL);
	} else if (StringUtils.isNotEmpty(view)) {
		url = new URL(urlJenkinsServer + "view/" + URLEncoder.encodeForURL(view) + RSS_ALL);
	} else {
		url = new URL(urlJenkinsServer + RSS_ALL);
	}
	return new SAXReader().read(url);
	}
	
}
