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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;

import de.codecentric.jira.jenkins.plugin.conditions.IsPriorToJiraVersion;
import de.codecentric.jira.jenkins.plugin.model.BuildType;
import de.codecentric.jira.jenkins.plugin.model.DefaultTrustManager;
import de.codecentric.jira.jenkins.plugin.model.JenkinsBuild;
import de.codecentric.jira.jenkins.plugin.model.JenkinsJob;
import de.codecentric.jira.jenkins.plugin.model.JenkinsServer;
import de.codecentric.jira.jenkins.plugin.model.ServerList;
import de.codecentric.jira.jenkins.plugin.util.NewUser;
import de.codecentric.jira.jenkins.plugin.util.OldUser;
import de.codecentric.jira.jenkins.plugin.util.URLEncoder;

/**
 * Gives a overview of the Jenkins jobs.
 * 
 */ 
public class OverviewServlet extends HttpServlet {
	
	private static final long serialVersionUID = 2927728737906256279L;
	private static final String DATE_FORMAT = "yyMMddHHmmssZ";
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);

	private static final String PARAM_JENKINS_URL = "jenkinsUrl";
	private static final String PARAM_VIEW = "view";
	
    private static final String TEMPLATE_PATH = "/templates/jenkins.vm";
    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext authenticationContext;
    private final boolean old;
    
    private ServerList serverList;
    private HttpClient client;
    private Credentials defaultcreds;
    
    public OverviewServlet(TemplateRenderer templateRenderer, JiraAuthenticationContext authenticationContext, PluginSettingsFactory settingsFactory, ApplicationProperties applicationProperties) {
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.serverList = new ServerList(settingsFactory);
        this.client = new HttpClient(new MultiThreadedHttpConnectionManager());
        
        //test if jiraversion < 4.3
        IsPriorToJiraVersion isPrior = new IsPriorToJiraVersion(applicationProperties);
        isPrior.setmaxMajorVersion(4);
        isPrior.setmaxMinorVersion(3);
        this.old = isPrior.shouldDisplay(null);
  	  
    	client.getParams().setAuthenticationPreemptive(true);
    	
    	//set SSLContext to accept all certificates
    	try{
    		SSLContext ctx = SSLContext.getInstance("TLS");
    		ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
    		SSLContext.setDefault(ctx);
    	}catch(NoSuchAlgorithmException e){
    		e.printStackTrace();
    	}catch(KeyManagementException e){
    		e.printStackTrace();
    	}
    	SecureProtocolSocketFactory secureProtocolSocketFactory = new SSLProtocolSocketFactory();

    	Protocol.registerProtocol("https", new Protocol("https",
    		   (ProtocolSocketFactory)secureProtocolSocketFactory, 443));  
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
		Map<String, Object> velocityValues = new HashMap<String, Object>();

		try {
			String urlJenkinsServer = req.getParameter(PARAM_JENKINS_URL);
			String view = req.getParameter(PARAM_VIEW);
			String userName = req.getParameter("userName");
			String password = req.getParameter("password");

			I18nHelper i18nHelper;
	    	if(old){
	    		i18nHelper = OldUser.getI18nHelper(authenticationContext);
	    	}else{
	    		i18nHelper = NewUser.getI18nHelper(authenticationContext);
	    	}
			
			//check if urlJenkinsServer equals Server.name
			JenkinsServer server = serverList.find(urlJenkinsServer);
			if(server!=null){
				urlJenkinsServer = server.getUrl();
			}
			
			if (urlJenkinsServer.lastIndexOf('/') < urlJenkinsServer.length()-1) {
				urlJenkinsServer += "/";
			}
			
			List<JenkinsJob> portletData;
			
			//Test if authorization is available
			if(userName=="" && password=="" && !urlJenkinsServer.startsWith("https")){
				portletData = getJobListByServer(urlJenkinsServer, view, i18nHelper);
				velocityValues.put("user", "anonymous");
			}else if(userName==""){
				defaultcreds = new UsernamePasswordCredentials(userName, password);
		    	client.getState().setCredentials(new AuthScope(null, -1, AuthScope.ANY_REALM), defaultcreds);

				portletData = getJobListByServerAuth(urlJenkinsServer, view, i18nHelper);
				
				velocityValues.put("user", "anonymous");
			}else{
				defaultcreds = new UsernamePasswordCredentials(userName, password);
		    	client.getState().setCredentials(new AuthScope(null, -1, AuthScope.ANY_REALM), defaultcreds);

				portletData = getJobListByServerAuth(urlJenkinsServer, view, i18nHelper);
				
				velocityValues.put("user", userName);
			}
			
			velocityValues.put("serverList", serverList.getServerList());
			velocityValues.put("jenkinsUrl", urlJenkinsServer);
			velocityValues.put("view", view);
			velocityValues.put("jobs", portletData);
			velocityValues.put("context", this.getServletContext());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		resp.setContentType("text/html;charset=utf-8");
        templateRenderer.render(TEMPLATE_PATH, velocityValues, resp.getWriter());
    }
    
    /**
	 * Creates a Jenkins Job List if authorization is required
	 * @return Jenkins Job List
	 */
    @SuppressWarnings("unchecked")
	private List<JenkinsJob> getJobListByServerAuth(String urlJenkinsServer, String view, I18nHelper i18nHelper)
			throws MalformedURLException, DocumentException {
    	
		List<JenkinsJob> jobList = new ArrayList<JenkinsJob>();

		String jobListUrl = urlJenkinsServer + (StringUtils.isNotEmpty(view) ? "view/" + view : "")
				+ "/api/xml?depth=1";
		PostMethod post = new PostMethod(jobListUrl);
		post.setDoAuthentication( true );
		
		Document jobsDocument = null;
		try{
			client.executeMethod(post);
			jobsDocument = new SAXReader().read(post.getResponseBodyAsStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SSLException e) {
			if(e.getMessage().equals("Unrecognized SSL message, plaintext connection?")){
				urlJenkinsServer = urlJenkinsServer.replaceFirst("s", "");
				this.getJobListByServerAuth(urlJenkinsServer, view, i18nHelper);
				return jobList;
			}else{
				e.printStackTrace();
			}
		} catch (IOException e){
			e.printStackTrace();
		}finally{
			post.releaseConnection(); 
		}
		
		// if jenkinsUrl is invalid
		if(jobsDocument==null){
			return jobList;
		}
		
		// get all jobs from xml and add them to list
		for (Element job : (List<Element>) jobsDocument.getRootElement().elements("job")) {
			// create a new job and set all params from xml
			JenkinsJob hJob = new JenkinsJob();
			String jobName = job.elementText("name");
			hJob.setName(jobName);
			hJob.setUrl(job.elementText("url"));
			hJob.setBuildTrigger(hJob.getUrl() + "/build");
			hJob.setColor(job.elementText("color"));
			hJob.setLastSuccBuild(createBuildAuth(urlJenkinsServer, jobName, BuildType.LAST_SUCCESS, i18nHelper));
			hJob.setLastFailBuild(createBuildAuth(urlJenkinsServer, jobName, BuildType.LAST_FAIL, i18nHelper));

			jobList.add(hJob);
		}

		return jobList;
	}

    /**
	 * Creates JenkinsBuild if authorization is required
	 * @return JenkinsBuild
	 */
	private JenkinsBuild createBuildAuth(String urlJenkinsServer, String jobName, BuildType type, I18nHelper i18nHelper) {
		JenkinsBuild build = new JenkinsBuild();
		build.setI18nHelper(i18nHelper);
		String encodedJobName = URLEncoder.encodeForURL(jobName);
		PostMethod post = new PostMethod(urlJenkinsServer + "job/" + encodedJobName + "/" + type.toString()
				+ "/buildNumber");
		post.setDoAuthentication( true );
		
		try {
			client.executeMethod(post);
			build.setNumber(post.getResponseBodyAsString());
			build.setUrl(urlJenkinsServer + "job/" + encodedJobName + "/" + build.getNumber());
		} catch (MalformedURLException e) {
			return JenkinsBuild.UNKNOWN;
		} catch (IOException e) {
			return JenkinsBuild.UNKNOWN;
		}finally{
			post.releaseConnection();
		}

		// if we were able to obtain a build number, there should also a date
		// and time exist ...
		post = new PostMethod(urlJenkinsServer + "job/" + encodedJobName + "/" + type.toString()
				+ "/buildTimestamp?format=" + DATE_FORMAT);
		post.setDoAuthentication( true );
		
		try {
			client.executeMethod(post);
			build.setTimestamp(SIMPLE_DATE_FORMAT.parse(post.getResponseBodyAsString())); 
		} catch (MalformedURLException e) {
			return JenkinsBuild.UNKNOWN;
		} catch (IOException e) {
			return JenkinsBuild.UNKNOWN;
		} catch (ParseException e) {
			return JenkinsBuild.UNKNOWN;
		}finally{
			post.releaseConnection(); 
		}

		return build;
	}
	
	/**
	 * Creates a Jenkins Job List if no authorization is required
	 * @return Jenkins Job List
	 */
	@SuppressWarnings("unchecked")
	private List<JenkinsJob> getJobListByServer(String urlJenkinsServer, String view, I18nHelper i18nHelper)
			throws MalformedURLException, DocumentException {

		List<JenkinsJob> jobList = new ArrayList<JenkinsJob>();

		String jobListUrl = urlJenkinsServer + (StringUtils.isNotEmpty(view) ? "view/" + view : "")
				+ "/api/xml?depth=1";
		Document jobsDocument = new SAXReader().read(new URL(jobListUrl));

		// get all jobs from xml and add them to list
		for (Element job : (List<Element>) jobsDocument.getRootElement().elements("job")) {

			// create a new job and set all params from xml
			JenkinsJob hJob = new JenkinsJob();
			String jobName = job.elementText("name");
			hJob.setName(jobName);
			hJob.setUrl(job.elementText("url"));
			hJob.setBuildTrigger(hJob.getUrl() + "/build");
			hJob.setColor(job.elementText("color"));
			hJob.setLastSuccBuild(createBuild(urlJenkinsServer, jobName, BuildType.LAST_SUCCESS, i18nHelper));
			hJob.setLastFailBuild(createBuild(urlJenkinsServer, jobName, BuildType.LAST_FAIL, i18nHelper));

			jobList.add(hJob);
		}

		return jobList;
	}
	
	/**
	 * Creates JenkinsBuild if no authorization is required
	 * @return JenkinsBuild
	 */
	private JenkinsBuild createBuild(String urlJenkinsServer, String jobName, BuildType type, I18nHelper i18nHelper) {
		JenkinsBuild build = new JenkinsBuild();
		build.setI18nHelper(i18nHelper);
		String encodedJobName = URLEncoder.encodeForURL(jobName);
		try {
			URL urlBuildNumber = new URL(urlJenkinsServer + "job/" + encodedJobName + "/" + type.toString()
					+ "/buildNumber");
			build.setNumber(IOUtils.toString((InputStream) urlBuildNumber.getContent()));
			build.setUrl(urlJenkinsServer + "job/" + encodedJobName + "/" + build.getNumber());
		} catch (MalformedURLException e) {
			return JenkinsBuild.UNKNOWN;
		} catch (IOException e) {
			return JenkinsBuild.UNKNOWN;
		}

		// if we were able to obtain a build number, there should also a date
		// and time exist ...
		try {
			URL urlTimestamp = new URL(urlJenkinsServer + "job/" + encodedJobName + "/" + type.toString()
					+ "/buildTimestamp?format=" + DATE_FORMAT);
			build.setTimestamp(SIMPLE_DATE_FORMAT.parse(IOUtils.toString((InputStream) urlTimestamp.getContent())));
		} catch (MalformedURLException e) {
			return JenkinsBuild.UNKNOWN;
		} catch (IOException e) {
			return JenkinsBuild.UNKNOWN;
		} catch (ParseException e) {
			return JenkinsBuild.UNKNOWN;
		}

		return build;
	}
}