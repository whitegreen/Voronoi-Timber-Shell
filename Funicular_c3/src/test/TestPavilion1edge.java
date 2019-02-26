package test;

import fabrication.Pavilion;
import fabrication.Sicily;
import fabrication.Timber;
import funicular.M;
import funicular.Optimizer;
import funicular.Optimizer_Spring;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import peasy.PeasyCam;
import processing.core.PApplet;

public class TestPavilion1edge extends PApplet {
	private Mesh_Agents_Voronoi mesh;
	private Pavilion pav;
	private  float sc = 0.03f;

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
		
		//pav.intersectHexa(); //create Sicilys
	}

	public void draw() {
		background(255);
		scale(1, -1, 1);
	
		translate(-sc * 4000, -sc * 4000);
		stroke(255, 0, 0);
		line(0, 0, sc * 8000, 0);
		stroke(0, 255, 0);
		line(0, 0, 0, sc * 8000);
		stroke(0, 0, 255);
		line(0, 0, 0, 0, 0, sc * 8000);
		line(0, sc * mesh.HEI, 0, sc * mesh.WID, sc * mesh.HEI, sc * mesh.ceiling_h);
		line(0, sc * mesh.HEI, sc * mesh.ceiling_h, sc * mesh.WID, sc * mesh.HEI, 0);
//		
//		fill(0,255,255);
//		beginShape(QUAD);
//		vertex(0, 0, sc * mesh.ceiling_h);
//		vertex(0, sc*Pavilion.fix_hexa_depth, sc * mesh.ceiling_h);
//		vertex(sc * mesh.WID, sc*Pavilion.fix_hexa_depth, sc * mesh.ceiling_h);
//		vertex(sc * mesh.WID, 0, sc * mesh.ceiling_h);
//		endShape();
//		beginShape(QUAD);
//		vertex(0, 0, sc * mesh.ceiling_h);
//		vertex(sc*Pavilion.fix_hexa_depth, 0, sc * mesh.ceiling_h);
//		vertex(sc*Pavilion.fix_hexa_depth, sc * mesh.HEI, sc * mesh.ceiling_h);
//		vertex(0, sc * mesh.HEI, sc * mesh.ceiling_h);
//		endShape();
//		beginShape(QUAD);
//		vertex(0, sc * mesh.HEI, sc * mesh.ceiling_h);
//		vertex(0, sc * mesh.HEI-sc*Pavilion.fix_hexa_depth, sc * mesh.ceiling_h);
//		vertex(sc * mesh.WID, sc * mesh.HEI-sc*Pavilion.fix_hexa_depth, sc * mesh.ceiling_h);
//		vertex(sc * mesh.WID, sc * mesh.HEI, sc * mesh.ceiling_h);
//		endShape();
//		beginShape(QUAD);
//		vertex(sc * mesh.WID, 0, sc * mesh.ceiling_h);
//		vertex(sc * mesh.WID-sc*Pavilion.fix_hexa_depth, 0, sc * mesh.ceiling_h);
//		vertex(sc * mesh.WID-sc*Pavilion.fix_hexa_depth, sc * mesh.HEI, sc * mesh.ceiling_h);
//		vertex(sc * mesh.WID, sc * mesh.HEI, sc * mesh.ceiling_h);
//		endShape();
		
		fill(190);
		pushMatrix();
		translate(sc*2300, sc*900,  sc * 1730 * 0.5f);
		box(sc * 200, sc * 200, sc * 1730);  //person
		popMatrix();
		
		stroke(0);
		noFill();
		pushMatrix();
		translate(sc*mesh.WID/2, sc*mesh.HEI/2,  sc * mesh.ceiling_h/2);
		box(sc*mesh.WID, sc*mesh.HEI, sc*mesh.ceiling_h);
		popMatrix();

		stroke(0);
		double[][] nodes=mesh.nodes;
		int membsize= mesh.membsize;
		int nodesize=mesh.nodesize;
		double[][] nors=pav.node_normals;
		
		fill(200);
		for (int i = 0; i <membsize; i++) {
//			if(60!=i)
//				continue;
			Timber tim = pav.tims[i];
			// fill(cols[tim.ID], 255, 255);
			double s=(pav.mem_lens[i] - pav.minlen) / (pav.maxlen - pav.minlen);
			stroke(0);
			line(nodes[tim.m0], nodes[tim.m1]);
			if (null != tim.fps && null != tim.bps)
				draw(tim, sc);
		}
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
		fill(0, 225, 0);
		int len = 300;
		if (mesh.fixed(tim.m0)) {
			stroke(255,0,0);
			line(tim.forhole, M.add(tim.forhole, new double[] { 0, 0, len }));
			line(tim.forhole, M.add(tim.forhole, new double[] { 0, 0, -len }));
//			 beginShape(QUAD);
//			 vertex(M.add( tim.forhole, new double[]{-50,-50,0}));
//			 vertex(M.add( tim.forhole, new double[]{50,-50,0}));
//			 vertex(M.add( tim.forhole, new double[]{50,50,0}));
//			 vertex(M.add( tim.forhole, new double[]{-50,50,0}));
//			 endShape();
			
			if (mesh.fixed[tim.m0] > 2) {
				line(tim.forcuts[0], M.add(tim.forcuts[0], M.scale(len, tim.inward_onXY)));
				line(tim.forcuts[1], M.add(tim.forcuts[1], M.scale(len, tim.inward_onXY)));
			}
		}
		if (mesh.fixed(tim.m1)) {
			stroke(255,0,0);
			line(tim.backhole, M.add(tim.backhole, new double[] { 0, 0, len }));
			line(tim.backhole, M.add(tim.backhole, new double[] { 0, 0, -len }));
//			 beginShape(QUAD);
//			 vertex(M.add( tim.backhole, new double[]{-50,-50,0}));
//			 vertex(M.add( tim.backhole, new double[]{50,-50,0}));
//			 vertex(M.add( tim.backhole, new double[]{50,50,0}));
//			 vertex(M.add( tim.backhole, new double[]{-50,50,0}));
//			 endShape();
			if (mesh.fixed[tim.m1] > 2) {
				line(tim.backcuts[0], M.add(tim.backcuts[0], M.scale(len, tim.inward_onXY)));
				line(tim.backcuts[1], M.add(tim.backcuts[1], M.scale(len, tim.inward_onXY)));
			}
		}
		stroke(0, 0, 255);
		if (tim.uphole != null) {
			line(tim.uphole, tim.dnhole);
		}
		stroke(255,0,255);
		line(tim.forcuts[0], tim.forcuts[1]);
		line(tim.backcuts[0], tim.backcuts[1]);
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
