package com.zootcat.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.zootcat.assets.ZootAssetManager;
import com.zootcat.exceptions.RuntimeZootException;
import com.zootcat.fsm.events.ZootActorEventCounterListener;
import com.zootcat.fsm.events.ZootEventType;
import com.zootcat.game.ZootGame;
import com.zootcat.gfx.ZootGraphicsFactory;
import com.zootcat.scene.ZootActor;
import com.zootcat.screen.ZootDialogScreen;
import com.zootcat.testing.ZootTestUtils;

public class ZootShowDialogScreenActionTest
{
	private static final String DIALOG_TOKEN = "Test1";
	
	@Mock private ZootGame game;
	@Mock private ZootAssetManager assetManager;
	@Mock private ZootGraphicsFactory graphicsFactory;
	@Rule public ExpectedException expectedEx = ExpectedException.none();
	
	private String dialogPath;
	private ZootActor triggeringActor;
	private ZootActorEventCounterListener eventCounter;
	private ZootShowDialogScreenAction action;
	
	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);
		when(game.getAssetManager()).thenReturn(assetManager);
		when(game.getGraphicsFactory()).thenReturn(graphicsFactory);
						
		eventCounter = new ZootActorEventCounterListener();		
		triggeringActor = new ZootActor();
		triggeringActor.addListener(eventCounter);
		
		dialogPath = ZootTestUtils.getResourcePath("dialogs/TestDialog.dialog", this);
		
		action = new ZootShowDialogScreenAction();
		action.setZootGame(game);
		action.setDialogPath(dialogPath);
		action.setDialogToken(DIALOG_TOKEN);
		action.setTarget(triggeringActor);
	}
	
	@Test
	public void shouldSetZootGame()
	{
		assertEquals(game, action.getZootGame());
	}
	
	@Test
	public void shouldSetDialogPath()
	{
		assertEquals(dialogPath, action.getDialogPath());
	}
	
	@Test
	public void shouldSetDialogToken()
	{
		assertEquals(DIALOG_TOKEN, action.getDialogToken());
	}
	
	@Test
	public void shouldStopTriggeringActor()
	{
		action.act(0.0f);
				
		assertEquals(1, eventCounter.getCount());
		assertEquals(ZootEventType.Stop, eventCounter.getLastZootEvent().getType());
	}
		
	@Test
	public void shouldSetDialogScreenForGame()
	{
		action.act(0.0f);
		
		ArgumentCaptor<ZootDialogScreen> captor = ArgumentCaptor.forClass(ZootDialogScreen.class);
		
		verify(game).setScreen(captor.capture());
		assertNotNull(captor.getValue());
		assertNotNull(captor.getValue().getDialog());
		assertEquals(triggeringActor, captor.getValue().getTriggeringActor());
	}
	
	@Test
	public void shouldThrowIfNoPathIsGiven()
	{
		expectedEx.expect(RuntimeZootException.class);
		expectedEx.expectMessage("No dialog path was given for ZootShowDialogScreenAction");		
	
		action.setDialogPath(null);
		action.act(0.0f);
	}
	
	@Test
	public void shouldThrowIfPathIsEmpty()
	{
		expectedEx.expect(RuntimeZootException.class);
		expectedEx.expectMessage("No dialog path was given for ZootShowDialogScreenAction");		
	
		action.setDialogPath("");
		action.act(0.0f);
	}
	
	@Test
	public void shouldThrowIfNoTokenIsGiven()
	{
		expectedEx.expect(RuntimeZootException.class);
		expectedEx.expectMessage("No dialog token was given for ZootShowDialogScreenAction");		
		
		action.setDialogToken(null);
		action.act(0.0f);
	}
	
	@Test
	public void shouldThrowIfTokenIsEmpty()
	{
		expectedEx.expect(RuntimeZootException.class);
		expectedEx.expectMessage("No dialog token was given for ZootShowDialogScreenAction");		
		
		action.setDialogToken("");
		action.act(0.0f);
	}
	
	@Test
	public void shouldThrowIfDialogFileDoesNotExist()
	{
		expectedEx.expect(RuntimeZootException.class);
		expectedEx.expectMessage("File FakePath does not exist!");
		
		action.setDialogPath("FakePath");
		action.setDialogToken("FakeToken");
		action.act(0.0f);
	}
	
	@Test
	public void shouldThrowIfNoGameIsGiven()
	{
		expectedEx.expect(RuntimeZootException.class);
		expectedEx.expectMessage("No zoot game was given for ZootShowDialogScreenAction");		
		
		action.setZootGame(null);
		action.act(0.0f);
	}
	
	@Test
	public void shouldReset()
	{
		action.reset();
		
		assertNull(action.getTargetZootActor());
		assertNull(action.getDialogToken());
		assertNull(action.getDialogPath());
		assertNull(action.getZootGame());
	}
}
