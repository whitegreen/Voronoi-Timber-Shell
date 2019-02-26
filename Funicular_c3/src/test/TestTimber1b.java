package test;

import java.text.DecimalFormat;
import java.util.Random;
import fabrication.Pavilion;
import fabrication.Timber;
import funicular.M;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import funicular.Optimizer_Spring;
import peasy.PeasyCam;
import processing.core.PApplet;

public class TestTimber1b extends PApplet {
	private static final DecimalFormat df = new DecimalFormat("#.#");
	private Mesh_Agents_Voronoi mesh;
	private Pavilion pav;
	private float sc = 0.5f;
	private int tid =63; //*************
	private Timber tim;
	private final double toolR=4;
	private final double margin=8.0;
	private double le = 200;
	
	public void setup() {
		size(1100, 800, P3D);
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
		tim.toolPath(toolR,margin);
		println("timber length "+ df.format(M.dist(tim._p0, tim._p1) ) );
		
		/*int[][] members = mesh.members;
		double[][] nodes= mesh.nodes;
		for (int i = 0; i < members.length; i++) {
			int[] m = members[i];
			Timber tim= pav.tims[i];
			tim.local();
			//println(i + ": " + m[0] + " - " + m[1]+"  "+ df.format(M.dist(nodes[m[0]], nodes[m[1]])));
			double dist = M.dist(tim._p0, tim._p1);
			double base_shift = 0.5 * dist - 233;
			println(i+": " + df.format(base_shift)+" mm");
		}*/
//		for (double[] p : tim._fps) {
//			println(df.format(p[0]) + "," + df.format(p[1]) + "," + df.format(p[2]));
//		}
//		for (int i=0;i< 6;i++) {
//			double[] pa = tim._fps[i];
//			double[] pb = tim._fps[(i+1)%6];
//			println(i+": "+ df.format(M.dist(pa, pb)));
//		}
		
	}

	public void draw() {
		background(255);
		noFill();
		scale(1, -1, 1);
		stroke(255, 0, 0);
		line(0, 0, sc * 100, 0);
		stroke(0, 255, 0);
		line(0, 0, 0, sc * 100);
		stroke(0, 0, 255);
		line(0, 0, 0, 0, 0, sc * 100);
		
		draw_for();
		draw_back();
	}
	
	private void draw_for(){  //red
		stroke(200, 0, 0);
		double[] arrow = M.scale(le, tim._fornormal);
		line(tim._p0, M.add(tim._p0, arrow));
		line(tim._forhole, M.add(tim._forhole, new double[] { 0, 0.5 * Pavilion.hei, 0 }));
		line(tim._forhole, tim._p0);
		line(tim._forcuts[0], tim._forcuts[1]);
		stroke(0);
		line(tim._forcutpb, tim._forcutpb_half);//******************************
line(tim._forcutpa, tim._forcutpa_half);
		
		stroke(200, 0, 0);
		line(tim._p0, tim._p0up);
		line(tim._p0, tim._p0dn);
		noFill();
		poly(tim._forts, false);
		stroke(150,0,0);
		poly(tim._forts_hale, false);   //left dark
		stroke(255,0,0);
		poly(tim._forts_hari, false);  //right bright
		stroke(200, 0, 0);
		line(M.add(tim._forts[0], new double[] { 0, 0, 30 }), tim._forts[0]); // tool path
		poly(tim._bps, true);
	}
	
	private void draw_back(){  //green
		stroke(0, 200, 0);
		double[] arrow = M.scale(le, tim._backnormal);
		line(tim._p1, M.add(tim._p1, arrow));
		line(tim._backhole, M.add(tim._backhole, new double[] { 0, 0.5 * Pavilion.hei, 0 }));
		line(tim._backhole, tim._p1);
		line(tim._backcuts[0], tim._backcuts[1]);
		stroke(0);
		line(tim._backcutpb, tim._backcutpb_half);// ******************************
		line(tim._backcutpa, tim._backcutpa_half);
		
		stroke(0, 200, 0);
		line(tim._p1, tim._p1up);
		line(tim._p1, tim._p1dn);
		noFill();
		poly(tim._backts, false);
		stroke(0, 150, 0);
		poly(tim._backts_hale, false);  //left dark
		stroke(0, 255, 0);
		poly(tim._backts_hari, false);  //right bright
		stroke(0, 200, 0);
		line(M.add(tim._backts[0], new double[] { 0, 0, 30 }), tim._backts[0]); // tool path
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
