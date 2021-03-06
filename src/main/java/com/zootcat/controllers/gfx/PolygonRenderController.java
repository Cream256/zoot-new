package com.zootcat.controllers.gfx;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.zootcat.controllers.factory.CtrlParam;
import com.zootcat.controllers.physics.PhysicsBodyController;
import com.zootcat.scene.ZootActor;
import com.zootcat.scene.ZootScene;

//TODO BETA use box2d utility class for rendering polygon (https://bitbucket.org/dermetfan/libgdx-utils/wiki/Home)
public class PolygonRenderController extends RenderControllerAdapter 
{
	@CtrlParam(required = true) private String textureFile;
	@CtrlParam(global = true) private AssetManager assetManager;
	@CtrlParam(global = true) private ZootScene scene;
	
	private PolygonSprite polygonSprite;
	private PolygonSpriteBatch polyBatch;
	
	@Override
	public void init(ZootActor actor) 
	{		
		polygonSprite = null;
		polyBatch = new PolygonSpriteBatch();		
	}
	
	@Override
	public void onAdd(ZootActor actor) 
	{
		PhysicsBodyController ctrl = actor.getSingleController(PhysicsBodyController.class);		
		PolygonShape polygon = (PolygonShape) ctrl.getFixtures().get(0).getShape();
		
		Vector2 vertex = new Vector2();
		float[] vertices = new float[polygon.getVertexCount() * 2];		
		for(int i = 0; i < polygon.getVertexCount(); ++i)	
		{
			polygon.getVertex(i, vertex);			
			vertices[i * 2] = vertex.x + actor.getWidth() / 2.0f;
			vertices[i * 2 + 1] = vertex.y + actor.getHeight() / 2.0f;			
		}		
		short[] triangles = new EarClippingTriangulator().computeTriangles(vertices).toArray();
	
		TextureRegion textureRegion = new TextureRegion(assetManager.get(textureFile, Texture.class));
		PolygonRegion polygonRegion = new PolygonRegion(textureRegion, vertices, triangles);		
		polygonSprite = new PolygonSprite(polygonRegion);
	}
	
	@Override
	public void onRender(Batch batch, float parentAlpha, ZootActor actor, float delta)
	{
		batch.end();
		
		polyBatch.setProjectionMatrix(batch.getProjectionMatrix());
		polyBatch.begin();
		polygonSprite.setPosition(actor.getX() + getOffsetX(), actor.getY() + getOffsetY());
		polygonSprite.setRotation(actor.getRotation());
		polygonSprite.draw(polyBatch);
		polyBatch.end();
		
		batch.begin();
	}
}
