package com.zootcat.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.zootcat.controllers.ChangeListenerController;
import com.zootcat.controllers.Controller;
import com.zootcat.controllers.ZootControllerOrderComparator;
import com.zootcat.controllers.gfx.RenderController;
import com.zootcat.controllers.recognizers.ZootClassControllerRecognizer;
import com.zootcat.controllers.recognizers.ZootControllerRecognizer;
import com.zootcat.exceptions.ZootControllerNotFoundException;
import com.zootcat.exceptions.ZootDuplicatedControllerException;
import com.zootcat.fsm.ZootStateMachine;

/**
 * ZootScene actor. Extends from LibGdx {@link Actor}. It exends 
 * the base actor class so that it could use {@link Controller}'s. 
 * @author Cream
 * @see ZootScene
 *
 */
public class ZootActor extends Actor
{
	public static final String DEFAULT_NAME = "Unnamed Actor";
	
	private int id = 0;
	private int gid = -1;
	private ZootScene scene;
	private float opacity = 1.0f;		
	private Set<String> types = new HashSet<String>();
	private List<Controller> controllers = new ArrayList<Controller>();
	private ZootStateMachine stateMachine = new ZootStateMachine();
	private ZootControllerRecognizer controllerRecognizer = ZootClassControllerRecognizer.Instance;
	
	public ZootActor()
	{
		setName(DEFAULT_NAME);
		stateMachine.setOwner(this);
		addListener(stateMachine);
	}
	
	@Override
	public void act(float delta)
	{				
		controllers.stream().filter(ctrl -> ctrl.isEnabled()).forEach(ctrl -> ctrl.onUpdate(delta, this));
		stateMachine.update(delta);
		super.act(delta);
	}
	
	@Override
	public boolean remove() 
	{
		removeAllControllers();
		return super.remove();
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) 
	{
		float delta = Gdx.graphics.getDeltaTime();
		controllers.stream().filter(ctrl -> ClassReflection.isInstance(RenderController.class, ctrl))
							.filter(ctrl -> ctrl.isEnabled())
							.map(ctrl -> (RenderController)ctrl)
							.forEach(ctrl -> ctrl.onRender(batch, parentAlpha, this, delta));
	}
	
	@Override
	protected void positionChanged() 
	{
		controllers.stream().filter(ctrl -> ClassReflection.isInstance(ChangeListenerController.class, ctrl))
							.filter(ctrl -> ctrl.isEnabled())
							.map(ctrl -> (ChangeListenerController)ctrl)
							.forEach(ctrl -> ctrl.onPositionChange(this));
	}

	@Override
	protected void sizeChanged() 
	{
		controllers.stream().filter(ctrl -> ClassReflection.isInstance(ChangeListenerController.class, ctrl))
							.filter(ctrl -> ctrl.isEnabled())
							.map(ctrl -> (ChangeListenerController)ctrl)
							.forEach(ctrl -> ctrl.onSizeChange(this));
	}
	
	@Override
	protected void rotationChanged() 
	{
		controllers.stream().filter(ctrl -> ClassReflection.isInstance(ChangeListenerController.class, ctrl))
							.filter(ctrl -> ctrl.isEnabled())
							.map(ctrl -> (ChangeListenerController)ctrl)
							.forEach(ctrl -> ctrl.onRotationChange(this));
	}
		
	/**
	 * Check if all controllers of a given class match the provided predicate.
	 * @param clazz - controller class to look for
	 * @param func - predicate
	 * @return true if all controllers match the predicate, false otherwise. False, if no controllers were found.
	 */
	public <T extends Controller> boolean controllersAllMatch(Class<T> clazz, Function<T, Boolean> func)
	{				
		List<T> controllers = getControllers(clazz);
		if(controllers.isEmpty()) return false;		
		return controllers.stream().allMatch(ctrl -> func.apply(ctrl));
	}
	
	/**
	 * Check if any controller of a given class match the provided predicate.
	 * @param clazz - controller class to look for
	 * @param func - predicate
	 * @return true if any controller matches the predicate, false otherwise. False, if no controllers were found.
	 */
	public <T extends Controller> boolean controllersAnyMatch(Class<T> clazz, Function<T, Boolean> func)
	{		
		List<T> controllers = getControllers(clazz);
		if(controllers.isEmpty()) return false;		
		return controllers.stream().anyMatch(ctrl -> func.apply(ctrl));
	}
	
	/**
	 * Find every controller of a given class and perform some action on it.
	 * @param clazz - controller class to be found
	 * @param action - action that will be preformed on found controllers
	 */
	public <T extends Controller> void controllersAction(Class<T> clazz, Consumer<T> action)
	{
		getControllers(clazz).forEach(action);
	}
	
	/**
	 * Find every controller basing on a supported predicate and perform some action on it.
	 * @param predicate - predicate for controllers
	 * @param action - action that will be preformed on controllers matching the predicate
	 */
	public void controllersAction(Predicate<Controller> predicate, Consumer<Controller> action)
	{
		controllers.stream().filter(predicate).forEach(action);
	}
	
	public void addControllers(Collection<Controller> newControllers)
	{		
		//check for duplicated singletons already added to actor
		newControllers.forEach(newCtrl -> 
		{
			verifyNoDuplicatedSingletonControllers(newCtrl, getControllers(newCtrl.getClass()));
		});
		
		//controllers must be added to actor
		newControllers.forEach((ctrl) -> controllers.add(ctrl));
		
		//must be invoked in proper order
		newControllers.stream().sorted(ZootControllerOrderComparator.Instance)
							   .forEach((ctrl) -> ctrl.onAdd(this));
		
		//reorder controllers
		controllers.sort(ZootControllerOrderComparator.Instance);
	}
		
	/**
	 * Adds new controller to the actor. 
	 * @param new controller to be added
	 * @throws ZootDuplicatedControllerException if trying to add a duplicated singleton controller.
	 */
	public void addController(Controller newController)
	{		
		verifyNoDuplicatedSingletonControllers(newController, getControllers(newController.getClass()));
		
		controllers.add(newController);
		controllers.sort(ZootControllerOrderComparator.Instance);
		newController.onAdd(this);		
	}
	
	private <T extends Controller> void verifyNoDuplicatedSingletonControllers(Controller newController, Collection<T> controllers)
	{
		if(newController.isSingleton() && !controllers.isEmpty())
		{
			throw new ZootDuplicatedControllerException(newController.getClass().getSimpleName(), getName());
		}
	}
	
	public void removeController(Controller controller)
	{
		controller.onRemove(this);
		controllers.remove(controller);
	}
	
	public void removeAllControllers()
	{
		controllers.stream().sorted(ZootControllerOrderComparator.Instance.reversed())
							.forEach(ctrl -> ctrl.onRemove(this));
		controllers.clear();
	}
	
	public List<Controller> getAllControllers()
	{
		return new ArrayList<Controller>(controllers);
	}
		
	/**
	 * Get all controllers of a given class.
	 * @param controllerClass - controllers of given class (only) will be returned
	 * @return List of controllers. Empty list if none are found.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Controller> List<T> getControllers(Class<T> controllerClass)
	{
		List<Controller> result = controllers.stream()
						 .filter(ctrl -> controllerRecognizer.isControllerExact(ctrl, controllerClass))
						 .collect(Collectors.toList());
		return (List<T>)result;
	}
		
	/**
	 * Returns a single controller instance.
	 * @param controllerClass - controller class to look for
	 * @return Controller instance, or throws ZootControllerNotFoundException if no controller was found
	 * @throws ZootDuplicatedControllerException if more then one instance of given controller class were found.
	 */
	public <T extends Controller> T getSingleController(Class<T> controllerClass)
	{
		List<T> controllers = getControllers(controllerClass);
		if(controllers.isEmpty()) throw new ZootControllerNotFoundException(controllerClass.getSimpleName(), getName());
		if(controllers.size() > 1) throw new ZootDuplicatedControllerException(controllerClass.getSimpleName(), getName());
		return controllers.get(0);
	}
	
	/**
	 * Returns a single controller instance. 
	 * @param controllerClass - controller class to look for
	 * @return Controller instance, or null if controller is not found.
	 * @throws ZootDuplicatedControllerException if more then one instance of given controller class were detected.
	 */
	public <T extends Controller> T tryGetSingleController(Class<T> controllerClass)
	{
		try
		{
			return getSingleController(controllerClass);
		}
		catch(ZootControllerNotFoundException e)
		{
			return null;
		}
	}
	
	public float getOpacity() 
	{
		return opacity;
	}
	
	public void setOpacity(float value)
	{
		this.opacity = MathUtils.clamp(value, 0.0f, 1.0f);
	}
	    
    public void addType(String newType)
    {
        types.add(newType.toLowerCase().trim());
    }
    
    public void removeType(String typeToRemove)
    {
        types.remove(typeToRemove.toLowerCase());
    }
    
    public Set<String> getTypes()
    {
        return new HashSet<String>(types);
    }
    
    public boolean isType(String type)
    {
        return types.contains(type.toLowerCase());
    }	
    
    public int getId()
    {
    	return id;
    }
    
    public void setId(int id)
    {
    	this.id = id;
    }
    
	public void setGid(int gid)
	{
		this.gid = gid;
	}
	
	public int getGid()
	{
		return gid;
	}
    
	public ZootStateMachine getStateMachine()
	{
		return stateMachine;
	}
		
    @Override
    public String toString()
    {
    	return getName();
    }
    
	public void setScene(ZootScene scene)
	{
		this.scene = scene;
	}
	
	public ZootScene getScene()
	{
		return scene;
	}

	public void setControllerRecognizer(ZootControllerRecognizer controllerRecognizer)
	{
		this.controllerRecognizer = controllerRecognizer;
	}

	public ZootControllerRecognizer getControllerRecognizer()
	{
		return controllerRecognizer;
	}
}
