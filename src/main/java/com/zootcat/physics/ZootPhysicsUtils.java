package com.zootcat.physics;

import java.util.Arrays;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Shape.Type;
import com.zootcat.exceptions.RuntimeZootException;

public class ZootPhysicsUtils
{
	public static Vector2[] getPolygonVertices(PolygonShape polygon)
	{
		Vector2 vertices[] = new Vector2[polygon.getVertexCount()];			
		for(int i = 0; i < polygon.getVertexCount(); ++i)
		{
			vertices[i] = new Vector2();
			polygon.getVertex(i, vertices[i]);
		}
		return vertices;		
	}
	
	public static Vector2 getFixtureCenter(Fixture fixture)
	{
		Shape shape = fixture.getShape();
		if(shape == null)
		{
			return null;
		}
		
		switch(shape.getType())
		{
		case Polygon:
			return getPolygonCentroid((PolygonShape) shape);
		
		case Circle:
			return ((CircleShape)shape).getPosition();
		
		default:
			throw new RuntimeZootException("Unable to get fixture center with shape type " + shape.getType());		
		}		
	}
	
	public static Vector2 getPolygonCentroid(PolygonShape polygon)
	{
		Vector2 centroid = new Vector2();
		Vector2[] vertices = getPolygonVertices(polygon);
		
		Arrays.stream(vertices).forEach(vert -> 
		{
			centroid.x += vert.x;
			centroid.y += vert.y;			
		});		
		
		centroid.x /= vertices.length;
		centroid.y /= vertices.length;		
		return centroid;
	}
	
	public static void moveFixture(Fixture fixture, float mx, float my)
	{
		Shape shape = fixture.getShape();
		if(shape == null)
		{
			return;
		}
		
		switch(shape.getType())
		{
		case Polygon:
			PolygonShape polygon = (PolygonShape)shape;
			Vector2 vertices[] = getPolygonVertices(polygon);
			Arrays.stream(vertices).forEach(vert -> vert.add(mx, my));
			polygon.set(vertices);
			fixture.getBody().resetMassData();
			return;
		
		case Circle:
			CircleShape circle = (CircleShape) shape;
			Vector2 position = circle.getPosition();
			position.add(mx, my);
			circle.setPosition(position);
			fixture.getBody().resetMassData();
			break;
			
		default:
			throw new RuntimeZootException("Unable to move fixture with shape type " + shape.getType());
		}
	}

	public static void setFixturePosition(Fixture fixture, float x, float y)
	{
		Shape shape = fixture.getShape();
		switch(shape.getType())
		{
		case Polygon:
			PolygonShape polygon = (PolygonShape)shape;
			Vector2 centroid = getPolygonCentroid(polygon);
			moveFixture(fixture, x - centroid.x, y - centroid.y);
			return;
						
		case Circle:
			CircleShape circle = (CircleShape) shape;
			circle.setPosition(new Vector2(x,y));
			fixture.getBody().resetMassData();
			return;
		
		default:
			throw new RuntimeZootException("Unable to move fixture with shape type " + shape.getType());		
		}
		
	}
	
	public static void scaleFixture(Fixture fixture, float radiusScale, float scaleX, float scaleY)
	{	
		Shape shape = fixture.getShape();
		if(shape.getType() == Type.Circle)
		{
			CircleShape circle = (CircleShape)shape;			
			Vector2 pos = circle.getPosition();			
			circle.setPosition(pos.scl(radiusScale, radiusScale));
			circle.setRadius(shape.getRadius() * radiusScale);
			return;
		}
		else if(shape.getType() == Type.Polygon)
		{
			PolygonShape poly = (PolygonShape)shape;
			
			Vector2[] vertices = new Vector2[poly.getVertexCount()];
			for(int i = 0; i < poly.getVertexCount(); ++i)
			{
				vertices[i] = new Vector2();					
				poly.getVertex(i, vertices[i]);
				vertices[i].x *= scaleX;
				vertices[i].y *= scaleY;
			}
			poly.set(vertices);
		}
	}	
}
