package animation;

import fabrication.Pavilion;
import fabrication.Timber;
import funicular.Optimizer_Spring;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import processing.core.PApplet;

public class Ani_Optimize extends PApplet {
	private Mesh_Agents_Voronoi mesh;
	private Pavilion pav;
	private  float sc = 0.12f;
	private Optimizer_Spring opt ;
	
	public void setup() {
		size(1280, 720, P3D);

		 mesh = new Mesh_Agents_Voronoi();
		MiniDist md = new MiniDist(mesh);
		md.solve();
		 opt = new Optimizer_Spring(mesh); // Spring Equilibrium
		//opt.optimize();

	}

	public void draw() {
		if (frameCount < opt.iteration) {
			opt.updateWeights();
			opt.updateGeometry();
		}
		smooth();
		background(0);
		strokeWeight(1);
		scale(1, -1, 1);
		float ts = 1;
		translate(770, (1 - ts) * (-225) + ts * (-570));
		rotateX(-ts * PI / 2);
		rotateZ(-ts * PI / 2);

		translate(-sc * 4000, -sc * 4000);
		stroke(255, 0, 0);
		line(0, 0, sc * 8000, 0);
		stroke(0, 255, 0);
		line(0, 0, 0, sc * 8000);
		stroke(0, 0, 255);
		line(0, 0, 0, 0, 0, sc * 8000);
		line(0, sc * mesh.HEI, 0, sc * mesh.WID, sc * mesh.HEI, sc * mesh.ceiling_h);
		line(0, sc * mesh.HEI, sc * mesh.ceiling_h, sc * mesh.WID, sc * mesh.HEI, 0);

		stroke(255);
		noFill();
		pushMatrix();
		translate(sc * mesh.WID / 2, sc * mesh.HEI / 2, sc * mesh.ceiling_h / 2);
		box(sc * mesh.WID, sc * mesh.HEI, sc * mesh.ceiling_h);
		popMatrix();

		strokeWeight(2);
		int membsize = mesh.membsize;
		int[][] members = mesh.members;
		double[][] nodes = mesh.nodes;
		for (int i = 0; i < membsize; i++) {
			int m0 = members[i][0];
			int m1 = members[i][1];
			line(nodes[m0], nodes[m1]);
		}

		if(frameCount<  opt.iteration){
			if(0==frameCount%2)
			saveFrame("###.png");
		}
	}


	private  void poly(double[][] ps) {
		beginShape();
		for (double[] p : ps)
			vertex(p);
		endShape(CLOSE);
	}
	private void vertex(double[] p){
		vertex((float) (sc * p[0]), (float) (sc * p[1]), (float) (sc * p[2]));
	}

	private void line(double[] pa, double[] pb) {
		line((float) (sc * pa[0]), (float) (sc * pa[1]), (float) (sc * pa[2]), (float) (sc * pb[0]), (float) (sc * pb[1]), (float) (sc * pb[2]));
	}
	

}
