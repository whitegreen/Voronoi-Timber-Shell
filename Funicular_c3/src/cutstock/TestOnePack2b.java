package cutstock;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import fabrication.Pavilion;
import funicular.M;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import funicular.Optimizer_Spring;
import processing.core.PApplet;

public class TestOnePack2b extends PApplet {
	private static final DecimalFormat df = new DecimalFormat("#.##");
	final float stockLen = 4000;
	Random ran = new Random();//(-1006);
	ArrayList<int[]> result = new ArrayList<int[]>();
	int margin= 25;

	public void setup() {
		Mesh_Agents_Voronoi mesh = new Mesh_Agents_Voronoi();
		MiniDist md = new MiniDist(mesh);
		md.solve();
		Optimizer_Spring opt = new Optimizer_Spring(mesh); // Spring Equilibrium
		opt.optimize();
		Pavilion pav = new Pavilion(mesh);
		pav.intersectHexa();
		pav.timberGeometry();
		pav.reportMesh();
		pav.postReport();

		double[] cls = new double[mesh.membsize]; // 460 pieces
		int[][] members = mesh.members;
		double[][] nodes = mesh.nodes;
		for (int i = 0; i < cls.length; i++) {
			int[] m = members[i];
			cls[i] = margin + M.dist(nodes[m[0]], nodes[m[1]]);
		}
		cutStock(cls);

		for (int[] tt : result) {
			double sum = 0;
			for (int i : tt) {
				sum += cls[i];
				print(i + ",");
			}
			println("    left " + df.format(stockLen - sum));
		}
		println(result.size() + " stocks ");
	}

	private void cutStock(double[] data) {
		int initlen = data.length;
		double[] cls = data.clone();
		boolean[] dead = new boolean[initlen];

		for (int k = 0; k < initlen; k++) {
			boolean[] bs = OnePack.solve(cls, stockLen, dead);
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (int i = 0; i < cls.length; i++) {
				if (bs[i]) {
					list.add(i);
					dead[i] = true;
				}
			}
			if(list.isEmpty()){
				break;
			}
			int[] arr = new int[list.size()];
			for (int i = 0; i < arr.length; i++)
				arr[i] = list.get(i);
			result.add(arr);
		}
	}
}