package animation;

import org.ujmp.core.Matrix;
import org.ujmp.core.doublematrix.SparseDoubleMatrix2D;
import funicular.M;
import funicular.Mesh;

public class ForceDensity {
	private final Mesh mesh;
	private Integer[] all2N, all2F; // force density
	private double[][] loads;
	private double[][] XF;
	private final int nvar;

	public ForceDensity(Mesh mesh, double weight) {
		this.mesh = mesh;
		nvar = mesh.nodesize - mesh.fixedsize;
		loads = new double[nvar][];
		for (int i = 0; i < loads.length; i++)
			loads[i] = new double[] { 0, 0, -weight };
	}

	public void solve() {
		int nodesize = mesh.nodesize;
		int membsize = mesh.membsize;
		all2N = new Integer[nodesize];
		all2F = new Integer[nodesize];
		int nc = 0;
		int fc = 0;
		for (int i = 0; i < nodesize; i++) {
			if (mesh.fixed(i)) {
				all2F[i] = fc;
				fc++;
			} else {
				all2N[i] = nc;
				nc++;
			}
		}

		XF = new double[mesh.fixedsize][];
		for (int i = 0; i < nodesize; i++) {
			if (mesh.fixed(i))
				XF[all2F[i]] = mesh.nodes[i];
		}

		// println(mesh.fixedsize+" fixed");
		double[][] CN = new double[membsize][nvar];
		double[][] CF = new double[membsize][mesh.fixedsize];
		for (int i = 0; i < membsize; i++) {
			int[] m = mesh.members[i];
			if (mesh.fixed(m[0]) && mesh.fixed(m[1]))
				throw new RuntimeException();

			if (mesh.fixed(m[0])) {
				CF[i][all2F[m[0]]] = -1; // F
				CN[i][all2N[m[1]]] = 1;
			} else if (mesh.fixed(m[1])) {
				CN[i][all2N[m[0]]] = -1;
				CF[i][all2F[m[1]]] = 1;// F
			} else {
				CN[i][all2N[m[0]]] = -1;
				CN[i][all2N[m[1]]] = 1;
			}
		}

		double[][] DN = M.mul(M.transpose(CN), CN);
		double[][] DF = M.mul(M.transpose(CN), CF);
		double[][] DFX = M.mul(DF, XF); // nvar * 3
		double[] b0 = new double[nvar];
		double[] b1 = new double[nvar];
		double[] b2 = new double[nvar];
		for (int i = 0; i < nvar; i++) {
			b0[i] = -loads[i][0] - DFX[i][0];
			b1[i] = -loads[i][1] - DFX[i][1];
			b2[i] = -loads[i][2] - DFX[i][2];
		}

		SparseDoubleMatrix2D has = SparseDoubleMatrix2D.Factory.zeros(nvar, nvar);
		for (int i = 0; i < nvar; i++)
			for (int j = 0; j < nvar; j++)
				has.setAsDouble(DN[i][j], i, j);

		SparseDoubleMatrix2D bm0 = SparseDoubleMatrix2D.Factory.zeros(nvar, 1);
		SparseDoubleMatrix2D bm1 = SparseDoubleMatrix2D.Factory.zeros(nvar, 1);
		SparseDoubleMatrix2D bm2 = SparseDoubleMatrix2D.Factory.zeros(nvar, 1);
		for (int i = 0; i < nvar; i++) {
			bm0.setAsDouble(b0[i], i, 0);
			bm1.setAsDouble(b1[i], i, 0);
			bm2.setAsDouble(b2[i], i, 0);
		}

		Matrix re0 = has.solveSPD(bm0); // quick
		Matrix re1 = has.solveSPD(bm1); // quick
		Matrix re2 = has.solveSPD(bm2); // quick
		int count = 0;
		for (int i = 0; i < nodesize; i++) {
			if (mesh.fixed(i))
				continue;
			double[] p = mesh.nodes[i];
			p[0] = re0.getAsDouble(count, 0);
			p[1] = re1.getAsDouble(count, 0);
			p[2] = re2.getAsDouble(count, 0);
			count++;
		}
	}

}
