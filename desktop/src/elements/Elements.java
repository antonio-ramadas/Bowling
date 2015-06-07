package elements;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public abstract class Elements implements ApplicationListener {
	public ModelBatch modelBatch;
	public Model model;
	public ModelInstance instance;
	
	@Override
	public void dispose() {
		modelBatch.dispose();
		model.dispose();
	}
	
	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
