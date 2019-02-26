package funicular;

public class Optimizer_Spring extends Optimizer {
	double globalDensity = -0.006;
	private static final double unit = 300;  //unit = 0
	private static final double k = 0.1;

	public Optimizer_Spring(Mesh me) {
		super(me);
		iteration = 600; //***************-0.008:600        -0.004:800 

		int membsize = mesh.membsize;
		membDensity = new double[membsize];
		for (int i = 0; i < membsize; i++)
			membDensity[i] = globalDensity;
	}

	public void updateGeometry() {
		int nodesize = mesh.nodesize;
		double[][] nodes = mesh.nodes;
		for (int i = 0; i < nodesize; i++) {
			if (mesh.fixed(i))
				continue;
			nodes[i][2] -= nodeWeights[i]; // gravity
		}
		for (int i = 0; i < mesh.membsize; i++) {
			int[] member = mesh.members[i];
			int m0 = member[0];
			int m1 = member[1];
			double[] n0 = nodes[m0];
			double[] n1 = nodes[m1];
			double[] arrow = M.sub(n1, n0);
			double F = k * (M.mag(arrow) - unit);
			double[] movement = M.scaleTo(F, arrow);
			//double[] movement = M.scale(k, arrow);  // when unit=0
			if (!mesh.fixed(m0))
				M._add(n0, movement);
			if (!mesh.fixed(m1))
				M._sub(n1, movement);
		}
	}

}
