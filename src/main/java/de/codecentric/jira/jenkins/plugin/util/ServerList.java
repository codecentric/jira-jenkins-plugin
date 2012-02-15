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

import java.util.ArrayList;
import java.util.List;

import de.codecentric.jira.jenkins.plugin.model.JenkinsServer;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.pluginsettings.PluginSettings;

public class ServerList {
	private final PluginSettingsFactory settingsFactory;
	private List<JenkinsServer> serverList;
	
	public ServerList(PluginSettingsFactory settingsFactory){
		this.settingsFactory = settingsFactory;
		this.serverList = new ArrayList<JenkinsServer>();
		this.setServerList();
	}
	
	public void setServerList(){
		PluginSettings settings = settingsFactory.createGlobalSettings();
		this.serverList = new ArrayList<JenkinsServer>();
    	String help = (String) settings.get("jiraJenkinsPlugin.number");
    	if(help!=null) {
    		int number = Integer.parseInt((String) settings.get("jiraJenkinsPlugin.number"));
    		for(int i=0; i<number; i++){
    			JenkinsServer server = new JenkinsServer((String) settings.get("jiraJenkinsPlugin.name" + i), (String) settings.get("jiraJenkinsPlugin.url" + i));
        		this.serverList.add(server);
        	}
    	}
	}
	
	/**
	 * Returns all jenkins-instances
	 */
	public List<JenkinsServer> getServerList(){
    	return this.serverList;
    }
	
	/**
	 * Returns the jenkins-instance with name=name or null
	 */
	public JenkinsServer find(String name){
		for(JenkinsServer server: this.serverList){
			if(server.getName().equals(name)){
				return  server;
			}
		}
		return null;
	}
	
	/**
	 * Add a new jenkins-instance. If the given name already exist nothing happens.
	 */
	public void add(String name, String url){
		PluginSettings settings = settingsFactory.createGlobalSettings();
		if(this.find(name)==null){
			settings.put("jiraJenkinsPlugin.name" + this.serverList.size(), name);
			settings.put("jiraJenkinsPlugin.url" + this.serverList.size(), url);
			settings.put("jiraJenkinsPlugin.number", Integer.toString(this.serverList.size()+1));
			this.setServerList();
		}
	}
	
	/**
	 * Delete the jenkins-instance with the given name.
	 */
	public void del(String name){
		PluginSettings settings = settingsFactory.createGlobalSettings();
		JenkinsServer server = this.find(name);
		if(server!=null){
			this.serverList.remove(server);
			for(int i=0; i<this.serverList.size(); i++){
				settings.put("jiraJenkinsPlugin.name" + i, this.serverList.get(i).getName());
				settings.put("jiraJenkinsPlugin.url" + i, this.serverList.get(i).getUrl());
			}
			settings.put("jiraJenkinsPlugin.name" + this.serverList.size(), "");
			settings.put("jiraJenkinsPlugin.url" + this.serverList.size(), "");
			settings.put("jiraJenkinsPlugin.number", Integer.toString(this.serverList.size()));
		}
	}
}
