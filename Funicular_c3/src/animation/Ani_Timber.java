package animation;

import fabrication.Pavilion;
import fabrication.Sicily;
import fabrication.Timber;
import funicular.Optimizer_Spring;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import processing.core.PApplet;

public class Ani_Timber extends PApplet {
	private Mesh_Agents_Voronoi mesh;
	private Pavilion pav;
	private  float sc = 0.12f;

	public void setup() {
		size(1280, 720, P3D);

		 mesh = new Mesh_Agents_Voronoi();
		MiniDist md = new MiniDist(mesh);
		md.solve();
		Optimizer_Spring opt = new Optimizer_Spring(mesh); // Spring Equilibrium
		opt.optimize();
		pav = new Pavilion(mesh);
		pav.intersectHexa();
		pav.timberGeometry();
		pav.reportMesh();
		pav.postReport();
	}

	public void draw() {
		background(0);
		smooth();
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
		//strokeWeight(2);
		noFill();
		pushMatrix();
		translate(sc*mesh.WID/2, sc*mesh.HEI/2,  sc * mesh.ceiling_h/2);
		box(sc*mesh.WID, sc*mesh.HEI, sc*mesh.ceiling_h);
		popMatrix();

		double[][] nodes=mesh.nodes;
		int membsize= mesh.membsize;
		
		fill(200);
		for (int i = 0; i <membsize; i++) {
			Timber tim = pav.tims[i];
			double s=(pav.mem_lens[i] - pav.minlen) / (pav.maxlen - pav.minlen);
			line(nodes[tim.m0], nodes[tim.m1]);
			if (null != tim.fps && null != tim.bps)
				draw(tim, sc);
		}
		
		fill(255,0,0);
		noStroke();
		for (Sicily sici:  pav.sicis) {
			 double[][] ps =sici.ps3D();
			beginShape();
			for (double[] p : ps)
				vertex(p);
			endShape(CLOSE);
		}
		
		save("final.png");
	}

	private void draw(Timber tim, float sc) {
		double[][] fps = tim.fps;
		double[][] bps = tim.bps;
		fill(220,150);
		poly(fps);
		poly(bps);
		for (int i = 0; i < fps.length; i++) {
			beginShape();
			int j = (i + 1) % fps.length;
			vertex(fps[i]);
			vertex(fps[j]);
			vertex(bps[j]);
			vertex(bps[i]);
			endShape(CLOSE);
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
