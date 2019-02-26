package fabrication;

import funicular.M;

public class Sicily {
	public final  int nodeId; 
	public final double[] origin;
	private final double[][] frame; // 3*3 rotation matrix
	private final double[][] frameT;
	public double[][] hex2ds;
	public double[][] intersects;
	public double[][] ps; // refer to hex2ds & intersects
	public double[][] dir2ds; // direction of holes
	public double[][] holes; // positions
	private final double[] offsets={10, 15, 10, 5,5};  //{9, 15, 9, 4.5,4.5} tested on Apr23
	public double[][][] reduce_offset;

	public Sicily(int nodeId,  double[] cnt, double[] nor,  double[][] dirs, double[][] hexa) { // nor and dirs normalized
		this.nodeId= nodeId;
		origin = cnt;
		double[] dx = dirs[0];
		double[] dy = M.crossProduct(nor, dx);
		frameT = new double[][] { dx, dy, nor };
		frame = M.transpose(frameT);
		
		dir2ds = new double[dirs.length][]; // directions
	    holes= new double[dirs.length][];
		for (int i = 0; i < dirs.length; i++) {
			double[] project = M.mul(frameT, dirs[i]);
			dir2ds[i] = new double[] { project[0], project[1] };
			holes[i]= M.scale(Pavilion.hexa_hole_depth, dir2ds[i]);
		}
		hex2ds = new double[hexa.length][]; // positions
		for (int i = 0; i < hexa.length; i++) {
			double[] v = M.sub(hexa[i], cnt);
			double[] project = M.mul(frameT, v);
			hex2ds[i] = new double[] { project[0], project[1] };
		}

		intersects = new double[dirs.length][];
		for (int i = 0; i < dirs.length; i++) {
			double[] p0 = hex2ds[2 * i + 1];
			double[] n0 = dir2ds[i];
			int j = (i + 1) % dirs.length;
			double[] p1 = hex2ds[2 * j];
			double[] n1 = dir2ds[j];
			intersects[i] = M.lineIntersect(p0, n0, p1, n1);
			if (null == intersects[i])
				throw new RuntimeException();
		}
		
		ps=new double[3*dirs.length][];
		for (int i = 0; i < dirs.length; i++) {
			ps[3 * i] = hex2ds[2 * i];
			ps[3 * i + 1] = hex2ds[2 * i + 1];
			ps[3 * i + 2] = intersects[i];
		}
		reduce();
	}

	private void reduce() {
		reduce_offset = new double[3][][];
		for (int i = 0; i < 3; i++) {
			double[][] poly = new double[5][];
			for (int j = 0; j < 4; j++)
				poly[j] = ps[(i * 3 + j - 1 + ps.length) % ps.length];
			poly[4] = new double[] { 0, 0 };
			reduce_offset[i] = M.offsetPoly(poly,M.area(poly) < 0 , offsets); //M.area(poly) < 0
		}
	}

	public double[][] ps3D() {
		double[][] arr = new double[ps.length][];
		for (int i = 0; i < ps.length; i++) {
			double[] p = { ps[i][0], ps[i][1], 0 };
			p = M.mul(frame, p);
			arr[i] = M.add(origin, p);
		}
		return arr;
	}

}
