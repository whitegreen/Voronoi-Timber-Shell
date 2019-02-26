package fabrication;

import java.text.DecimalFormat;
import kuka.LA;
import funicular.M;
import funicular.Mesh_Agents_Voronoi;

public class Timber {
	private static final DecimalFormat df = new DecimalFormat("#.###");
	public static final double[] upz = { 0, 0, 1 };
	public static final double[] downz = { 0, 0, -1 };
	public static final double[] upy = { 0, 1, 0 };
	public static final double[] downy = { 0, -1, 0 };
	public final Mesh_Agents_Voronoi mesh;
	public final Pavilion pav;
	
    private final int type; //0, 1, 3456
	public Integer ID;
	public Integer m0, m1; // refer to mesh nodes
	public double[][] for_betdirs;// refer to Pavilion's node_between_dirs;
	public double[][] back_betdirs;
	public double[][] fps; // usually 6 points
	public double[][] bps;

	public double[][] forcuts; // inherit from pavilion, identical with Sicily
	public double[][] backcuts;
	public double[] forhole; // inherit from pavilion
	public double[] uphole, dnhole; //3456
	public double[] backhole;
    public double[] inward ;// for 3456, either for or back
	public double[] inward_onXY;

	// local********************************************************
	public double[] _p0, _p1;
	public double[] _p0up, _p0dn, _p1up, _p1dn;  //not along   _back(for)_betdirs, 
	public double[] _fornormal, _backnormal;
	public double[][] _fps, _bps;
	public double[] _forhole, _backhole; // may null
	public double[][] _for_betdirs;
	public double[][] _back_betdirs;
	public double[][] _forcuts; // hexa may null
	public double[][] _backcuts; //hexa  may null
	public double[] _inward_onXY;
	public double[] _uphole, _dnhole; //for 3456
	// tool path***********************************************************
	public double[] _backleftABC, _backrightABC;
	public double[] _forleftABC, _forrightABC;
	public double[] _backcutABC, _forcutABC;
	public double[][] _backts, _forts;
	public double[][] _backts_hale, _backts_hari; // half by half
	public double[][] _forts_hale, _forts_hari;

	public double[] _backcutpa, _backcutpb;
	public double[] _forcutpa, _forcutpb;
	public double[] _forcutpa_half, _forcutpb_half;
	public double[] _backcutpa_half, _backcutpb_half;

	public Timber(int id, Mesh_Agents_Voronoi mesh, Pavilion pav) {
		ID = id;
		this.mesh = mesh;
		this.pav = pav;
		int[][] members = mesh.members;
		m0 = members[ID][0];
		m1 = members[ID][1];
		forcuts = new double[2][];
		backcuts = new double[2][];
		forhole = new double[3];
		backhole = new double[3];
        
		if (1 == mesh.fixed[m0] || 1 == mesh.fixed[m1]) { // either
			type = 1;
		} else if (3 == mesh.fixed[m0] || 3 == mesh.fixed[m1]) { // either
			type = 3;
			inward = new double[] { 0, 1, 0 };
		} else if (4 == mesh.fixed[m0] || 4 == mesh.fixed[m1]) { // either
			type = 4;
			inward = new double[] { -1, 0, 0 };
		} else if (5 == mesh.fixed[m0] || 5 == mesh.fixed[m1]) { // either
			type = 5;
			inward = new double[] { 0, -1, 0 };
		} else if (6 == mesh.fixed[m0] || 6 == mesh.fixed[m1]) { // either
			type = 6;
			inward = new double[] { 1, 0, 0 };
		} else {
			type = 0;
		}
		for_betdirs = select_between(m0, m1, forcuts, forhole);
		back_betdirs = select_between(m1, m0, backcuts, backhole);
		
		if (2<type) {
			double[] facenor = pav.mem_dys[ID].clone();
			double[] faceupv = M.add(pav.mem_cnts[ID], M.scaleTo(0.5 * Pavilion.hei, facenor));
			double[] facednv = M.add(pav.mem_cnts[ID], M.scaleTo(-0.5 * Pavilion.hei, facenor));
			double[] hole = mesh.fixed[m0] > 2 ? forhole : backhole;
			uphole = M.planeLine_Intersect(facenor, faceupv, hole, upz);// n, point, p, dir
			dnhole = M.planeLine_Intersect(facenor, facednv, hole, downz);
		}
	}

	private static final double[][] path4(double toolR, double margin, double[] vup, double[] vmd, double[] vdn, double[] nor_toLeft, double[][] dirs) {
		double[][] path = new double[4][];
		double[] arrow = M.crossProduct(nor_toLeft, dirs[0]);
		arrow = M.scaleTo(toolR, arrow);
		double[] tt = M.scaleTo(margin, M.sub(vup, vmd));
		path[0] = M.add(M.add(vup, arrow), tt);
		path[1] = M.sub(M.add(vmd, arrow), tt);
		arrow = M.crossProduct(dirs[1], nor_toLeft);
		arrow = M.scaleTo(toolR, arrow);
		tt = M.scaleTo(margin, M.sub(vdn, vmd));
		path[2] = M.sub(M.add(vmd, arrow), tt);
		path[3] = M.add(M.add(vdn, arrow), tt);
		if (path[0][2] < path[3][2])
			throw new RuntimeException();
		return path;
	}

	public void toolPath(double toolR, double margin) {
		_backleftABC = LA.ABCby2Axis(_backnormal, downz);
		_backrightABC = LA.ABCby2Axis(M.scale(-1, _backnormal), upz);
		_forleftABC = LA.ABCby2Axis(M.scale(-1, _fornormal), downz);
		_forrightABC = LA.ABCby2Axis(_fornormal, upz);
		
		if (mesh.fixed[m0] > 2)
			_forcutABC = LA.ABCby2Axis(_inward_onXY, downy);// / 3,4,5,6 ceiling 
		else
			_forcutABC = LA.ABCby2Axis(M.sub(_forhole, _p0), downy);
		if (mesh.fixed[m1] > 2)
			_backcutABC = LA.ABCby2Axis(_inward_onXY, upy);// 3,4,5,6 ceiling
		else
			_backcutABC = LA.ABCby2Axis(M.sub(_backhole, _p1), upy);

		
		double[] nor_toLeft = _backnormal;
		_backts = path4(toolR, margin, _p1up, _p1, _p1dn, nor_toLeft, _back_betdirs);
		double[] vup = M.between(0.5, _p1up, _bps[2]);
		double[] vmd = M.between(0.5, _p1, _bps[3]);
		double[] vdn = M.between(0.5, _p1dn, _bps[4]);
		_backts_hale = path4(toolR, margin, vup, vmd, vdn, nor_toLeft, _back_betdirs);
		vup = M.between(0.5, _p1up, _fps[2]);
		vmd = M.between(0.5, _p1, _fps[3]);
		vdn = M.between(0.5, _p1dn, _fps[4]);
		_backts_hari = path4(toolR, margin, vup, vmd, vdn, nor_toLeft, _back_betdirs);

		nor_toLeft = M.scale(-1, _fornormal);
		_forts = path4(toolR, margin, _p0up, _p0, _p0dn, nor_toLeft, _for_betdirs);
		vup = M.between(0.5, _p0up, _fps[1]);
		vmd = M.between(0.5, _p0, _fps[0]);
		vdn = M.between(0.5, _p0dn, _fps[5]);
		_forts_hale = path4(toolR, margin, vup, vmd, vdn, nor_toLeft, _for_betdirs);
		vup = M.between(0.5, _p0up, _bps[1]);
		vmd = M.between(0.5, _p0, _bps[0]);
		vdn = M.between(0.5, _p0dn, _bps[5]);
		_forts_hari= path4(toolR, margin, vup, vmd, vdn, nor_toLeft, _for_betdirs);
		// *************************************************************************************
		double[] pa, pb;
		if (_backcuts[0][2] < _backcuts[1][2]) {
			pa = _backcuts[0];
			pb = _backcuts[1];
		} else {
			pa = _backcuts[1];
			pb = _backcuts[0];
		}
		double[] arrow = M.sub(pa, pb); // down
		arrow = M.scaleTo(margin, arrow);
		_backcutpa = M.add(pa, arrow);
		_backcutpb = M.sub(pb, arrow);

		double[] arrow_back;
		if (mesh.fixed[m1] > 2)
			arrow_back = M.scaleTo(-Pavilion.fix_hexa_depth * 0.5, _inward_onXY);// _inward_onXY is along surface but globally horizontal
		else
			arrow_back = M.scaleTo(Pavilion.hexa_depth * 0.5, M.sub(_p1, _backhole));
		_backcutpa_half = M.add(_backcutpa, arrow_back);
		_backcutpb_half = M.add(_backcutpb, arrow_back);

		if (_forcuts[0][2] < _forcuts[1][2]) {
			pa = _forcuts[0];
			pb = _forcuts[1];
		} else {
			pa = _forcuts[1];
			pb = _forcuts[0];
		}
		arrow = M.sub(pa, pb); // down
		arrow = M.scaleTo(margin, arrow);
		_forcutpa = M.add(pa, arrow);
		_forcutpb = M.sub(pb, arrow);
		double[] arrow_for;
		if (mesh.fixed[m0] > 2)
			arrow_for = M.scaleTo(-Pavilion.fix_hexa_depth * 0.5, _inward_onXY);// _inward_onXY is along surface but globally horizontal
		else
			arrow_for = M.scaleTo(Pavilion.hexa_depth * 0.5, M.sub(_p0, _forhole));
		_forcutpa_half = M.add(_forcutpa, arrow_for);
		_forcutpb_half = M.add(_forcutpb, arrow_for);
	}

	public void local() {
		double[] cnt = pav.mem_cnts[ID].clone();
		double[] dx = pav.mem_dxs[ID].clone();
		double[] dy = pav.mem_dys[ID].clone();
		double[] dz = pav.mem_dzs[ID].clone();
		double[][] frameT = { dx, dy, dz };

		if (null != inward_onXY)
			_inward_onXY = M.mul(frameT, inward_onXY);  //direction
		
		_fornormal = M.mul(frameT, pav.node_normals[m0]);
		_backnormal = M.mul(frameT, pav.node_normals[m1]);
		_for_betdirs = new double[for_betdirs.length][];
		_back_betdirs = new double[for_betdirs.length][];
		for (int i = 0; i < for_betdirs.length; i++) {
			_for_betdirs[i] = M.mul(frameT, for_betdirs[i]);
			_back_betdirs[i] = M.mul(frameT, back_betdirs[i]);
		}
		if(uphole!=null)
			_uphole=M.mul(frameT, M.sub(uphole, cnt));
		if(dnhole!=null)
			_dnhole=M.mul(frameT, M.sub(dnhole, cnt));
		
		_p0 = M.mul(frameT, M.sub(mesh.nodes[m0], cnt));
		_p1 = M.mul(frameT, M.sub(mesh.nodes[m1], cnt));
		_fps = pointsToLocal(frameT, cnt, fps);
		_bps = pointsToLocal(frameT, cnt, bps);

		_forhole = M.mul(frameT, M.sub(forhole, cnt)); 
		_forcuts = pointsToLocal(frameT, cnt, forcuts); 
		_backhole = M.mul(frameT, M.sub(backhole, cnt));  
		_backcuts = pointsToLocal(frameT, cnt, backcuts);

		_p0up = M.between(0.5, _fps[1], _bps[1]);
		_p0dn = M.between(0.5, _fps[5], _bps[5]);
		if (_p0up[2] < _p0dn[2])
			throw new RuntimeException();
		_p1up = M.between(0.5, _fps[2], _bps[2]);
		_p1dn = M.between(0.5, _fps[4], _bps[4]);
		if (_p1up[2] < _p1dn[2])
			throw new RuntimeException();
	}

	private static double[][] pointsToLocal(double[][] frameT, double[] cnt, double[][] ps) {
		double[][] arr = new double[ps.length][];
		for (int i = 0; i < ps.length; i++)
			arr[i] = M.mul(frameT, M.sub(ps[i], cnt));
		return arr;
	}

	private double[][] select_between( int na, int nb, double[][] twocuts, double[] hole) {
		double[][] nodes = mesh.nodes;
		double[] axis = M.sub(nodes[nb], nodes[na]);
		double[] cnt = pav.mem_cnts[ID].clone();
		double[] dz = pav.mem_dzs[ID].clone();
		double[] plane_p1 = M.add(cnt, M.scale(0.5 * Pavilion.wid, dz));
		double[] plane_p2 = M.add(cnt, M.scale(-0.5 * Pavilion.wid, dz));
		double[] node_na=nodes[na];
		double[] node_nor = pav.node_normals[na];
		
		if (mesh.fixed(na)) {
			double[][] arr = new double[2][];
			double[] hh = null;
			if (1 == type || 2 == type) { // sink on ground, very much like conventional nodes
				arr[0] = new double[] { -node_nor[1], node_nor[0], 0 };
				arr[1] = new double[] { node_nor[1], -node_nor[0], 0 };
				
				hh = M.add(nodes[na], new double[] { 0, 0, Pavilion.hexa_hole_depth }); //straight up
				
				double[] line_p = M.add(node_na, new double[] { 0, 0, Pavilion.hexa_depth }); //straight up
//				double[] vec= M.sub(node_na,  mesh.SINK ,2);
				//double[] line_dir = { vec[1], -vec[0], 0 };  //regular******
				double[] line_dir = {dz[0], dz[1],0};
				M._normalize(line_dir);
				twocuts[0] = M.planeLine_Intersect(dz, plane_p1, line_p, line_dir); // through p0 or p1
				twocuts[1] = M.planeLine_Intersect(dz, plane_p2, line_p, line_dir);
			} else { // ****************************************************** ceiling
				int dep = Pavilion.fix_hole_depth;
				int cha = Pavilion.fix_hexa_depth - Pavilion.fix_hole_depth;
				double[] v = axis.clone();
			    inward_onXY = new double[3];
				if (3 == type) {
					M._scale(Math.abs(dep / v[1]), v);
				} else if (4 == type) {
					M._scale(Math.abs(dep / v[0]), v);
				} else if (5 == type) {
					M._scale(Math.abs(dep / v[1]), v);
				} else if (6 == type) {
					M._scale(Math.abs(dep / v[0]), v);
				}
				hh = M.add(node_na, v);
				double[] line_p = M.add(hh, M.scale(cha, inward));
				double[] line_dir = { -inward[1], inward[0], 0 }; // sign?
				twocuts[0] = M.planeLine_Intersect(dz, plane_p1, line_p, line_dir);// NOT through p0 or p1
				twocuts[1] = M.planeLine_Intersect(dz, plane_p2, line_p, line_dir);
				
				M.planes_Intersect(hh, upz, plane_p1, dz, new double[3], inward_onXY); //(p,n ,p n, linep, line dir)
				double[] sk = { mesh.SINK[0], mesh.SINK[1], 0 };
				if (0 > M.dotProduct(inward_onXY, M.sub(sk, nodes[na])))
					M._scale(-1, inward_onXY);
				
				arr[0] = line_dir.clone();
				arr[1] = M.scale(-1, line_dir);
			}

			M.assign(hole, hh); // **hole
			boolean b1 = M.dotProduct(arr[0], dz) < 0;
			boolean b2 = M.dotProduct(arr[1], dz) < 0;
			if (b1 == b2)
				throw new RuntimeException();
			if (b1)
				M.swap(arr[0], arr[1]);
			return arr;
		}

		int[] edges = mesh.nodeEdges[na];
		double[][] bds = pav.node_between_dirs[na];
		Integer flag = null;
		for (int i = 0; i < edges.length; i++) {
			if (ID == edges[i]) {
				flag = i;
				break;
			}
		}
		double[][] hex = pav.nodeHexa[na];
		twocuts[0] = hex[2 * flag];
		twocuts[1] = hex[2 * flag + 1];
		double[] hh = M.add(node_na, pav.nodeTriHoles[na][flag]);
		M.assign(hole, hh);

		double[][] between_dirs = new double[2][];
		between_dirs[0] = bds[flag];
		between_dirs[1] = bds[(flag + edges.length - 1) % edges.length];
		boolean b1 = M.dotProduct(between_dirs[0], dz) < 0;
		boolean b2 = M.dotProduct(between_dirs[1], dz) < 0;
		if (b1 == b2)
			throw new RuntimeException();
		double[][] re = new double[2][];
		if (b1) {
			re[0] = between_dirs[1];
			re[1] = between_dirs[0];
		} else {
			re[0] = between_dirs[0];
			re[1] = between_dirs[1];
		}
		return re;
	}

	public void createVolume() {
		fps = inter6(0.5 * Pavilion.hei);
		bps = inter6(-0.5 * Pavilion.hei);
	}

	private double[][] inter6(double h) {
		double[] fornode = mesh.nodes[m0];
		double[] backnode = mesh.nodes[m1];
		double[] cnt = pav.mem_cnts[ID].clone();
		double[] dx = pav.mem_dxs[ID].clone();
		double[] dy = pav.mem_dys[ID].clone();
		double[] dz = pav.mem_dzs[ID].clone();
		double[] fornormal = pav.node_normals[m0];
		double[] backnormal = pav.node_normals[m1];
		double[][] ps = new double[6][];
		ps[0] = M.planeLine_Intersect(dy, M.add(fornode, M.scale(h, dy)), fornode, fornormal); // plane normal, plane point, line normal, line direction
		ps[3] = M.planeLine_Intersect(dy, M.add(backnode, M.scale(h, dy)), backnode, backnormal);

		double[] rela = M.add(h, dy, 0.5 * Pavilion.wid, dz);
		double[] ep = M.add(cnt, rela);
		double[] plane_normal = M.crossProduct(fornormal, for_betdirs[0]);
		ps[1] = M.planeLine_Intersect(plane_normal, fornode, ep, dx);
		plane_normal = M.crossProduct(backnormal, back_betdirs[0]);
		ps[2] = M.planeLine_Intersect(plane_normal, backnode, ep, dx);
		rela = M.add(h, dy, -0.5 * Pavilion.wid, dz);
		ep = M.add(cnt, rela);
		plane_normal = M.crossProduct(backnormal, back_betdirs[1]);
		ps[4] = M.planeLine_Intersect(plane_normal, backnode, ep, dx);
		plane_normal = M.crossProduct(fornormal, for_betdirs[1]);
		ps[5] = M.planeLine_Intersect(plane_normal, fornode, ep, dx);
		return ps;
	}

	private static void println(double v) {
		System.out.println(df.format(v));
	}

	private static void println(double[] v) {
		System.out.println(df.format(v[0]) + "," + df.format(v[1]) + "," + df.format(v[2]));
	}
}
