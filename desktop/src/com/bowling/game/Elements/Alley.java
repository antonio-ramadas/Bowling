package com.bowling.game.Elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;

public class Alley extends Elements {

	@Override
	public void create()
	{		
		modelBatch = new ModelBatch();
		
		ModelLoader<?> loader = new ObjLoader();
        model = loader.loadModel(Gdx.files.internal("Alley.obj"));
        instance = new ModelInstance(model);
	}

}
