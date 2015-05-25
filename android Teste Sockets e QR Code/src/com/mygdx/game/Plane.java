package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;



public class Plane {

	Sprite plane;
	float w = Gdx.graphics.getWidth();
	float h = Gdx.graphics.getHeight();

	public Plane(Texture texture){
		plane = new Sprite(texture);		
		plane.setPosition(w/2, h/2);
	}

	public void draw(SpriteBatch batch){
		plane.draw(batch);
	}


	public void moveUp(float pixels)
	{	
		plane.translateY(pixels);

		if (plane.getY() > (h - plane.getHeight()) )
			plane.setPosition(plane.getX(), h - plane.getHeight());

	}
	public void moveDown(float pixels)
	{	
		plane.translateY(-pixels);

		if (plane.getY() < 0)
			plane.setPosition(plane.getX(), 0);
	}
	public void moveLeft(float pixels)
	{	
		plane.translateX(-pixels);

		if (plane.getX() < 0)
			plane.setPosition(0, plane.getY());
	}
	public void moveRight(float pixels)
	{	
		plane.translateX(pixels);

		if (plane.getX() > (w - plane.getWidth())   )
			plane.setPosition(w - plane.getWidth(), plane.getY());
	}

	public void goToPosition(float x, float y)
	{
		plane.setX(x);
		plane.setY(y);
		
		if (plane.getX() > (w - plane.getWidth())   )
			plane.setPosition(w - plane.getWidth(), plane.getY());
		if (plane.getX() < 0)
			plane.setPosition(0, plane.getY());
		if (plane.getY() > (h - plane.getHeight()) )
			plane.setPosition(plane.getX(), h - plane.getHeight());
		if (plane.getY() < 0)
			plane.setPosition(plane.getX(), 0);
	}

	public float getWidth(){
		return plane.getWidth();
	}
	public float getHeight(){
		return plane.getHeight();
	}
	
	public boolean changeTexture(Texture texture)
	{
		float tempX = plane.getX(), tempY = plane.getY();
		plane = new Sprite(texture);
		goToPosition(tempX,tempY);
		return true;
	}
}
