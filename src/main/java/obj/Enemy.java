package obj;

import Model.Model;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import shaders.ShaderProgram;
import util.Utils3D;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

public class Enemy extends Object3D {
	public Vector3f cor = new Vector3f();
	public Model model = null;

	FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);
	public float rotvel = 0;
	float ang = 0;
	public float speed = 0f; // velocidade inicial
	public float maxSpeed = 10f; // velocidade máxima
	public float acceleration = 1f; // valor de aceleração
	public float raioColisao = 1.0f; // Defina um valor padrão adequado para o raio de colisão do inimigo
	public float vida = 1f; // Vida inicial do inimigo

	public Vector4f Front = new Vector4f(0.0f, 0.0f, -1.0f,1.0f);
	public Vector4f UP = new Vector4f(0.0f, 1.0f, 0.0f,1.0f);
	public Vector4f Right = new Vector4f(1.0f, 0.0f, 0.0f,1.0f);

	public Enemy(float x, float y, float z, float r) {
		super(x, y, z);
		raio = r;
	}

	public boolean testaColisao(float xt, float yt, float zt, float rt) {
		float prx = xt - x;
		if(prx < 0) prx = prx*-1;
		float pry = yt - y;
		if(prx < 0) pry = pry*-1;
		float prz = zt - z;
		if(prx < 0) pry = pry*-1;

		if (prx <= raio + rt && pry <= raio + rt && prz <= raio + rt) {
			System.out.println(" COLIDIU "+x+" "+xt+" "+y+" "+yt+" "+z+" "+zt+" "+rt);
			return true;
		}else return false;
	}
	
	@Override
	public void DesenhaSe(ShaderProgram shader) {

		
		Matrix4f modelm1 = new Matrix4f();
		modelm1.translate(new Vector3f(x,y,z));
		Matrix4f modelm = Utils3D.positionMatrix(Front, UP, Right); //new Matrix4f();
		Matrix4f.mul(modelm1,modelm, modelm);
		//modelm.setIdentity();
		//modelm.translate(new Vector3f(x,y,z));
		modelm.scale(new Vector3f(raio,raio,raio));
		
		int modellocation = glGetUniformLocation(shader.programID, "model");
		
		modelm.storeTranspose(matrixBuffer);
		matrixBuffer.flip();
		glUniformMatrix4fv(modellocation, false, matrixBuffer);	
		
		model.draw();
	}
	
	@Override
	public void SimulaSe(long diftime) {
		super.SimulaSe(diftime);
		
		x += vx*diftime/1000.0f;
		y += vy*diftime/1000.0f;
		z += vz*diftime/1000.0f;
		
		ang+=rotvel*diftime/1000.0f;
	}
}
