package com.zootcat.controllers.physics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Shape.Type;
import com.zootcat.controllers.Controller;
import com.zootcat.controllers.ControllerPriority;
import com.zootcat.controllers.factory.CtrlDebug;
import com.zootcat.controllers.factory.CtrlParam;
import com.zootcat.exceptions.RuntimeZootException;
import com.zootcat.physics.ZootBodyShape;
import com.zootcat.physics.ZootShapeFactory;
import com.zootcat.scene.ZootActor;
import com.zootcat.scene.ZootScene;

//TODO add tests
public class PhysicsBodyController implements Controller
{
	@CtrlParam(debug = true) protected float density = 1.0f;
	@CtrlParam(debug = true) protected float friction = 0.2f;
	@CtrlParam(debug = true) protected float restitution = 0.0f;
	@CtrlParam(debug = true) protected float linearDamping = 0.0f;
	@CtrlParam(debug = true) protected float angularDamping = 0.0f;	
	@CtrlParam(debug = true) protected float gravityScale = 1.0f;
	@CtrlParam(debug = true) protected float shapeOffsetX = 0.0f;
	@CtrlParam(debug = true) protected float shapeOffsetY = 0.0f;
	@CtrlParam(debug = true) protected float width = 0.0f;
	@CtrlParam(debug = true) protected float height = 0.0f;
	@CtrlParam(debug = true) protected boolean sensor = false;	
	@CtrlParam(debug = true) protected boolean bullet = false;
	@CtrlParam(debug = true) protected boolean canRotate = true;
	@CtrlParam(debug = true) protected boolean canSleep = true;
	@CtrlParam(debug = true) protected BodyType type = BodyType.DynamicBody;
	@CtrlParam(debug = true) protected ZootBodyShape shape = ZootBodyShape.BOX;
	@CtrlParam(global = true) protected ZootScene scene;	
	@CtrlDebug private float velocityX = 0.0f;
	@CtrlDebug private float velocityY = 0.0f;
		
	private Body body;
	private List<Fixture> fixtures;
	
	@Override
	public void init(ZootActor actor)
	{
		body = scene.getPhysics().createBody(createBodyDef(actor));
		body.setActive(false);
		
		fixtures = scene.getPhysics().createFixtures(body, createFixtureDefs(actor));
		assignUserData(actor, body, fixtures);
	}
	
	@Override
	public void onAdd(ZootActor actor) 
	{
		body.setActive(true);
	}

	@Override
	public void onRemove(ZootActor actor)
	{
		scene.getPhysics().removeBody(body);
		body = null;
	}

	@Override
	public void onUpdate(float delta, ZootActor actor) 
	{
		float bottomLeftX = body.getPosition().x - actor.getWidth() * 0.5f; 
		float bottomLeftY = body.getPosition().y - actor.getHeight() * 0.5f;
		actor.setPosition(bottomLeftX, bottomLeftY);
		actor.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
		
		Vector2 velocity = body.getLinearVelocity();
		velocityX = velocity.x;
		velocityY = velocity.y;
	}
	
	@Override
	public ControllerPriority getPriority()
	{
		return ControllerPriority.High;
	}
		
	public Body getBody()
	{
		return body;
	}
	
	public List<Fixture> getFixtures()
	{
		return Collections.unmodifiableList(fixtures);
	}
	
	public void setCollisionFilter(Filter collisionFilter) 
	{
		fixtures.forEach((fixture) -> fixture.setFilterData(collisionFilter));
	}	
		
	public void setVelocity(float vx, float vy)
	{
		setVelocity(vx, vy, true, true);
	}
	
	public Vector2 getVelocity()
	{
		return body.getLinearVelocity();
	}
	
	public void setVelocity(float vx, float vy, boolean setX, boolean setY)
	{
		Vector2 velocity = body.getLinearVelocity();
		body.setLinearVelocity(setX ? vx : velocity.x, setY ? vy : velocity.y);	
	}
	
	public Fixture addFixture(FixtureDef fixtureDef, ZootActor actor)
	{
		Fixture fixture = body.createFixture(fixtureDef);
		fixture.setUserData(actor);
		
		fixtures.add(fixture);		
		return fixture;
	}
	
	public void removeFixture(Fixture fixture)
	{
		body.destroyFixture(fixture);
		fixtures.remove(fixture);		
	}
	
	public void setGravityScale(float scale)
	{
		body.setGravityScale(scale);	
		body.setAwake(true);
	}
	
	public float getGravityScale()
	{
		return body.getGravityScale();
	}
	
	public void applyImpulse(float vx, float vy)
	{
		float cx = body.getPosition().x;
		float cy = body.getPosition().x;		
		body.applyLinearImpulse(vx, vy, cx, cy, true);
	}
	
	public void applyAngularImpulse(float i)
	{
		body.applyAngularImpulse(i, true);
	}
	
	public void setPosition(float x, float y)
	{
		body.setTransform(x, y, body.getAngle());
	}
	
	public Vector2 getCenterPositionRef()
	{
		return body.getPosition();
	}
	
	public void setCanRotate(boolean canRotate)
	{
		this.canRotate = canRotate;
		this.body.setFixedRotation(!canRotate);
	}
	
	public void scale(PhysicsBodyScale bodyScale)
	{
		fixtures.forEach(f ->
		{
			if(f.isSensor() && !bodyScale.scaleSensors) return;
			
			Shape shape = f.getShape();
			if(shape.getType() == Type.Circle)
			{
				CircleShape circle = (CircleShape)shape;
				
				Vector2 pos = circle.getPosition();			
				circle.setPosition(pos.scl(bodyScale.radiusScale, bodyScale.radiusScale));
				circle.setRadius(shape.getRadius() * bodyScale.radiusScale);
			}
			else if(shape.getType() == Type.Polygon)
			{
				PolygonShape poly = (PolygonShape)shape;
				
				Vector2[] vertices = new Vector2[poly.getVertexCount()];
				for(int i = 0; i < poly.getVertexCount(); ++i)
				{
					vertices[i] = new Vector2();					
					poly.getVertex(i, vertices[i]);
					vertices[i].x *= bodyScale.scaleX;
					vertices[i].y *= bodyScale.scaleY;
				}
				poly.set(vertices);
			}
		});
		body.setAwake(true);
	}
		
	protected BodyDef createBodyDef(ZootActor actor) 
	{
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.x = actor.getX() + actor.getWidth() * 0.5f;
		bodyDef.position.y = actor.getY() + actor.getHeight() * 0.5f;
		bodyDef.angle = actor.getRotation() * MathUtils.degreesToRadians;		
		bodyDef.active = true;
		bodyDef.allowSleep = canSleep;
		bodyDef.angularDamping = angularDamping;
		bodyDef.angularVelocity = 0.0f;
		bodyDef.awake = true;
		bodyDef.bullet = bullet;
		bodyDef.fixedRotation = !canRotate;
		bodyDef.gravityScale = gravityScale;
		bodyDef.linearDamping = linearDamping;
		bodyDef.type = type;		
		return bodyDef;
	}
	
	protected List<FixtureDef> createFixtureDefs(ZootActor actor) 
	{
		List<FixtureDef> fixtureDefs = new ArrayList<FixtureDef>(1);		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = density;
		fixtureDef.friction = friction;
		fixtureDef.restitution = restitution;
		fixtureDef.isSensor = sensor;
		fixtureDef.shape = createShape(actor, shape);
		fixtureDefs.add(fixtureDef);
		return fixtureDefs;
	}
	
	protected Shape createShape(ZootActor actor, ZootBodyShape shape)
	{		
		switch(shape)
		{
		case BOX:
			return ZootShapeFactory.createBox(
					getBodyWidth(actor), 
					getBodyHeight(actor), 
					shapeOffsetX * scene.getUnitScale(), 
					shapeOffsetY * scene.getUnitScale());
			
		case CIRCLE:
			return ZootShapeFactory.createCircle(getBodyWidth(actor));
			
		case SLOPE_LEFT:
		case SLOPE_RIGHT:
			return ZootShapeFactory.createSlope(getBodyWidth(actor), getBodyHeight(actor), shape == ZootBodyShape.SLOPE_LEFT);
			
		case POLYGON:
			PolygonMapObject polygonObj = (PolygonMapObject) scene.getMap().getObjectById(actor.getId());
			return ZootShapeFactory.createPolygon(polygonObj.getPolygon(), actor.getX(), actor.getY(), scene.getUnitScale());
			
		default:
			throw new RuntimeZootException("Unknown fixture shape type for for actor: " + actor);
		}
	}
	
	protected float getBodyWidth(ZootActor actor)
	{
		return width == 0.0f ? actor.getWidth() : width * scene.getUnitScale();
	}
	
	protected float getBodyHeight(ZootActor actor)
	{
		return height == 0.0f ? actor.getHeight() : height * scene.getUnitScale();
	}
	
	protected void assignUserData(ZootActor actor, Body body, List<Fixture> fixtures)
	{
		body.setUserData(actor);
		fixtures.forEach(fixture -> fixture.setUserData(actor));
	}
}
