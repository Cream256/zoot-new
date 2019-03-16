package com.zootcat.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.zootcat.scene.ZootActor;
import com.zootcat.scene.ZootScene;
import com.zootcat.testing.HeadlessGdxTestRunner;

@RunWith(HeadlessGdxTestRunner.class)
public class ZootCameraTest
{
	private static final float WORLD_WIDTH = 32.0f;
	private static final float WORLD_HEIGHT = 16.0f;
	
	private ZootCamera camera;
	
	@Before
	public void setup()
	{
		camera = new ZootCamera(WORLD_WIDTH, WORLD_HEIGHT);
	}
	
	@Test
	public void shouldNotThrowIfNullStrategyWasSet()
	{
		camera.setScrollingStrategy(null);
		camera.update(1.0f, true);
		//ok
	}
	
	@Test
	public void shouldSetScrollingStrategy()
	{
		//given
		ZootCameraScrollingStrategy strategy = mock(ZootCameraScrollingStrategy.class);
		
		//when
		camera.setScrollingStrategy(strategy);
		
		//then
		assertEquals(strategy, camera.getScrollingStrategy());		
	}
	
	@Test
	public void shouldUseScrollingStrategyOnUpdate()
	{
		//given
		ZootCameraScrollingStrategy strategy = mock(ZootCameraScrollingStrategy.class);
		
		//when
		camera.setScrollingStrategy(strategy);
		camera.update(1.0f, true);
		
		//then
		verify(strategy, times(1)).scrollCamera(camera, 1.0f);
	}
	
	@Test
	public void shouldSetZoom()
	{
		camera.setZoom(0.0f);
		assertEquals(0.0f, camera.getZoom(), 0.0f);
		
		camera.setZoom(1.0f);
		assertEquals(1.0f, camera.getZoom(), 0.0f);
		
		camera.setZoom(2.0f);
		assertEquals(2.0f, camera.getZoom(), 0.0f);
	}
	
	@Test
	public void shouldHaveDefaultZoom()
	{
		assertEquals(1.0f, camera.getZoom(), 0.0f);
	}
	
	@Test
	public void shouldSetTarget()
	{
		camera.setTarget(null);
		assertNull(camera.getTarget());
		
		ZootActor actor = mock(ZootActor.class); 
		camera.setTarget(actor);
		assertEquals(actor, camera.getTarget());
	}
	
	@Test
	public void shouldSetEdgeSnapping()
	{
		camera.setEdgeSnapping(false);
		assertFalse(camera.isEdgeSnapping());
		
		camera.setEdgeSnapping(true);
		assertTrue(camera.isEdgeSnapping());
	}
	
	@Test
	public void shouldSetPosition()
	{
		camera.setPosition(0.0f, 0.0f);
		assertEquals(0.0f, camera.getPosition().x, 0.0f);
		assertEquals(0.0f, camera.getPosition().y, 0.0f);
		assertEquals(0.0f, camera.getPosition().z, 0.0f);
		
		camera.setPosition(1.0f, 2.0f);
		assertEquals(1.0f, camera.getPosition().x, 0.0f);
		assertEquals(2.0f, camera.getPosition().y, 0.0f);
		assertEquals(0.0f, camera.getPosition().z, 0.0f);
		
		camera.setPosition(-1.0f, -2.0f);
		assertEquals(-1.0f, camera.getPosition().x, 0.0f);
		assertEquals(-2.0f, camera.getPosition().y, 0.0f);
		assertEquals(0.0f, camera.getPosition().z, 0.0f);
	}
	
	@Test
	public void shouldSnapPositionToWorldEdges()
	{
		//given
		camera.setViewportSize(0.0f, 0.0f);
		camera.setEdgeSnapping(true);
		
		//then
		camera.setPosition(-WORLD_WIDTH * 2, -WORLD_WIDTH * 2);
		assertEquals(0.0f, camera.getPosition().x, 0.0f);
		assertEquals(0.0f, camera.getPosition().y, 0.0f);
		
		camera.setPosition(WORLD_WIDTH * 2, WORLD_HEIGHT * 2);
		assertEquals(WORLD_WIDTH, camera.getPosition().x, 0.0f);
		assertEquals(WORLD_HEIGHT, camera.getPosition().y, 0.0f);
	}
	
	@Test
	public void shouldSetViewportSize()
	{
		camera.setViewportSize(1.0f, 2.0f);
		assertEquals(1.0f, camera.getViewportWidth(), 0.0f);
		assertEquals(2.0f, camera.getViewportHeight(), 0.0f);
	}
	
	@Test
	public void shouldSetScene()
	{
		//given
		ZootScene scene = mock(ZootScene.class);
		
		//when
		camera.setScene(scene);
		
		//then
		assertEquals(scene, camera.getScene());
	}
	
	@Test
	public void shouldReturnWorldSize()
	{
		assertEquals(WORLD_WIDTH, camera.getWorldWidth(), 0.0f);
		assertEquals(WORLD_HEIGHT, camera.getWorldHeight(), 0.0f);
	}
}
