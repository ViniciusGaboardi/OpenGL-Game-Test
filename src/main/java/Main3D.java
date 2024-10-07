
import obj.*;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import Model.VboBilboard;
import Model.VboCube;
import dados.Constantes;
import shaders.StaticShader;
import util.TextureLoader;
import util.Utils3D;

import java.awt.image.BufferedImage;

//import com.sun.org.apache.xerces.internal.dom.DeepNodeListImpl;

import java.nio.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main3D {

	// The window handle
	private long window;
	int score;
	int life;

	float viewAngX = 0;
	float viewAngY = 0;
	float scale = 1.0f;
	
	public Random rnd = new Random();

	VboCube vboc;
	VboBilboard vboBilbord;
	StaticShader shader;
	ArrayList<Object3D> listaObjetos = new ArrayList<>();
	ArrayList<Object3D> listaEnemies = new ArrayList<>();
	
	
	Vector4f cameraPos = new  Vector4f(0.0f,10.0f, 0.0f,1.0f);
	Vector4f cameraVectorFront = new Vector4f(0.0f, 0.0f, -1.0f,1.0f);
	Vector4f cameraVectorUP = new Vector4f(0.0f, 1.0f, 0.0f,1.0f);
	Vector4f cameraVectorRight = new Vector4f(1.0f, 0.0f, 0.0f,1.0f);
	
	Matrix4f view = new Matrix4f();
	
	boolean UP = false;
	boolean DOWN = false;
	boolean LEFT = false;
	boolean RIGHT = false;
	
	boolean FORWARD = false;
	boolean BACKWARD = false;
	
	boolean QBu = false;
	boolean EBu = false;

	boolean FIRE = false;
	
	Matrix4f cameraMatrix = new Matrix4f();
	

	FloatBuffer matrixBuffer = memAllocFloat(16);
	
	Cubo3D umcubo;
	Player m29;
	Enemy m29enemy;

	double angluz = 0;

	double deltaX;
	double deltaY;
	float deltaRotVelX;
	float deltaRotVelY;
	float brakeVel = 0.5f;

	long timerfechar = 0;

	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		score = 0;
		life = 10;

		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(1500, 1000, "Hello World!", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated
		// or released.
		
		
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			if(!m29.morrendo){
				if(action == GLFW_PRESS) {
					if ( key == GLFW_KEY_W) {
						UP = true;
					}
					if ( key == GLFW_KEY_S) {
						DOWN = true;
					}
					if ( key == GLFW_KEY_A) {
						RIGHT = true;
					}
					if ( key == GLFW_KEY_D) {
						LEFT = true;
					}
					if ( key == GLFW_KEY_Q) {
						QBu = true;
					}
					if ( key == GLFW_KEY_E) {
						EBu = true;
					}
					if ( key == GLFW_KEY_UP) {
						FORWARD = true;
					}
					if ( key == GLFW_KEY_DOWN) {
						BACKWARD = true;
					}
					if ( key == GLFW_KEY_SPACE) {
						FIRE = true;
					}
				}
				if(action == GLFW_RELEASE) {
					if ( key == GLFW_KEY_W) {
						UP = false;
					}
					if ( key == GLFW_KEY_S) {
						DOWN = false;
					}
					if ( key == GLFW_KEY_A) {
						RIGHT = false;
					}
					if ( key == GLFW_KEY_D) {
						LEFT = false;
					}
					if ( key == GLFW_KEY_Q) {
						QBu = false;
					}
					if ( key == GLFW_KEY_E) {
						EBu = false;
					}
					if ( key == GLFW_KEY_UP) {
						FORWARD = false;
					}
					if ( key == GLFW_KEY_DOWN) {
						BACKWARD = false;
					}
					if ( key == GLFW_KEY_SPACE) {
						FIRE = false;
					}
				}
			}
		});

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}

	private void checkCollisions() {
		List<Enemy> inimigosARemover = new ArrayList<>();


		for (Object3D projetil : listaObjetos) {
			if (projetil instanceof Projetil) {
				for (Object3D obj : listaEnemies) {
					if (obj instanceof Enemy && ((Projetil) projetil).verificaColisao((Enemy) obj)) {
						projetil.colidirComEnemy((Enemy) obj); // Aplicar efeito de explosão ou colisão
						inimigosARemover.add((Enemy) obj); // Marcar inimigo para remoção
						score += 10;
					}
				}
			}
			if (projetil instanceof ProjetilEnemy) {
				if (((ProjetilEnemy) projetil).verificaColisao(m29) && damagetimer > 1000) {
					((ProjetilEnemy) projetil).colidirComPlayer(m29); // Aplicar efeito de explosão ou colisão
					life -= 1;
					System.out.println("Tomou dano de projétil, Vida atual: "+life);
					damagetimer = 0;
				}
			}
		}
		for (Object3D enemy : listaEnemies) {
			if(((Enemy) enemy).testaColisao(m29.x, m29.y, m29.z, 0.1f) && damagetimer > 1000){
				life -= 3;
				System.out.println("Tomou dano de colisão, Vida atual: "+life);
				damagetimer = 0;
			}
		}

		// Remover todos os inimigos marcados para remoção
		listaEnemies.removeAll(inimigosARemover);
	}

	private void checkLife(long diftime){
		if(life <= 0)
			m29.morrendo = true;
		if(m29.morrendo){
			timerfechar += diftime;
			//System.out.println("Esta morrendo");
			if(timerfechar%100 == 0 || timerfechar%1000 == 0){
				ProjetilEnemy pj = new ProjetilEnemy(m29.x + cameraVectorFront.x * 0.5f, m29.y + cameraVectorFront.y * 0.5f, m29.z + cameraVectorFront.z * 0.5f);
				pj.vx = 0;
				pj.vy = 0;
				pj.vz = 0;
				pj.raio = 0.2f;
				pj.model = vboBilbord;
				pj.setRotation(cameraVectorFront, cameraVectorUP, cameraVectorRight);

				listaObjetos.add(pj);

				pj.morrendo = true;
			}
			if(timerfechar > 2500){
				System.out.println("Morreu");
				System.out.println("Pontuação total: " + score);
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			}
		}
	}

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.

		GL.createCapabilities();

		view.setIdentity();
		
		vboc = new VboCube();
		vboc.load();
		vboBilbord = new VboBilboard();
		vboBilbord.load();
		shader = new StaticShader();
		
		System.out.println("------> Carrega MIG");
		ObjModel mig29 = new ObjModel();
		mig29.loadObj("Mig_29_obj.obj");
		mig29.load();
		
		m29 = new Player(0, 0, 0, 0.01f);
		m29.model = mig29;

		//Teste de inimigo
		ObjModel mig29enemy = new ObjModel();
		mig29enemy.loadObj("Mig_29_obj.obj");
		mig29enemy.load();

		/*
		m29enemy = new Enemy(0,6,0,0.01f);
		m29enemy.model = mig29;
		listaEnemies.add(m29enemy);

		m29enemy = new Enemy(2,7,1,0.01f);
		m29enemy.model = mig29;
		listaEnemies.add(m29enemy);
		*/
		
		ObjHTGsrtm model = new ObjHTGsrtm();
		model.load();
		
		Constantes.mapa = new Mapa3D(-100.0f, 0.0f, -100.0f, 10);
		Constantes.mapa.model = model;
		
		umcubo = new Cubo3D(0.0f, 0.0f, 0.8f, 0.1f);
		umcubo.model = vboc;
		
		BufferedImage imggato = TextureLoader.loadImage("texturaGato.jpeg");
		Constantes.tgato = TextureLoader.loadTexture(imggato);
		//System.out.println("tgato "+Constantes.tgato);
		
		BufferedImage imgmulttexture = TextureLoader.loadImage("multtexture.png");
		Constantes.tmult = TextureLoader.loadTexture(imgmulttexture);
		//System.out.println("tmult "+Constantes.tmult);

		BufferedImage texturamig = TextureLoader.loadImage("TexturaMig01.png");
		Constantes.txtmig = TextureLoader.loadTexture(texturamig);
		//System.out.println("tmult "+Constantes.tmult);

		BufferedImage texturamig2 = TextureLoader.loadImage("TexturaMig02.png");
		Constantes.txtmigenemy = TextureLoader.loadTexture(texturamig2);
		//System.out.println("tmult "+Constantes.tmult);
		
		BufferedImage imgtexttiro = TextureLoader.loadImage("texturaTiro.png");
		Constantes.texturaTiro = TextureLoader.loadTexture(imgtexttiro);
		//System.out.println("texturaTiro "+Constantes.texturaTiro);
		
		BufferedImage imgtextexp = TextureLoader.loadImage("texturaExplosao.png");
		Constantes.texturaExplosao = TextureLoader.loadTexture(imgtextexp);
		//System.out.println("texturaExplosao "+Constantes.texturaExplosao);

		int frame = 0;
		long lasttime = System.currentTimeMillis();

		float angle = 0;

		//Coisas do mouse
		boolean mouseLocked = false;

		double newMouseX = 750;
		double newMouseY = 500;

		double prevMouseX = 0;
		double prevMouseY = 0;

		boolean rotMouseX = false;
		boolean rotMouseY = false;

		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		//Fim das coisas do mouse

		long ultimoTempo = System.currentTimeMillis();
		while (!glfwWindowShouldClose(window)) {
			
			long diftime = System.currentTimeMillis()-ultimoTempo;
			ultimoTempo = System.currentTimeMillis();
			
			gameUpdate(diftime);
			gameRender();

			frame++;
			long actualTime = System.currentTimeMillis();
			if ((lasttime / 1000) != (actualTime / 1000)) {
				//System.out.println("FPS " + frame);
				frame = 0;
				lasttime = actualTime;
			}
			//Coisas do mouse
			if(m29.morrendo == false) {
				if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) != GLFW_PRESS) {
					//glfwSetCursorPos(window, 1500/2, 1000/2);
					brakeVel = 1 / 256;
					FORWARD = true;
					mouseLocked = true;
				}
				if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_RELEASE) {
					FORWARD = false;
					brakeVel = 0.5f;
				}
				if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) == GLFW_PRESS && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) != GLFW_PRESS) {
					BACKWARD = true;
					brakeVel = 1.5f;
				}
				if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) == GLFW_RELEASE) {
					BACKWARD = false;
					brakeVel = 0.5f;
				}

				if (mouseLocked) {
					DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
					DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

					glfwGetCursorPos(window, x, y);
					x.rewind();
					y.rewind();

					newMouseX = x.get();
					newMouseY = y.get();

					deltaX = newMouseX - 750;
					deltaY = newMouseY - 500;

					rotMouseX = newMouseX != prevMouseX;
					rotMouseY = newMouseY != prevMouseY;

					if (rotMouseY) {
						//System.out.println("ROTATE Y AXIS: " + deltaY);

					}
					if (rotMouseX) {
						//System.out.println("ROTATE X AXIS: " + deltaX);
					}

					prevMouseX = newMouseX;
					prevMouseY = newMouseY;


					//System.out.println("Delta X = " + deltaX + " Delta Y = " + deltaY);

					glfwSetCursorPos(window, 1500 / 2, 1000 / 2);
				}
			}
			//Fim das coisas do mouse

            checkCollisions();

			updateEnemyOrientation(diftime);

			checkLife(diftime);
		}
	}

	private void updateEnemyOrientation(long diftime) {
		// Posição do m29
		Vector3f m29Pos = new Vector3f(m29.x, m29.y, m29.z);

		for (Object3D enemy : listaEnemies) {
			if (enemy instanceof Enemy) {
				// Posição do inimigo
				Vector3f enemyPos = new Vector3f(enemy.x, enemy.y, enemy.z);

				// Calcular direção
				Vector3f direction = Vector3f.sub(m29Pos, enemyPos, null);

				// Normalizar a direção
				direction.normalise();

				// Criar vetor UP (pode ser um vetor unitário apontando para cima)
				Vector3f up = new Vector3f(0, 1, 0);

				// Criar matriz de rotação usando a função lookAt
				Matrix4f lookAtMatrix = new Matrix4f();
				lookAt(direction, up, lookAtMatrix);

				// Definir a orientação do inimigo
				((Enemy) enemy).Front = new Vector4f(-direction.x, -direction.y, -direction.z, 0);
				((Enemy) enemy).Right = new Vector4f(lookAtMatrix.m00, lookAtMatrix.m01, lookAtMatrix.m02, 0);
				((Enemy) enemy).UP = new Vector4f(lookAtMatrix.m10, lookAtMatrix.m11, lookAtMatrix.m12, 0);

				// Calcular a distância ao jogador
				float distance = Vector3f.sub(m29Pos, enemyPos, null).length();

				// Atualizar a velocidade do inimigo
				if (distance < 5.0f) { // Se a distância for menor que 5 unidades, acelere
					((Enemy) enemy).speed -= ((Enemy) enemy).acceleration * diftime / 1000.0f;
					if (((Enemy) enemy).speed < 0) {
						((Enemy) enemy).speed = 0;
					}
				} else { // Caso contrário, acelere
					((Enemy) enemy).speed += ((Enemy) enemy).acceleration * diftime / 1000.0f;
					if (((Enemy) enemy).speed > ((Enemy) enemy).maxSpeed) {
						((Enemy) enemy).speed = ((Enemy) enemy).maxSpeed;
					}
				}

				// Movimentar o inimigo com base na velocidade atual
				float fatorAleatorio = 0.8f + rnd.nextFloat() * (1.22f - 0.78f);
				((Enemy) enemy).x -= (((Enemy) enemy).Front.x*fatorAleatorio) * ((Enemy) enemy).speed * diftime / 1000.0f;
				fatorAleatorio = 0.8f + rnd.nextFloat() * (1.22f - 0.78f);
				((Enemy) enemy).y -= (((Enemy) enemy).Front.y*fatorAleatorio) * ((Enemy) enemy).speed * diftime / 1000.0f;
				fatorAleatorio = 0.8f + rnd.nextFloat() * (1.22f - 0.78f);
				((Enemy) enemy).z -= (((Enemy) enemy).Front.z*fatorAleatorio) * ((Enemy) enemy).speed * diftime / 1000.0f;
			}
		}
	}

	private void lookAt(Vector3f direction, Vector3f up, Matrix4f dest) {
		Vector3f x = new Vector3f();
		Vector3f y = new Vector3f();
		Vector3f z = new Vector3f();

		// Z axis
		z.set(direction).normalise();

		// X axis = up cross Z
		Vector3f.cross(up, z, x).normalise();

		// Y axis = Z cross X
		Vector3f.cross(z, x, y).normalise();

		dest.m00 = x.x;
		dest.m01 = x.y;
		dest.m02 = x.z;
		dest.m03 = 0.0f;
		dest.m10 = y.x;
		dest.m11 = y.y;
		dest.m12 = y.z;
		dest.m13 = 0.0f;
		dest.m20 = z.x;
		dest.m21 = z.y;
		dest.m22 = z.z;
		dest.m23 = 0.0f;
		dest.m30 = 0.0f;
		dest.m31 = 0.0f;
		dest.m32 = 0.0f;
		dest.m33 = 1.0f;
	}

	long tirotimer = 0;
	long tirotimerenemy = 0;
	long spawntimer = 0;
	long damagetimer = 0;

	private void spawnEnemies(){
		ObjModel mig29enemy = new ObjModel();
		mig29enemy.loadObj("Mig_29_obj.obj");
		mig29enemy.load();

		float fatorAleatorioX = -70.0f + rnd.nextFloat() * (70.0f - (-70.0f));
		float fatorAleatorioY = 20.0f + rnd.nextFloat() * (80.0f - 20.0f);
		float fatorAleatorioZ = -70.0f + rnd.nextFloat() * (70.0f - (-70.0f));
		m29enemy = new Enemy(m29.x+fatorAleatorioX,m29.y+fatorAleatorioY,m29.z+fatorAleatorioZ,0.01f);
		m29enemy.model = mig29enemy;
		listaEnemies.add(m29enemy);
	}


	private void gameUpdate(long diftime) {
		float vel = 5.0f;

		cameraPos.x -= cameraVectorFront.x*vel*diftime/1000.0f;
		cameraPos.y -= cameraVectorFront.y*vel*diftime/1000.0f;
		cameraPos.z -= cameraVectorFront.z*vel*diftime/1000.0f;

		tirotimer+=diftime;
		tirotimerenemy+=diftime;
		spawntimer+=diftime;
		damagetimer+=diftime;
		
		//angluz+=(Math.PI/4)*diftime/1000.0f;
		angluz = 0;
		
		Matrix4f rotTmp = new Matrix4f();
		rotTmp.setIdentity();

		//Rotação para esquerda
		if(deltaX < 0 && deltaX >= -50) {
			rotTmp.rotate((float) (0.1f*deltaX*brakeVel*diftime/1000.0f), new Vector3f(cameraVectorUP.x, cameraVectorUP.y, cameraVectorUP.z));
		}else if(deltaX < -50){
			rotTmp.rotate(0.1f*-50*brakeVel*diftime/1000.0f, new Vector3f(cameraVectorUP.x, cameraVectorUP.y, cameraVectorUP.z));
		}
		//Rotação para Direita
		if(deltaX > 0 && deltaX <= 50) {
			rotTmp.rotate((float) (0.1f*deltaX*brakeVel*diftime/1000.0f), new Vector3f(cameraVectorUP.x, cameraVectorUP.y, cameraVectorUP.z));
		}else if(deltaX > 50){
			rotTmp.rotate(0.1f*50*brakeVel*diftime/1000.0f, new Vector3f(cameraVectorUP.x, cameraVectorUP.y, cameraVectorUP.z));
		}
		//Rotação para baixo
		if(deltaY < 0 && deltaY >= -50) {
			rotTmp.rotate((float) (0.1f*deltaY*brakeVel*diftime/1000.0f), new Vector3f(cameraVectorRight.x, cameraVectorRight.y, cameraVectorRight.z));
		}else if(deltaY < -50){
			rotTmp.rotate(0.1f*-50*brakeVel*diftime/1000.0f, new Vector3f(cameraVectorRight.x, cameraVectorRight.y, cameraVectorRight.z));
		}
		//Rotação para cima
		if(deltaY > 0 && deltaY <= 50) {
			rotTmp.rotate((float) (0.1f*deltaY*brakeVel*diftime/1000.0f), new Vector3f(cameraVectorRight.x, cameraVectorRight.y, cameraVectorRight.z));
		}else if(deltaY > 50){
			rotTmp.rotate(0.1f*50*brakeVel*diftime/1000.0f, new Vector3f(cameraVectorRight.x, cameraVectorRight.y, cameraVectorRight.z));
		}
		//Fim da Movimentação do Mouser

		if(QBu) {
			rotTmp.rotate(-1.0f*diftime/1000.0f, new Vector3f(cameraVectorFront.x, cameraVectorFront.y, cameraVectorFront.z));
		}
		if(EBu) {
			rotTmp.rotate(1.0f*diftime/1000.0f, new Vector3f(cameraVectorFront.x, cameraVectorFront.y, cameraVectorFront.z));
		}

		rotTmp.transform(rotTmp,cameraVectorFront, cameraVectorFront);
		rotTmp.transform(rotTmp,cameraVectorRight, cameraVectorRight);
		rotTmp.transform(rotTmp,cameraVectorUP, cameraVectorUP);
		
		Utils3D.vec3dNormilize(cameraVectorFront);
		Utils3D.vec3dNormilize(cameraVectorRight);
		Utils3D.vec3dNormilize(cameraVectorUP);
		
		if(FORWARD) {
			cameraPos.x -= cameraVectorFront.x*vel*2*diftime/1000.0f;
			cameraPos.y -= cameraVectorFront.y*vel*2*diftime/1000.0f;
			cameraPos.z -= cameraVectorFront.z*vel*2*diftime/1000.0f;
			//System.out.println("UP "+diftime);
		}
		if(BACKWARD) {
			cameraPos.x += cameraVectorFront.x*vel/2*diftime/1000.0f;
			cameraPos.y += cameraVectorFront.y*vel/2*diftime/1000.0f;
			cameraPos.z += cameraVectorFront.z*vel/2*diftime/1000.0f;
			//System.out.println("UP "+diftime);
		}		
		
		Vector4f t = new Vector4f(cameraPos.dot(cameraPos, cameraVectorRight),cameraPos.dot(cameraPos, cameraVectorUP),cameraPos.dot(cameraPos, cameraVectorFront),1.0f);
		
		view = Utils3D.setLookAtMatrix(t, cameraVectorFront, cameraVectorUP, cameraVectorRight);
		
		Matrix4f transf = new Matrix4f();
		transf.setIdentity();
		transf.translate(new Vector3f(1,1,0));
		view.mul(transf,view, view);

		m29.raio = 0.01f;
		m29.Front = cameraVectorFront;
		m29.UP = cameraVectorUP;
		m29.Right = cameraVectorRight;
		m29.x = cameraPos.x - cameraVectorFront.x*2;
		m29.y = cameraPos.y - cameraVectorFront.y*2;
		m29.z = cameraPos.z - cameraVectorFront.z*2;

		if(Constantes.mapa.testaColisao(m29.x, m29.y, m29.z, 0.1f)){
			life -= 1;
		}

		if(FIRE&&tirotimer>=100&&!m29.morrendo) {
			float velocidade_projetil = 20;
			Projetil pj = new Projetil(m29.x + cameraVectorRight.x * 0.5f + cameraVectorUP.x * 0.2f,
					m29.y + cameraVectorRight.y * 0.5f + cameraVectorUP.y * 0.2f,
					m29.z + cameraVectorRight.z * 0.5f + cameraVectorUP.z * 0.2f);
			pj.vx = -cameraVectorFront.x * velocidade_projetil;
			pj.vy = -cameraVectorFront.y * velocidade_projetil;
			pj.vz = -cameraVectorFront.z * velocidade_projetil;
			pj.raio = 0.2f;
			pj.model = vboBilbord;
			pj.setRotation(cameraVectorFront, cameraVectorUP, cameraVectorRight);

			listaObjetos.add(pj);

			pj = new Projetil(m29.x - cameraVectorRight.x * 0.5f + cameraVectorUP.x * 0.2f,
					m29.y - cameraVectorRight.y * 0.5f + cameraVectorUP.y * 0.2f,
					m29.z - cameraVectorRight.z * 0.5f + cameraVectorUP.z * 0.2f);
			pj.vx = -cameraVectorFront.x * velocidade_projetil;
			pj.vy = -cameraVectorFront.y * velocidade_projetil;
			pj.vz = -cameraVectorFront.z * velocidade_projetil;
			pj.raio = 0.2f;
			pj.model = vboBilbord;
			pj.setRotation(cameraVectorFront, cameraVectorUP, cameraVectorRight);

			listaObjetos.add(pj);
			tirotimer = 0;
		}

		//projétil do Inimigo
		if(tirotimerenemy>=1000) {
			for(int i = 0; i < listaEnemies.size(); i++) {
				float fatorAleatorio = 0.98f + rnd.nextFloat() * (1.12f - 0.88f);
				float velocidade_projetil = 10;
				Object3D enemy = listaEnemies.get(i);

				ProjetilEnemy pj = new ProjetilEnemy(enemy.x, enemy.y, enemy.z);
				pj.vx = -((Enemy) enemy).Front.x * velocidade_projetil * fatorAleatorio;
				fatorAleatorio = 0.98f + rnd.nextFloat() * (1.12f - 0.88f);
				pj.vy = -((Enemy) enemy).Front.y * velocidade_projetil * fatorAleatorio;
				fatorAleatorio = 0.98f + rnd.nextFloat() * (1.12f - 0.88f);
				pj.vz = -((Enemy) enemy).Front.z * velocidade_projetil * fatorAleatorio;
				pj.raio = 0.2f;
				pj.model = vboBilbord;
				pj.setRotation(cameraVectorFront, cameraVectorUP, cameraVectorRight);

				listaObjetos.add(pj);
			}
			tirotimerenemy = 0;
		}

		// Atualizar posição dos projéteis
		for (int i = 0; i < listaObjetos.size(); i++) {
			Object3D obj = listaObjetos.get(i);
			if (obj instanceof ProjetilEnemy) {
				((ProjetilEnemy) obj).SimulaSe(diftime);
			}
		}

		if(spawntimer > 5000){
			spawnEnemies();
			spawntimer = 0;
		}

		for(int i = 0; i < listaObjetos.size();i++) {
			Object3D obj = listaObjetos.get(i);
			obj.SimulaSe(diftime);
			if(obj.vivo==false) {
				listaObjetos.remove(i);
				i--;
			}
		}

		for(int i = 0; i < listaObjetos.size();i++) {
			Object3D obj = listaObjetos.get(i);
			obj.SimulaSe(diftime);
			if(obj instanceof Enemy && obj.vivo==false) {
				listaObjetos.remove(i);
				i--;
			}
		}
	}

	private void gameRender() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

		glEnable(GL_LIGHTING);
		glShadeModel(GL_SMOOTH);

		glLoadIdentity();

		shader.start();
		
		int projectionlocation = glGetUniformLocation(shader.programID, "projection");
		//Matrix4f projection = setFrustum(-1f,1f,-1f,1f,1f,100.0f);
		Matrix4f projection = Utils3D.setFrustum(-1.5f,1.5f,-1f,1f,1f,500.0f);
		projection.storeTranspose(matrixBuffer);
		matrixBuffer.flip();
		glUniformMatrix4fv(projectionlocation, false, matrixBuffer);
		
		int lightpos = glGetUniformLocation(shader.programID, "lightPosition");
		
		float yl = (float)(Math.cos(angluz)*50.0);
		float zl = (float)(Math.sin(angluz)*50.0);
		
		float vf[] = {0.0f,yl,zl,1.0f};
		glUniform4fv(lightpos, vf);
		
		glEnable(GL_DEPTH_TEST);
		
		glActiveTexture(GL_TEXTURE0);
		//glBindTexture(GL_TEXTURE_2D, tgato);
		glBindTexture(GL_TEXTURE_2D, Constantes.tmult);
		
		
		int loctexture = glGetUniformLocation(shader.programID, "tex");
		glUniform1i(loctexture, 0);
		
		int viewlocation = glGetUniformLocation(shader.programID, "view");
		view.storeTranspose(matrixBuffer);
		matrixBuffer.flip();
		glUniformMatrix4fv(viewlocation, false, matrixBuffer);
		
		Constantes.mapa.DesenhaSe(shader);
		umcubo.DesenhaSe(shader);		

		glBindTexture(GL_TEXTURE_2D, Constantes.txtmig);
		m29.DesenhaSe(shader);

		glBindTexture(GL_TEXTURE_2D, Constantes.txtmigenemy);
		for(int i = 0; i < listaEnemies.size();i++) {
			listaEnemies.get(i).DesenhaSe(shader);
		}
		
		//glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		glBindTexture(GL_TEXTURE_2D, Constantes.texturaTiro);
		for(int i = 0; i < listaObjetos.size();i++) {
			listaObjetos.get(i).DesenhaSe(shader);
		}
		
		
		shader.stop();
		
		glfwSwapBuffers(window); // swap the color buffers

		glfwPollEvents();
	}

	public static void main(String[] args) {
		new Main3D().run();
	}

	public static void gluPerspective(float fovy, float aspect, float near, float far) {
		float bottom = -near * (float) Math.tan(fovy / 2);
		float top = -bottom;
		float left = aspect * bottom;
		float right = -left;
		glFrustum(left, right, bottom, top, near, far);
	}
}
