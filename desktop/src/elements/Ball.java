package elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;

public class Ball extends Elements {

	@Override
	public void create() {
		modelBatch = new ModelBatch();
		
		ModelLoader<?> loader = new ObjLoader();
        model = loader.loadModel(Gdx.files.internal("Ball.obj"));
        instance = new ModelInstance(model);
		instance.transform.setTranslation(1000, 10, 0);
        
	}

}
