package animation;

import fabrication.Pavilion;
import fabrication.Timber;
import funicular.Optimizer_Spring;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import peasy.PeasyCam;
import processing.core.PApplet;

public class Shot_Optimize extends PApplet {
	private Mesh_Agents_Voronoi mesh;
	private  float sc = 0.12f;
	private Optimizer_Spring opt ;
	boolean flag=true;
	
	public void setup() {
		size(1280, 720, P3D);
    //new PeasyCam(this,200);
		 mesh = new Mesh_Agents_Voronoi();
//		 for(double[] p: mesh.nodes){
//			 p[2]= mesh.ceiling_h;
//		 }
		 
//			ForceDensity fd=new ForceDensity(mesh,  180);
//			fd.solve();
		 
		MiniDist md = new MiniDist(mesh);
		md.solve();
		
		
		
		
//		 opt = new Optimizer_Spring(mesh); // Spring Equilibrium
//		opt.optimize();

	}

	public void draw() {
		smooth();
		background(255);
		strokeWeight(2);
		scale(1, -1, 1);
		float ts = 1;
		translate(770, (1 - ts) * (-225) + ts * (-570));
		rotateX(-ts * PI / 2);
		rotateZ(-ts * PI / 2);

		translate(-sc * 4000, -sc * 4000);

		strokeWeight(2);
		stroke(180);
		noFill();
		pushMatrix();
		translate(sc * mesh.WID / 2, sc * mesh.HEI / 2, sc * mesh.ceiling_h / 2);
		box(sc * mesh.WID, sc * mesh.HEI, sc * mesh.ceiling_h);
		popMatrix();

		strokeWeight(3);
		stroke(0);
		int membsize = mesh.membsize;
		int nodesize=mesh.nodesize;
		int[][] members = mesh.members;
		double[][] nodes = mesh.nodes;
		for (int i = 0; i < membsize; i++) {
			int m0 = members[i][0];
			int m1 = members[i][1];
			line(nodes[m0], nodes[m1]);
		}
		
		noStroke();
		fill(255,0,0);
		for (int i = 0; i < nodesize; i++) {
			if (mesh.fixed(i)) {
				double[] p = mesh.nodes[i];
				pushMatrix();
				translate((float) (sc * p[0]), (float) (sc * p[1]), (float) (sc * p[2]));
				box(5);
				popMatrix();
			}
		}

		if (flag) {
			//save("/Users/huahao/Desktop/fix.png");
			flag = false;
		}
	}


	private void vertex(double[] p){
		vertex((float) (sc * p[0]), (float) (sc * p[1]), (float) (sc * p[2]));
	}

	private void line(double[] pa, double[] pb) {
		line((float) (sc * pa[0]), (float) (sc * pa[1]), (float) (sc * pa[2]), (float) (sc * pb[0]), (float) (sc * pb[1]), (float) (sc * pb[2]));
	}
	

}
