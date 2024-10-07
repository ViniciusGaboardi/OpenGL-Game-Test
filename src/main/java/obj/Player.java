package obj;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.*;

import shaders.ShaderProgram;
import util.Utils3D;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import Model.Model;
import Model.VboCube;

public class Player extends Object3D {
	public Vector3f cor = new Vector3f();
	public Model model = null;
	
	FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);
	public float rotvel = 0;
	float ang = 0;
	
	public Vector4f Front = new Vector4f(0.0f, 0.0f, -1.0f,1.0f);
	public Vector4f UP = new Vector4f(0.0f, 1.0f, 0.0f,1.0f);
	public Vector4f Right = new Vector4f(1.0f, 0.0f, 0.0f,1.0f);

	public float raioColisao = 0.5f; // Defina um valor padrão adequado para o raio de colisão do inimigo

	public boolean morrendo = false;
	long timermorrendo = 0;
	
	public Player(float x, float y, float z, float r) {
		super(x, y, z);
		raio = r;
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
		
		
		//Matrix4f posmatrix = Utils3D.setLookAtMatrixB(new Vector4f(x,y,z,1.0f), Front, UP, Right);
		//modelm.mul(modelm,posmatrix, modelm);
		//modelm.scale(new Vector3f(raio,raio,raio));
		
		//modelm.translate(new Vector3f(x,y,z));
		//modelm.scale(new Vector3f(raio,raio,raio));
		//modelm.rotate(ang,new Vector3f(0.0f,1.0f,0.0f));
		
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

		if (morrendo) {
			timermorrendo += diftime;

			if (timermorrendo > 100) {
				vivo = false;
			}
		}
	}
}
