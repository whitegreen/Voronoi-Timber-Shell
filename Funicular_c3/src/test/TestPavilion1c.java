package test;

import java.util.ArrayList;

import fabrication.Pavilion;
import fabrication.Sicily;
import fabrication.Timber;
import funicular.Optimizer;
import funicular.Optimizer_Spring;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import peasy.PeasyCam;
import processing.core.PApplet;

public class TestPavilion1c extends PApplet {
	private Mesh_Agents_Voronoi mesh;
	private Pavilion pav;
	private float[] cols;

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
		cols = new float[mesh.membsize];
		for (int i = 0; i < mesh.membsize; i++)
			cols[i] = random(0, 255);
	}

	public void draw() {
		background(255);
		scale(1,-1,1);
		float sc = 0.02f;
		translate(-sc * 4000, -sc * 4000);
		stroke(255, 0, 0);
		line(0, 0, sc * 8000, 0);
		stroke(0, 255, 0);
		line(0, 0, 0, sc * 8000);
		stroke(0, 0, 255);
		line(0, 0, 0, 0, 0, sc * 8000);
		line(0, sc * mesh.HEI, 0, sc * mesh.WID, sc * mesh.HEI, sc * mesh.ceiling_h);
		line(0, sc * mesh.HEI, sc * mesh.ceiling_h, sc * mesh.WID, sc * mesh.HEI, 0);
		
		stroke(0);
		noFill();
		pushMatrix();
		translate(sc*mesh.WID/2, sc*mesh.HEI/2,  sc * mesh.ceiling_h/2);
		box(sc*mesh.WID, sc*mesh.HEI, sc*mesh.ceiling_h);
		popMatrix();
		
        stroke(0);
		fill(200);
		//ArrayList<Sicily> sicis = pav.sicis;
		int count=0;
		for (Sicily sici:  pav.sicis) {
//			if (mesh.dead[i]||mesh.fixed(i))
//				continue;
//			Sicily sici =sicis.get(i);// sicis[i];
			 double[][] ps =sici.ps3D();
//			double[][] ps = pav.nodeHexa[i];
			beginShape();
			for (double[] p : ps)
				vertex(p, sc);
			endShape(CLOSE);
			count++;
		}
		//println(count+" ^");
		
		for (int i = 0; i < mesh.membsize; i++) {
			Timber tim = pav.tims[i];
			double[][] ln = tim.forcuts;
			stroke(255, 0, 0);
			if (null != ln[0]  && null != ln[1]  )
				line(ln[0], ln[1], sc);

			ln = tim.backcuts;
			stroke(0, 255, 0);
			if (null != ln[0] && null != ln[1])
				line(ln[0], ln[1], sc);
		}
	}

	private void vertex(double[] p, float sc ){
		vertex((float) (sc * p[0]), (float) (sc * p[1]), (float) (sc * p[2]));
	}

	private void line(double[] pa, double[] pb, float sc) {
		line((float) (sc * pa[0]), (float) (sc * pa[1]), (float) (sc * pa[2]), (float) (sc * pb[0]), (float) (sc * pb[1]), (float) (sc * pb[2]));
	}
	

}
