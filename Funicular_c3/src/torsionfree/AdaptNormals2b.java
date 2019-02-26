package torsionfree;

import java.text.DecimalFormat;

import fabrication.Pavilion;
import funicular.Optimizer_Spring;
import funicular.M;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import peasy.PeasyCam;
import processing.core.PApplet;

public class AdaptNormals2b extends PApplet {
	private final DecimalFormat df = new DecimalFormat("###.###");
	private Mesh_Agents_Voronoi mesh;
	private Pavilion pav;
	private float sc = 0.02f;

	private double[][] candis;
	private final double evo_learnrate = 0.1;
	private final double remain_learnrate = 0.1;
	private final double diff_normal_cos=Math.cos( Math.PI/12 );

	public void setup() {
		size(1000, 800, P3D);
		new PeasyCam(this, 200);

		mesh = new Mesh_Agents_Voronoi();
		MiniDist md = new MiniDist(mesh);
		md.solve();
		Optimizer_Spring  opt = new Optimizer_Spring(mesh); // Spring Equilibrium
		opt.optimize();
		pav = new Pavilion(mesh);
		
		int nodesize=mesh.nodesize;
		double[][] nors=pav.node_normals;
		candis=new double[mesh.nodesize][];
		for (int i = 0; i <nodesize; i++) {
			if(mesh.dead[i] ||  mesh.fixed(i) )
				continue;
			candis[i]= nors[i].clone();
		}
		
		for (int i = 0; i < 200; i++)
			evo();
		double max_diff_ang = 0;
		for (int i = 0; i <nodesize; i++) {
			if(mesh.dead[i] ||  mesh.fixed(i) )
				continue;
			double cos = M.dotProduct(candis[i], nors[i]);
			double ang = Math.acos(cos);
			if (ang > max_diff_ang)
				max_diff_ang = ang;
			nors[i] = candis[i];
		}
		println("max dif deg " + df.format(max_diff_ang * 180 / PI));
	}
	
	private void evo() {
		int membsize = mesh.membsize;
		int nodesize = mesh.nodesize;
		int[][] members = mesh.members;
		double[][] nodes = mesh.nodes;
		double[][] nors=pav.node_normals;

		double[][] deltas = new double[nodesize][3];
		double err = 0;
		double max=0;
		for (int i = 0; i < membsize; i++) {
			int m0 = members[i][0];
			int m1 = members[i][1];
			if (mesh.dead[m0] || mesh.fixed(m0) || mesh.dead[m1] || mesh.fixed(m1))
				continue;

			double[] d = M.normalize(M.sub(nodes[m1], nodes[m0]));
			double[] c0 = M.normalize(M.crossProduct(d, candis[m0]));
			double[] c1 = M.normalize(M.crossProduct(d, candis[m1]));
			double[] cmean = M.normalize(M.add(c0, c1));
			double[] dir = M.crossProduct(d, cmean);
			double cos = M.dotProduct(c0, c1);
			if (cos < 0)
				throw new RuntimeException();
			if (M.dotProduct(c0, dir) * M.dotProduct(c1, dir) > 0)
				throw new RuntimeException();

			double ang = Math.acos(cos);
			err += ang;
			if (ang > max)
				max = ang;
			int sign = M.dotProduct(c0, dir) < 0 ? 1 : -1;
			double[] nv0 = M.rotate_Rodriguez(candis[m0], sign * 0.5 * ang, d);
			double[] nv1 = M.rotate_Rodriguez(candis[m1], -sign * 0.5 * ang, d);

			double[] del0 = M.between(evo_learnrate, candis[m0], M.normalize(nv0));
			double[] del1 = M.between(evo_learnrate, candis[m1], M.normalize(nv1));
			M._add(deltas[m0], del0);
			M._add(deltas[m1], del1);
		}
		println("sum err " + df.format(err) + " max deg " + df.format(max * 180 / PI));
		for (int i = 0; i < nodesize; i++) {
			if (mesh.dead[i] || mesh.fixed(i))
				continue;

			double[] result = M.normalize(M.add(candis[i], deltas[i]));
			if (M.dotProduct(result, nors[i]) > diff_normal_cos) { // good range
				M.assign(candis[i], result);
			} else {
				double[] bet = M.between(0.2, result, nors[i]);
				M.assign(candis[i], M.normalize(bet));
			}
		}
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
//			if(eid!=i)
//				continue;
			int m0 = members[i][0];
			int m1 = members[i][1];
			line(nodes[m0], nodes[m1]);
		}
		
		stroke(255, 0, 0);
		float len = 300; // mm
		double[][] nors=pav.node_normals;
		for (int i = 0; i <nodesize; i++) {
			if(mesh.dead[i])
				continue;
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
