package funicular;

import java.text.DecimalFormat;

import Jama.Matrix;

public class Optimizer_EquilibriumM6  {
	private DecimalFormat df=new DecimalFormat("##.###");

	public final Mesh mesh;
	public final Integer iteration;
	public final double globalDensity;
	private static final double ratio_update_geometry = 0.3; // related with weight(global_density)
	
	private final int membsize, nodesize;
	private final double[][] H, Q, B, C, G;
	private final double[] gravity = { 0, 0, -1 };
	private double[][] gra;
	
	private double[][] sigma;

	public Optimizer_EquilibriumM6(Mesh me, int ite, double den) {
		this.mesh = me;
		this.iteration = ite;
		this.globalDensity = den;
		membsize = mesh.membsize;
		nodesize = mesh.nodesize;

		B = new double[membsize][nodesize];
		C = new double[membsize][nodesize];
		sigma = new double[membsize][membsize];
		for (int i = 0; i < membsize; i++) {
			int[] member = mesh.members[i];
			int m0 = member[0];
			int m1 = member[1];
			B[i][m0] = 1;
			B[i][m1] = 1;
			C[i][m0] = -1; // signed
			C[i][m1] = 1; // singed
		}

		G = new double[nodesize][nodesize];
		for (int i = 0; i < nodesize; i++) {
			if (!mesh.fixed(i))
				G[i][i] = 1;
		}

		double[][] a = M.mul(M.mul(C, G), M.transpose(C));
		double[][] dig_inv = new double[membsize][membsize];
		double[][] identity = new double[membsize][membsize];
		for (int i = 0; i < membsize; i++) {
			if (Math.abs(a[i][i]) < 0.000001)
				dig_inv[i][i] = Double.MAX_VALUE;
			else
				dig_inv[i][i] = 1 / a[i][i];
			identity[i][i] = 1;
		}
		Q = M.mul(dig_inv, M.mul(C, G));// ***
		H = M.mul(dig_inv, a); // ***
	}

	public void optimize() {
		for (int i = 0; i < iteration; i++) {
			updateGeometry();
		}
	}

	public void updateGeometry() {
		double[][] nodes = mesh.nodes;
		double[][] u = M.mul(C, nodes);
		double[] L = new double[membsize];
		for (int i = 0; i < membsize; i++)
			L[i] = M.mag(u[i]);
		for (int i = 0; i < membsize; i++)
			M._normalize(u[i]);

		double[][] HSu = M.mul(H, M.mul(sigma, u));//********

		double[][] LV = new double[membsize][3];
		for (int i = 0; i < membsize; i++)
			for (int j = 0; j < 3; j++)
				LV[i][j] = 0.5 * globalDensity * L[i] * gravity[j];
		gra = M.mul(M.transpose(B), LV);
		double[][] Qgra = M.mul(Q, gra);

		double[][] re = M.mul(M.add(HSu, Qgra), M.transpose(u));//******************
		for (int i = 0; i < membsize; i++) {
			for (int j = 0; j < membsize; j++) {
				if (i != j)
					re[i][j] = 0;
			}
		}
		M._add( sigma,   M.scale(re, -0.5));// M.scale(re, -0.5))   0.3-0.6

		double[][] KSu = M.mul(M.transpose(C), M.mul(sigma, u));
		double[][] GKSu_gra = M.mul(G, M.add(KSu, gra));

		M._add(nodes, M.scale(GKSu_gra, ratio_update_geometry));
	}


}
