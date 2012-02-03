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
package de.codecentric.jira.jenkins.plugin.ao;

import java.util.List;

import com.atlassian.activeobjects.external.ActiveObjects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Implements methods to work with a Server entity.
 */
public class ServerServiceImpl implements ServerService {
	
	private final ActiveObjects ao;
	 
    public ServerServiceImpl(ActiveObjects ao)
    {
        this.ao = checkNotNull(ao);
    }

    /**
     * Add a new Server entity to database.
     * A new Server entity is only created if no Server entity with the provided name exists within the database.
     */
	@Override
	public Server add(String name, String url) {
		if(this.find(name)==null){
			final Server server = ao.create(Server.class); 
	        server.setName(name);
	        server.setUrl(url);
	        server.save(); 
	        return server;
		}else{
			return null;
		}
	}

	/**
	 * Returns a list of all Server entities.
	 */
	@Override
	public List<Server> all() {
		return newArrayList(ao.find(Server.class));
	}

	/**
	 * Delete the Server entity with the given name.
	 * If no Server entity with the given name exists the method does nothing.
	 */
	@Override
	public void del(String name) {
		Server server = this.find(name);
		if(server != null){
			ao.delete(server);
		}
	}

	/**
	 * Returns the first Server entity with the given name or null if no such Server entity exists.
	 */
	@Override
	public Server find(String name) {
		Server[] serverArray = ao.find(Server.class, "name = ?", name);
		Server server = null;
		if(serverArray.length >= 1){
			server = serverArray[0];
		}
		return server;
	}

}
