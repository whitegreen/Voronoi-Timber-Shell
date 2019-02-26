package animation;

import processing.core.PApplet;

public class Ani_voronoi extends PApplet {
	private MeshAV mesh;
	private  float sc = 0.12f;
	private int tnum=72;

	public void setup() {
		size(1280, 720, P3D);
		
		mesh = new MeshAV();
		mesh.finalize();
	}

	public void draw() {
		background(0);
		smooth();
		scale(1, -1, 1);
		float ts=   (float)frameCount/ tnum;
		if(frameCount>tnum)
			ts=1;
//		float ts=0;
		translate(770,    (1-ts)*(-225)+ ts*(-570));
		rotateX(-ts*PI/2);
		rotateZ(-ts*PI/2);
	
		translate(-sc * 4000, -sc * 4000);
		stroke(255, 0, 0);
		line(0, 0, sc * 8000, 0);
		stroke(0, 255, 0);
		line(0, 0, 0, sc * 8000);
	
		strokeWeight(2);
		stroke(255);
		int membsize= mesh.membsize;
		int[][] members = mesh.members;
		double[][] nodes = mesh.nodes;
		for (int i = 0; i < membsize; i++) {
			int m0 = members[i][0];
			int m1 = members[i][1];
			line(nodes[m0], nodes[m1]);
		}
//		fill(0,0,255,100);
//		for (int i = 0; i < mesh.cir_num; i++) {
//			double[] cnt= mesh.cs[i];
//			double radi= mesh.rs[i];
//			ellipse(  (float)(sc*cnt[0]), (float)(sc*cnt[1]), (float)(2*sc*radi),  (float)(2*sc*radi));
//		}
		if(frameCount<=tnum){
			//saveFrame("###.png");
		}
	}
	private void vertex2D(double[] p){
		vertex((float) (sc * p[0]), (float) (sc * p[1]), 0);
	}

	private void line(double[] pa, double[] pb) {
		line((float) (sc * pa[0]), (float) (sc * pa[1]),  (float) (sc * pb[0]), (float) (sc * pb[1]));
	}
	

}
