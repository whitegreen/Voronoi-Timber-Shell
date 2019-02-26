package torsionfree;

import fabrication.Pavilion;
import funicular.Optimizer;
import funicular.Optimizer_Spring;
import funicular.M;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import peasy.PeasyCam;
import processing.core.PApplet;

public class ShowNormals extends PApplet {
	private Mesh_Agents_Voronoi mesh;
	private Pavilion pav;
	private float sc = 0.02f;

	public void setup() {
		size(1000, 800, P3D);
		new PeasyCam(this, 200);

		mesh = new Mesh_Agents_Voronoi();
		MiniDist md = new MiniDist(mesh);
		md.solve();
		Optimizer_Spring  opt = new Optimizer_Spring(mesh); // Spring Equilibrium
		opt.optimize();
		pav = new Pavilion(mesh);
		//pav.timberGeometry();
	}
		
	public void draw() {
		background(255);
		scale(1,-1,1);
		translate(-sc * 4000, -sc * 4000);
		stroke(255, 0, 0);
		line(0, 0, sc * 8000, 0);
		stroke(0, 255, 0);
		line(0, 0, 0, sc * 8000);
		stroke(0, 0, 255);
		line(0, 0, 0, 0, 0, sc * 8000);

//		line(0, sc * mesh.HEI, 0, sc * mesh.WID, sc * mesh.HEI, sc * mesh.ceiling_h);
//		line(0, sc * mesh.HEI, sc * mesh.ceiling_h, sc * mesh.WID, sc * mesh.HEI, 0);
//		
		stroke(0);
		noFill();
		pushMatrix();
		translate(sc*mesh.WID/2, sc*mesh.HEI/2,  sc * mesh.ceiling_h/2);
		box(sc*mesh.WID, sc*mesh.HEI, sc*mesh.ceiling_h);
		popMatrix();
		
		stroke(0);
		int membsize= mesh.membsize;
		int nodesize=mesh.nodesize;
		int[][] members = mesh.members;
		double[][] nodes = mesh.nodes;
		for (int i = 0; i < membsize; i++) {
			int m0 = members[i][0];
			int m1 = members[i][1];
			line(nodes[m0], nodes[m1]);
		}
		
		stroke(255, 0, 0);
		float len = 300; // mm
		double[][][] betdirs = pav.node_between_dirs;
		double[][] nors=pav.node_normals;
		for (int i = 0; i <nodesize; i++) {
			if(mesh.dead[i])
				continue;
			double[][] dirs = betdirs[i];
//			if (null != dirs) {
//				double[] node = nodes[i];
//				for (int j = 0; j < dirs.length; j++) {
//					double[] v = dirs[j];
//					line(node, M.add(node, M.scale(len, v)));
//				}
//			}
			double[] nor= M.scale(len, nors[i]);
			double[] node= nodes[i];
			if(mesh.fixed(i))
				stroke(255,0,0);
			else
				stroke(0,255,0);
			line( node, M.add(node, nor));
		}
	}

	private void line(double[] pa, double[] pb) {
		line((float) (sc * pa[0]), (float) (sc * pa[1]), (float) (sc * pa[2]), (float) (sc * pb[0]), (float) (sc * pb[1]), (float) (sc * pb[2]));
	}
	

}
