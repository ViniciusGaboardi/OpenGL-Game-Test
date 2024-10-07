package obj;

import Model.Model;
import dados.Constantes;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import shaders.ShaderProgram;
import util.Utils3D;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL20.*;

public class ProjetilEnemy extends Object3D {
	public Vector3f cor = new Vector3f();
	public Model model = null;

	FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);
	public float rotvel = 0;

	public Vector4f Front = new Vector4f(0.0f, 0.0f, -1.0f,1.0f);
	public Vector4f UP = new Vector4f(0.0f, 1.0f, 0.0f,1.0f);
	public Vector4f Right = new Vector4f(1.0f, 0.0f, 0.0f,1.0f);

	long timervida = 0;

	public boolean morrendo = false;
	long timermorrendo = 0;

	public float raioColisao = 0.5f; // Raio usado para detecção de colisão
	public ProjetilEnemy(float x, float y, float z) {
		super(x, y, z);
	}
	
	public void setRotation(Vector4f Front, Vector4f UP, Vector4f Right) {
		this.Front = Front;
		this.UP = UP;
		this.Right = Right;
	}
	
	@Override
	public void DesenhaSe(ShaderProgram shader) {
		
		Matrix4f modelm1 = new Matrix4f();
		modelm1.translate(new Vector3f(x,y,z));
		Matrix4f modelm = Utils3D.positionMatrix(Front, UP, Right); //new Matrix4f();
		Matrix4f.mul(modelm1,modelm, modelm);
		modelm.scale(new Vector3f(raio,raio,raio));
		
		int modellocation = glGetUniformLocation(shader.programID, "model");
		modelm.storeTranspose(matrixBuffer);
		matrixBuffer.flip();
		glUniformMatrix4fv(modellocation, false, matrixBuffer);	
		
		int bilbloc = glGetUniformLocation(shader.programID, "bilb");
		
		glUniform1i(bilbloc,1);
		
		//System.out.println("bilbloc "+bilbloc);
		//System.out.println("modellocation "+modellocation);
		
		if(morrendo) {
			glBindTexture(GL_TEXTURE_2D, Constantes.texturaExplosao);
		}else {
			glBindTexture(GL_TEXTURE_2D, Constantes.texturaTiro);
		}
		
		model.draw();
		
		glUniform1i(bilbloc,0);
	}
	
	@Override
	public void SimulaSe(long diftime) {
		super.SimulaSe(diftime);
		
		timervida+=diftime;

		x += vx*diftime/1000.0f;
		y += vy*diftime/1000.0f;
		z += vz*diftime/1000.0f;
		
		if(timervida> 3000) {
			vivo = false;
		}
		
		if(Constantes.mapa.testaColisao(x, y, z, raio)) {
			//vivo = false;
			morrendo = true;
			vx = 0;
			vy = 0;
			vz = 0;
		}
		
		if(morrendo) {
			timermorrendo+=diftime;
			raio = raio*((diftime/400.0f)+1);
			//System.out.println("raio "+raio);
			if(timermorrendo>1000) {
				vivo = false;
			}
		}
		
		//ang+=rotvel*diftime/1000.0f;
	}

	public boolean verificaColisao(Player player) {
		float dx = this.x - player.x;
		float dy = this.y - player.y;
		float dz = this.z - player.z;
		float distancia = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		return distancia < (this.raioColisao + player.raioColisao);
	}

	public void colidirComPlayer(Player player) {
		this.morrendo = true;
		this.vx = 0;
		this.vy = 0;
		this.vz = 0;
		//enemy.recebeDano(this.dano);
	}
}
