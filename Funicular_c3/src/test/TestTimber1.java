package test;

import fabrication.Pavilion;
import fabrication.Timber;
import funicular.M;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import funicular.Optimizer_Spring;
import peasy.PeasyCam;
import processing.core.PApplet;

public class TestTimber1 extends PApplet {
	private Mesh_Agents_Voronoi mesh;
	private Pavilion pav;
	private float sc = 0.5f;
	private int tid = 33;  
	private Timber tim;
	private final double toolR=4;
	private final double margin=8.0;
	
	public void setup() {
		size(1000, 800, P3D);
		new PeasyCam(this, 200);

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

		tim = pav.tims[tid];
		println("this timer id " + tid);
		tim.local();
	}

	public void draw() {
		background(255);
		scale(1, -1, 1);
		noFill();
		stroke(255, 0, 0);
		line(0, 0, sc * 100, 0);
		stroke(0, 255, 0);
		line(0, 0, 0, sc * 100);
		stroke(0, 0, 255);
		line(0, 0, 0, 0, 0, sc * 100);

		double le = 200;
		stroke(255, 0, 0);
		double[] arrow = M.scale(le, tim._fornormal);
		line(tim._p0, M.add(tim._p0, arrow));
		line(tim._forhole, M.add(tim._forhole, new double[] { 0, 0.5 * Pavilion.hei, 0 }));
		line(tim._forcuts[0], tim._forcuts[1]);
		line(tim._p0, tim._p0up);
		line(tim._p0, tim._p0dn);
		poly(tim._bps,true);

		stroke(0, 225, 0);
		arrow = M.scale(le, tim._backnormal);
		line(tim._p1, M.add(tim._p1, arrow));
		line(tim._backhole, M.add(tim._backhole, new double[] { 0, 0.5 * Pavilion.hei, 0 }));
		line(tim._backcuts[0], tim._backcuts[1]);
		line(tim._p1, tim._p1up);
		line(tim._p1, tim._p1dn);
		poly(tim._fps, true);
	}

	private void poly(double[][] ps, boolean close) {
		beginShape();
		for (double[] p : ps)
			vertex(p);
		if (close)
			endShape(CLOSE);
		else
			endShape();
	}

	private void vertex(double[] p) {
		vertex((float) (sc * p[0]), (float) (sc * p[1]), (float) (sc * p[2]));
	}

	private void line(double[] pa, double[] pb) {
		line((float) (sc * pa[0]), (float) (sc * pa[1]), (float) (sc * pa[2]), (float) (sc * pb[0]), (float) (sc * pb[1]), (float) (sc * pb[2]));
	}
	
	public void keyPressed() {
		if (RIGHT == keyCode) {
			int len = pav.tims.length;
			tid = (tid + 1) % len;
			tim = pav.tims[tid];
			tim.local();
			tim.toolPath(toolR,margin);
			println(tid);
		} else if (LEFT == keyCode) {
			int len = pav.tims.length;
			tid = (tid - 1 + len) % len;
			tim = pav.tims[tid];
			tim.local();
			tim.toolPath(toolR,margin);
			println(tid);
		}
	}

}
