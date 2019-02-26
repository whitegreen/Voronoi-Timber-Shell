package test;

import java.text.DecimalFormat;

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

public class TestPavilion1d extends PApplet {
	private final DecimalFormat df = new DecimalFormat("###.#");

	private Mesh_Agents_Voronoi mesh;
	private Pavilion pav;
	private  float sc = 0.145f;

	public void setup() {
		size(1100, 860, P3D);
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
		
		// pav.intersectHexa(); //create Sicilys

		/*for (int i = 0; i < mesh.membsize; i++) {
			Timber tim = pav.tims[i];
			tim.local();
			
			double dist = M.dist(tim._p0, tim._p1);
			double base_shift = 0.5 * dist - 233;
//			println("m"+i + ": " + tim.m0 +"^"+ mesh.fixed[tim.m0]+ "  -  " + tim.m1+"^"+ mesh.fixed[tim.m1]+ "     "+df.format(dist)+",     "+df.format(base_shift));
			String f="";
			String b="";
			if(mesh.fixed[tim.m0]>0)
				f+="$"+mesh.fixed[tim.m0];
			if(mesh.fixed[tim.m1]>0)
				b+="$"+mesh.fixed[tim.m1];
			println(i + ": " + tim.m0 +f+ "  -  " + tim.m1+b);
		}*/
		
		for (int i = 0; i < mesh.nodesize; i++) {
			if (mesh.dead[i]) {
				println("dead " + i);
				continue;
			}
//			int[] edge = mesh.nodeEdges[i];
//			print("n" + i + " ");
//			for (int eid : edge)
//				print(eid + "-");
//			if (mesh.fixed(i))
//				println("   type " + mesh.fixed[i]);
//			else
//				println();
		}
		
		
//		Timber tim = pav.tims[15];
//		double[] v0= mesh.nodes[tim.m0];
//		println( v0[2] );
//		double[] v1= mesh.nodes[tim.m1];
//		println( v1[2] );
	}

	public void draw() {
		background(255);
//		ortho();
		scale(1, -1, 1);
//		translate(1200,-1020);
//		rotateX(PI);
	
		translate(-sc * 4000, -sc * 4000);
		colorMode(RGB);
		stroke(255, 0, 0);
		line(0, 0, sc * 8000, 0);
		stroke(0, 255, 0);
		line(0, 0, 0, sc * 8000);
		stroke(0, 0, 255);
		line(0, 0, 0, 0, 0, sc * 8000);
		line(0, sc * mesh.HEI, 0, sc * mesh.WID, sc * mesh.HEI, sc * mesh.ceiling_h);
		line(0, sc * mesh.HEI, sc * mesh.ceiling_h, sc * mesh.WID, sc * mesh.HEI, 0);
		
//		fill(190);
//		pushMatrix();
//		translate(sc*2300, sc*900,  sc * 1730 * 0.5f);
//		box(sc * 200, sc * 200, sc * 1730);  //person
//		popMatrix();
//		
//		stroke(0);
//		noFill();
//		pushMatrix();
//		translate(sc*mesh.WID/2, sc*mesh.HEI/2,  sc * mesh.ceiling_h/2);
//		box(sc*mesh.WID, sc*mesh.HEI, sc*mesh.ceiling_h);
//		popMatrix();

		stroke(0);
		//noStroke();
		colorMode(HSB);
		double[][] nodes=mesh.nodes;
		int membsize= mesh.membsize;
		int nodesize=mesh.nodesize;
		double[][] nors=pav.node_normals;
		
		fill(200);
		for (int i = 0; i <membsize; i++) {
//			if(63!=i)
//				continue;
			Timber tim = pav.tims[i];
			//double dist = M.dist(tim._p0, tim._p1);
			// fill(cols[tim.ID], 255, 255);
			double s=(pav.mem_lens[i] - pav.minlen) / (pav.maxlen - pav.minlen);
			fill((float) (230 * s), 255, 255);
//			if (pav.minlen_id== tim.ID)
//				fill(0);
			line(nodes[tim.m0], nodes[tim.m1]);
			if (null != tim.fps && null != tim.bps)
				draw(tim, sc);
		}
		
		fill(255,0,0);
		//Sicily[] sicis = pav.sicis;
		/*for (int i = 0; i <nodesize; i++) {
			if (mesh.fixed(i))
				continue;
			//Sicily sici = sicis[i];
			// double[][] ps =sici.ps3D();
			poly(pav.nodeHexa[i]);

//			double[] nor = M.scale(280, nors[i]);
//				stroke(255, 0, 0);
//			line(node, M.add(nodes[i], nor));
		}*/
	}

	private void draw(Timber tim, float sc) {
		double[][] fps = tim.fps;
		double[][] bps = tim.bps;
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
		fill(0);
		double[] p = pav.mem_cnts[tim.ID];
		
		pushMatrix();
		translate((float) (sc * p[0]), (float) (sc * p[1]), (float) (sc * (p[2]-100)));
		
		pushMatrix();
		scale(1.5f);
		text(tim.ID,0,0,0);  //-100
		popMatrix();
		
		popMatrix();
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
