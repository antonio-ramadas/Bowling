package graphics;

import java.io.IOException;
import java.util.Random;

import logic.GameMachine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;

import elements.Alley;
import elements.Ball;
import elements.Pin;

public class GameWindow  extends ApplicationAdapter {
	final static short GROUND_FLAG = 1<<8;
	final static short OBJECT_FLAG = 1<<9;
	final static short ALL_FLAG = -1;
	btDynamicsWorld dynamicsWorld;
	GameObject ballGo;
	Alley bowlingAlley;
	CameraInputController camController;
	Array<GameObject> instances;
	PerspectiveCamera cam;
	ModelBatch modelBatch;
	Environment environment;
	GameObject pinGo[] = new GameObject[10];
	Boolean pinUp[] = new Boolean[10];
	ArrayMap<String, GameObject.Constructor> constructors;
	Model model;
	btConstraintSolver constraintSolver;
	btBroadphaseInterface broadphase;
	MyContactListener contactListener;
	btDispatcher dispatcher;
	btCollisionConfiguration collisionConfig;
	GameMachine gameMachine;
	Sound soundMusic;

	class CollisionThread extends Thread {
		public void run() {
			Random r = new Random();
			Sound sound = Gdx.audio.newSound(Gdx.files.internal("bin/pin" + (r.nextInt(3) + 1) +".mp3"));
			sound.play(1.0f);
		}
	}

	class MusicThread extends Thread {
		public void run() {
			soundMusic = Gdx.audio.newSound(Gdx.files.internal("bin/soundMusic.mp3"));
			soundMusic.loop(0.4f);
			try {
				while (!gameMachine.gameIsOver)
				{
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			soundMusic.stop();
		}
	}

	class MyContactListener extends ContactListener {
		@Override
		public boolean onContactAdded (int userValue0, int partId0, int index0, boolean match0, int userValue1, int partId1, int index1, boolean match1) {
			/*if (match0)
				((ColorAttribute)instances.get(userValue0).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);
			if (match1)
				((ColorAttribute)instances.get(userValue1).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);*/

			new CollisionThread().start();

			return true;
		}
	}

	static class MyMotionState extends btMotionState {
		Matrix4 transform;
		@Override
		public void getWorldTransform (Matrix4 worldTrans) {
			worldTrans.set(transform);
		}
		@Override
		public void setWorldTransform (Matrix4 worldTrans) {
			transform.set(worldTrans);
		}
	}

	static class GameObject extends ModelInstance implements Disposable {
		public final btRigidBody body;
		public final MyMotionState motionState;

		public GameObject (Model model, String node, btRigidBody.btRigidBodyConstructionInfo constructionInfo) {
			super(model, node);
			motionState = new MyMotionState();
			motionState.transform = transform;
			body = new btRigidBody(constructionInfo);
			body.setMotionState(motionState);
		}

		@Override
		public void dispose () {
			body.dispose();
			motionState.dispose();
		}

		static class Constructor implements Disposable {
			public final Model model;
			public final String node;
			public final btCollisionShape shape;
			public final btRigidBody.btRigidBodyConstructionInfo constructionInfo;
			private static Vector3 localInertia = new Vector3();

			public Constructor (Model model, String node, btCollisionShape shape, float mass) {
				this.model = model;
				this.node = node;
				this.shape = shape;
				if (mass > 0f)
					shape.calculateLocalInertia(mass, localInertia);
				else
					localInertia.set(0, 0, 0);
				this.constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
			}

			public GameObject construct () {
				return new GameObject(model, node, constructionInfo);
			}

			@Override
			public void dispose () {
				shape.dispose();
				constructionInfo.dispose();
			}
		}
	}

	@Override
	public void create () {
		Bullet.init();

		final float gravity = -99.8f;

		Ball bowlingBall = new Ball();
		Pin bowlingPin = new Pin();

		globalInitialization(gravity);
		elementsInit(bowlingBall, bowlingPin);

		ModelBuilder mb = new ModelBuilder();		
		modelBuilderConstructor(bowlingBall, bowlingPin, mb);

		constructors = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);
		constructersConstructor();

		configEnvironment();

		try {
			gameMachine = new GameMachine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		gameMachine.configSpawnTime(gameMachine.timeToSpawn);
		startSound();
	}

	private void startSound() {
		new MusicThread().start();
	}

	private void configEnvironment() {
		buildAlley();
		ballConfig();
		//pin10set();
	}

	private void ballConfig()
	{
		ballGo = constructors.get("ball").construct();
		ballGo.transform.trn(1000f, 10f, 0);
		ballGo.body.proceedToTransform(ballGo.transform);
		ballGo.body.setUserValue(instances.size);
		ballGo.body.setCollisionFlags(ballGo.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
		instances.add(ballGo);
		dynamicsWorld.addRigidBody(ballGo.body);
		ballGo.body.setContactCallbackFlag(OBJECT_FLAG);
		ballGo.body.setContactCallbackFilter(GROUND_FLAG);

		//propriedades da bola
		ballGo.body.setFriction(1);
		ballGo.body.setRollingFriction(1);
		chooseBallType(0);
	}

	public void moveBallRight() {
		if (ballGo.body.getCenterOfMassPosition().z > -35)
		{
			ballGo.transform.trn(0f, 0f, -1f);
			ballGo.body.proceedToTransform(ballGo.transform);
		}
	}

	public void moveBallLeft() {
		if (ballGo.body.getCenterOfMassPosition().z < 35)
		{
			ballGo.transform.trn(0f, 0f, 1f);
			ballGo.body.proceedToTransform(ballGo.transform);
		}
	}

	public void moveBallRight(float i) {
		if (ballGo.body.getCenterOfMassPosition().z - i > -35)
		{
			ballGo.transform.trn(0f, 0f, -i);
			ballGo.body.proceedToTransform(ballGo.transform);
		}
	}

	public void moveBallLeft(float i) {
		if (ballGo.body.getCenterOfMassPosition().z + i < 35)
		{
			ballGo.transform.trn(0f, 0f, i);
			ballGo.body.proceedToTransform(ballGo.transform);
		}
	}

	public void chooseBallType(int i) {
		switch (i)
		{
		case 0:
			setColor(Color.WHITE);
			setMass(2.72f);
			break;
		case 1:
			setColor(Color.BLUE);
			setMass(3.72f);
			break;
		case 2:
			setColor(Color.ORANGE);
			setMass(4.72f);
			break;
		case 3:
			setColor(Color.PURPLE);
			setMass(6f);
			break;
		case 4:
			setColor(Color.BLACK);
			setMass(7.26f);
			break;
		default:
		{
			Random gerador = new Random();

			int numero = gerador.nextInt(5);

			chooseBallType(numero);

			break;
		}
		}
	}

	private void setColor(Color c) {
		if (c.equals(Color.ORANGE))
		{
			((ColorAttribute)ballGo.materials.get(0).get(ColorAttribute.Diffuse)).color.set(211f / 255, 117f / 255, 12/ 255, 1f);
			return;
		}
		((ColorAttribute)ballGo.materials.get(0).get(ColorAttribute.Diffuse)).color.set(c);
	}

	private void setMass(float fl) {
		ballGo.body.setMassProps(fl, new Vector3(500f, 500f, 500f));
	}

	public void releaseBall(float directionFront, float directionSide)
	{
		gameMachine.launching = true;
		ballGo.body.applyCentralImpulse(new Vector3(directionFront, 0f, directionSide));
		gameMachine.configTimerToEnd();
	}

	private void buildAlley() {
		GameObject object = constructors.get("ground").construct();
		object.transform.trn(-80f, 10f, 0);
		object.body.setCollisionFlags(object.body.getCollisionFlags()
				| btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
		instances.add(object);
		dynamicsWorld.addRigidBody(object.body);
		//object.body.setContactCallbackFlag(GROUND_FLAG);
		object.body.setContactCallbackFilter(0);
		object.body.setActivationState(Collision.DISABLE_DEACTIVATION);

		object = constructors.get("floor").construct();
		object.transform.trn(600f, -3f, 0);
		object.body.setCollisionFlags(object.body.getCollisionFlags()
				| btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
		instances.add(object);
		dynamicsWorld.addRigidBody(object.body);
		//object.body.setContactCallbackFlag(GROUND_FLAG);
		object.body.setContactCallbackFilter(0);
		object.body.setActivationState(Collision.DISABLE_DEACTIVATION);
	}

	private void setGravity(float gravity, btDynamicsWorld dynamicsWorld) {
		dynamicsWorld.setGravity(new Vector3(0f, gravity, 0));
	}

	private void constructersConstructor() {
		constructors.put("ground", new GameObject.Constructor(model, "ground", new btBoxShape(new Vector3(7.5f, 75f, 90f)), 0f));
		constructors.put("floor", new GameObject.Constructor(model, "floor", new btBoxShape(new Vector3(600f, 0.5f, 50f)), 0f));
		constructors.put("ball", new GameObject.Constructor(model, "ball", new btSphereShape(9.5f), 1f));

		for (int i = 1; i <= 10; i++)
		{
			constructors.put("pin" + i, new GameObject.Constructor(model, "pin", new btCylinderShape(new Vector3(5f, 17f, 5f)), 1f));
		}
	}

	private void modelBuilderConstructor(Ball bowlingBall, Pin bowlingPin,
			ModelBuilder mb) {
		mb.begin();
		mb.node().id = "ground";
		mb.part("ground", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.RED)))
		.box(15f, 150f, 180f);
		mb.node().id = "floor";
		mb.part("floor", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse((float)0.8, (float)0.085, 0, 1)))
		.box(1200f, 1f, 100f);
		mb.node("pin", bowlingPin.model);
		mb.node("ball", bowlingBall.model);
		model = mb.end();
	}

	private void elementsInit(Ball bowlingBall,	Pin bowlingPin) {
		bowlingAlley = new Alley();
		bowlingAlley.create();

		bowlingBall.create();

		bowlingPin.create();
	}

	private void globalInitialization(float gravity) {
		modelBatch = new ModelBatch();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		setCam();

		instances = new Array<GameObject>();
		collisionConfig = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfig);
		contactListener = new MyContactListener();
		constraintSolver = new btSequentialImpulseConstraintSolver();
		broadphase = new btDbvtBroadphase();
		dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
		setGravity(gravity, dynamicsWorld);
	}

	private void setCam() {
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(1300f, 100f, 0f);
		cam.lookAt(0,0,0);
		cam.near = 5f;
		cam.far = 3000f;
		cam.update();

		camController = new CameraInputController(cam);
		camController.scrollFactor = -3;
		Gdx.input.setInputProcessor(camController);

		camController.update();
	}

	private void pin10set() {
		for (int i = 1; i <= 10; i++)
		{
			pinGo[i-1] = constructors.get("pin" + i).construct();
			pinGoTrn(i);
			pinGo[i-1].body.proceedToTransform(pinGo[i-1].transform);
			pinGo[i-1].body.setUserValue(instances.size);
			pinGo[i-1].body.setCollisionFlags(pinGo[i-1].body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
			instances.add(pinGo[i-1]);
			dynamicsWorld.addRigidBody(pinGo[i-1].body);
			//pinGo[i-1].body.setContactCallbackFlag(OBJECT_FLAG);
			pinGo[i-1].body.setContactCallbackFlag(GROUND_FLAG);
			pinGo[i-1].body.setContactCallbackFilter(OBJECT_FLAG);
			pinGo[i-1].body.setMassProps(1.64f, new Vector3(500f, 500f, 500f));
		}
	}

	private void pinGoTrn(int i) {
		switch (i)
		{
		case 1:
			pinGo[i-1].transform.trn(30f, 10f, -30f);
			break;
		case 2:
			pinGo[i-1].transform.trn(30f, 10f, -10f);
			break;
		case 3:
			pinGo[i-1].transform.trn(30f, 10f, 10f);
			break;
		case 4:
			pinGo[i-1].transform.trn(30f, 10f, 30f);
			break;
		case 5:
			pinGo[i-1].transform.trn(50f, 10f, -20f);
			break;
		case 6:
			pinGo[i-1].transform.trn(50f, 10f, 0f);
			break;
		case 7:
			pinGo[i-1].transform.trn(50f, 10f, 20f);
			break;
		case 8:
			pinGo[i-1].transform.trn(70f, 10f, -10f);
			break;
		case 9:
			pinGo[i-1].transform.trn(70f, 10f, 10f);
			break;
		case 10:
			pinGo[i-1].transform.trn(90f, 10f, 0f);
			break;
		default:
			break;
		}
	}

	@Override
	public void render () {
		final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

		dynamicsWorld.stepSimulation(delta, 5, 1f/60f);

		if (ballGo.body.getCenterOfMassPosition().x > 50)
		{
			cam.position.set(ballGo.body.getCenterOfMassPosition().x + 300, 100, cam.position.z);
			cam.update();
		}

		camController.update();

		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		bowlingAlley.modelBatch.begin(cam);
		bowlingAlley.modelBatch.render(bowlingAlley.instance, environment);
		bowlingAlley.modelBatch.end();

		modelBatch.begin(cam);
		modelBatch.render(instances, environment);
		modelBatch.end();

		boolean temp_play = gameMachine.isPlayer1Turn;
		
		try {
			if (gameMachine.gameIsOver == false)
			{
				if (gameMachine.initial && !(gameMachine.checkInitialConnection(delta))) //inicial
				{
					//ligaram
				}
				else
				{
					if (hasLaunchEnded(delta))
					{
						if ((gameMachine.spawnTimer -= delta) < 0)
						{
							gameMachine.configTimerToEnd();
							gameMachine.configSpawnTime(gameMachine.timeToSpawn);
							gameMachine.launching = false;
							checkPinsUp();
							gameMachine.notifyPlayer(pinUp);
						}
					}

					if (gameMachine.launching == false && gameMachine.gameIsOver == false)
					{
						if (gameMachine.isPlayer1Turn && !gameMachine.waitingPlayer)
						{
							if (temp_play != gameMachine.isPlayer1Turn || gameMachine.restartPins)
							{
								gameMachine.newPlay();
								restartPins();
								newBallLaunch();
							}
							else
							{
								arePinsUp();
								finish();
							}

							if (gameMachine.waitingPlayer == false)
							{
								gameMachine.waitingPlayer = true;
								gameMachine.gameServer.sendMessagePlayer(1, "Turno", 1);
								gameMachine.getPlayerPlay(this, true);

								if (gameMachine.is2Players)
								{
									gameMachine.sendPoints(false);
								}
							}
						}
						else if (!gameMachine.waitingPlayer)
						{
							if (temp_play != gameMachine.isPlayer1Turn || gameMachine.restartPins)
							{
								gameMachine.newPlay();
								restartPins();
								newBallLaunch();
							}
							else
							{
								arePinsUp();
								finish();
							}

							if (gameMachine.waitingPlayer == false)
							{
								gameMachine.waitingPlayer = true;
								gameMachine.gameServer.sendMessagePlayer(2, "Turno", 1);
								gameMachine.sendPoints(true);
								if (gameMachine.is2Players)
								{
									gameMachine.getPlayerPlay(this, false);
								}
								else
								{
									gameMachine.computerPlay(this);	
									gameMachine.launching = true;
									gameMachine.waitingPlayer = false;
								}
							}
						}
						gameMachine.configTimerToEnd();
						gameMachine.configSpawnTime(gameMachine.timeToSpawn);
					}
				}
			}
			else //jogo acabou
			{
				gameMachine.spriteBatch.begin();
				gameMachine.backgroundSprite.draw(gameMachine.spriteBatch);
				gameMachine.spriteBatch.end();

				if ((gameMachine.timerToEnd -= delta) < 0)
				{					
					System.exit(0);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private boolean hasLaunchEnded(final float delta) {
		return gameMachine.launching && (ballGo.body.getCenterOfMassPosition().x < 15
				|| ballGo.body.getCenterOfMassPosition().y < -55
				|| ballGo.body.getCenterOfMassPosition().y > 55
				|| ballGo.body.getCenterOfMassPosition().z < -20
				|| (gameMachine.timerToEnd -= delta) < 0);
	}

	private void restartPins() {
		newPinsLaunch();
		for (int i = 1; i <= 10; i++)
		{
			pinUp[i-1] = true;
		}
	}

	private void checkPinsUp() {
		int index;
		for (int i = 1; i <= 10; i++)
		{
			if (pinUp[i-1])
			{
				if (pinGo[i-1] != null && pinGo[i-1].body != null && pinGo[i-1].body.getCenterOfMassPosition() != null)
				{
					pinUp[i-1] = !(pinGo[i-1].body.getCenterOfMassPosition().y < 10);

					if (!pinUp[i-1])
					{
						index = instances.indexOf(pinGo[i-1], true);
						if (index >= 0)
						{
							instances.removeIndex(index);
							dynamicsWorld.removeRigidBody(pinGo[i-1].body);
							pinGo[i-1].dispose();
						}
					}
				}
			}
		}

	}

	private void arePinsUp() {
		boolean isStrike = false;
		for (int i = 1; i <= 10; i++)
		{
			if (pinGo[i-1] != null && pinUp[i-1])
			{
				pinUp[i-1] = !(pinGo[i-1].body.getCenterOfMassPosition().y < 10);
			}
			else
			{
				pinUp[i-1] = false;
			}
			isStrike = isStrike || pinUp[i-1];
		}

		gameMachine.restartPins = false;

		if (!isStrike)
		{
			for (int i = 1; i <= 10; i++)
			{
				pinUp[i-1] = true;
			}
			gameMachine.newPlay();
			gameMachine.restartPins = true;
		}
	}

	private void finish() {
		int index;
		for (int i = 1; i <= 10; i++)
		{
			if (!pinUp[i-1])
			{
				index = instances.indexOf(pinGo[i-1], true);
				if (index >= 0)
				{
					instances.removeIndex(index);
					dynamicsWorld.removeRigidBody(pinGo[i-1].body);
					pinGo[i-1].dispose();
				}
				continue;
			}

			pinGo[i-1].body.clearForces();
			pinGo[i-1].body.applyGravity();
			pinGo[i-1].transform.trn(-pinGo[i-1].transform.M03, -pinGo[i-1].transform.M13 + 17, -pinGo[i-1].transform.M23);
			pinGoTrn(i);
		}

		if (gameMachine.restartPins)
		{
			pin10set();
		}

		newBallLaunch();
	}

	private void newBallLaunch() {
		instances.removeIndex(instances.indexOf(ballGo, true));
		dynamicsWorld.removeRigidBody(ballGo.body);
		ballGo.dispose();
		gameMachine.configTimerToEnd();
		ballConfig();
	}

	private void newPinsLaunch() {
		int index;
		for (int i = 1; i <= 10; i++)
		{
			if (pinUp[i-1] != null && pinUp[i-1])
			{
				index = instances.indexOf(pinGo[i-1], true);
				if (index >= 0)
				{
					instances.removeIndex(index);
					dynamicsWorld.removeRigidBody(pinGo[i-1].body);
					pinGo[i-1].dispose();
				}
			}
		}
		pin10set();
	}

	@Override
	public void dispose () {
		for (GameObject obj : instances)
			obj.dispose();
		instances.clear();

		for (GameObject.Constructor ctor : constructors.values())
			ctor.dispose();
		constructors.clear();

		dynamicsWorld.dispose();
		constraintSolver.dispose();
		broadphase.dispose();
		dispatcher.dispose();
		collisionConfig.dispose();

		contactListener.dispose();

		modelBatch.dispose();
		model.dispose();
	}

}
