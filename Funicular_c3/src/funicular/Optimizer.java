package funicular;


public abstract class Optimizer {
	public Mesh mesh;
	public double[] nodeWeights; // determined by membWeights
	public double[] membDensity;
	public Integer iteration;

	public Optimizer(Mesh me) {
		mesh = me;
	}

	public void optimize() {
		for (int i = 0; i < iteration; i++) { // 400 for Spring, 2400 for Equilibrium
			updateWeights();
			updateGeometry();
		}
	}

	public void updateWeights() {
		nodeWeights = new double[mesh.nodesize];
		for (int i = 0; i < mesh.membsize; i++) {
			int[] member = mesh.members[i];
			int m0 = member[0];
			int m1 = member[1];
			double[] n0 = mesh.nodes[m0];
			double[] n1 = mesh.nodes[m1];
			double wei = membDensity[i] * M.dist(n0, n1);
			nodeWeights[m0] += 0.5 * wei;
			nodeWeights[m1] += 0.5 * wei;
		}
	}

	public abstract void updateGeometry();

}
