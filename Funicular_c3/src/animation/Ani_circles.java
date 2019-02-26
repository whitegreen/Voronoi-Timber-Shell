package animation;

import processing.core.PApplet;

public class Ani_circles extends PApplet {
	private MeshAV mesh;
	private  float sc = 0.12f;
	
	public void setup() {
		size(1280, 720, P3D);
		
		mesh = new MeshAV();
		
	}

	public void draw() {
//		if(frameCount<mesh.agent_iteration)
//				mesh.updateCircles();
		background(0);
		smooth();
		scale(1, -1, 1);
//		float ts=   (float)frameCount/ tnum;
//		if(frameCount>tnum)
//			ts=1;
		float ts=0;
		translate(770,    (1-ts)*(-225)+ ts*(-570));
		rotateX(-ts*PI/2);
		rotateZ(-ts*PI/2);
	
		translate(-sc * 4000, -sc * 4000);
		stroke(255, 0, 0);
		line(0, 0, sc * 8000, 0);
		stroke(0, 255, 0);
		line(0, 0, 0, sc * 8000);
		
		noFill();
		stroke(255);
		strokeWeight(2);
		rect(0,0,sc*mesh.WID, sc*mesh.HEI);
		fill(0,0,255,100);
		for (int i = 0; i < mesh.cir_num; i++) {
			double[] cnt= mesh.cs[i];
			double radi= mesh.rs[i];
			ellipse(  (float)(sc*cnt[0]), (float)(sc*cnt[1]), (float)(2*sc*radi),  (float)(2*sc*radi));
		}
		if(frameCount<=mesh.agent_iteration){
			saveFrame("###.png");
		}
	}


	private void line(double[] pa, double[] pb) {
		line((float) (sc * pa[0]), (float) (sc * pa[1]), (float) (sc * pa[2]), (float) (sc * pb[0]), (float) (sc * pb[1]), (float) (sc * pb[2]));
	}
	

}
