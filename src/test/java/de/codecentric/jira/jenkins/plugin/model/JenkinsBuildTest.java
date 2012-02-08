/*package de.codecentric.jira.jenkins.plugin.model;

import java.util.Date;

import org.junit.Test;

import junit.framework.TestCase;

public class JenkinsBuildTest extends TestCase {
	private JenkinsBuild jenkinsBuild;
	private Date date;
	
	@Override
	public void setUp(){
		date = new Date();
		jenkinsBuild = new JenkinsBuild("Name", "Number", "Color", "Url", date);
	}
	
	@Test
	public void testConstructorName(){
		assertEquals("Name", jenkinsBuild.getName());
	}
	
	@Test
	public void testConstructorNumber(){
		assertEquals("Number", jenkinsBuild.getNumber());
	}
	
	@Test
	public void testConstructorColor(){
		assertEquals("Color", jenkinsBuild.getColor());
	}
	
	@Test
	public void testConstructorUrl(){
		assertEquals("Url", jenkinsBuild.getUrl());
	}
	
	@Test
	public void testConstructorTimestamp(){
		assertEquals(date, jenkinsBuild.getTimestamp());
	}
	
	@Test
	public void testSetResultStabil(){
		jenkinsBuild.setResult(BuildResult.valueOf("Stabil"));
		assertEquals("blue", jenkinsBuild.getColor());
	}
	
	@Test
	public void testSetResultSuccess(){
		jenkinsBuild.setResult(BuildResult.valueOf("SUCCESS"));
		assertEquals("blue", jenkinsBuild.getColor());
	}
	
	@Test
	public void testSetResultFailure(){
		jenkinsBuild.setResult(BuildResult.valueOf("FAILURE"));
		assertEquals("red", jenkinsBuild.getColor());
	}
	
	@Test
	public void testSetResultAborted(){
		jenkinsBuild.setResult(BuildResult.valueOf("ABORTED"));
		assertEquals("grey", jenkinsBuild.getColor());
	}
	
	@Test
	public void testSetResultNotBuild(){
		jenkinsBuild.setResult(BuildResult.valueOf("NOT_BUILT"));
		assertEquals("grey", jenkinsBuild.getColor());
	}
	
	@Test
	public void testSetResultUnstable(){
		jenkinsBuild.setResult(BuildResult.valueOf("UNSTABLE"));
		assertEquals("yellow", jenkinsBuild.getColor());
	}
}
*/