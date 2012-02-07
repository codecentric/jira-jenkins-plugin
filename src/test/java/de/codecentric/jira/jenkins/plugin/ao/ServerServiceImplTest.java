/*package de.codecentric.jira.jenkins.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import net.java.ao.EntityManager;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
 
import static org.junit.Assert.*;

@RunWith(ActiveObjectsJUnitRunner.class) 
public class ServerServiceImplTest {
	
	private EntityManager entityManager;
	private ActiveObjects ao;
    private ServerServiceImpl serverService;
    
    @Before
    public void setUp() throws Exception{
        assertNotNull(entityManager);
        ao = new TestActiveObjects(entityManager);
        serverService = new ServerServiceImpl(ao);
    }
 
    @Test
    public void testAdd() throws Exception{
    	final String name = "Name";
    	final String url = "url";
    	
    	ao.migrate(Server.class);
    	
    	assertEquals(0, ao.find(Server.class).length);
    	
    	final Server add = serverService.add(name, url);
    	assertFalse(add.getID() == 0);
    	
    	ao.flushAll();
    	
    	final Server[] server = ao.find(Server.class);
        assertEquals(1, server.length);
        assertEquals(name, server[0].getName());
        assertEquals(url, server[0].getUrl());
    }
 
    @Test
    public void testAddMuliple() throws Exception{
    	final String name = "Name";
    	final String url = "url";
    	
    	ao.migrate(Server.class);
    	
    	assertEquals(0, ao.find(Server.class).length);
    	
    	final Server add = serverService.add(name, url);
    	assertFalse(add.getID() == 0);
    	
    	final Server add2 = serverService.add(name, url);
    	assertTrue(add2 == null);

    	ao.flushAll();
    	
    	final Server[] server = ao.find(Server.class);
        assertEquals(1, server.length);
    }
    
    @Test
    public void testAll() throws Exception{
    	ao.migrate(Server.class); 
    	 
        assertTrue(serverService.all().isEmpty());
 
        final Server server = ao.create(Server.class);
        server.setName("Name");
        server.setUrl("Url");
        server.save();
 
        ao.flushAll(); 
 
        final List<Server> all = serverService.all();
        assertEquals(1, all.size());
        assertEquals(server.getID(), all.get(0).getID());
    }
    
    @Test
    public void testDel() throws Exception{
    	ao.migrate(Server.class); 
   	 
        assertTrue(serverService.all().isEmpty());
 
        final Server server = ao.create(Server.class);
        server.setName("Name");
        server.setUrl("Url");
        server.save();
 
        assertFalse(serverService.all().isEmpty());
        
        serverService.del("name");
        
        assertFalse(serverService.all().isEmpty());
        
        serverService.del("Name");
        
        assertTrue(serverService.all().isEmpty());
    }
    
    @Test
    public void testFind() throws Exception{
    	ao.migrate(Server.class); 
   	 
        assertTrue(serverService.all().isEmpty());
        assertTrue(serverService.find("Name")==null);
 
        final Server server = ao.create(Server.class);
        server.setName("Name");
        server.setUrl("Url");
        server.save();
 
        assertEquals(serverService.find("Name").getName(), "Name");
    }
}
*/