package funicular;

import fabrication.Pavilion;
import funicular.M;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import peasy.PeasyCam;
import processing.core.PApplet;

public class TestOptMatrix extends PApplet {
	private Mesh_Agents_Voronoi mesh;
	private float sc = 0.02f;
	Optimizer_EquilibriumM6  opt ;
	
	public void setup() {
		size(1000, 800, P3D);
		new PeasyCam(this, 200);

		mesh = new Mesh_Agents_Voronoi();
		MiniDist md = new MiniDist(mesh);
		md.solve();
		opt = new Optimizer_EquilibriumM6(mesh, 100, -0.012); // Spring Equilibrium
	}

	public void draw() {
		opt.updateGeometry();
		background(255);
		scale(1, -1, 1);
		translate(-sc * 4000, -sc * 4000);
		stroke(255, 0, 0);
		line(0, 0, sc * 8000, 0);
		stroke(0, 255, 0);
		line(0, 0, 0, sc * 8000);
		stroke(0, 0, 255);
		line(0, 0, 0, 0, 0, sc * 8000);

		stroke(0);
		noFill();
		pushMatrix();
		translate(sc * mesh.WID / 2, sc * mesh.HEI / 2, sc * mesh.ceiling_h / 2);
		box(sc * mesh.WID, sc * mesh.HEI, sc * mesh.ceiling_h);
		popMatrix();

		stroke(0);
		int membsize = mesh.membsize;
		int[][] members = mesh.members;
		double[][] nodes = mesh.nodes;
		for (int i = 0; i < membsize; i++) {
			int m0 = members[i][0];
			int m1 = members[i][1];
			line(nodes[m0], nodes[m1]);
		}

	}

	private void line(double[] pa, double[] pb) {
		line((float) (sc * pa[0]), (float) (sc * pa[1]), (float) (sc * pa[2]), (float) (sc * pb[0]), (float) (sc * pb[1]), (float) (sc * pb[2]));
	}

}
