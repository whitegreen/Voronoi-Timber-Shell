package funicular;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Voronoi;
import wblut.geom.WB_VoronoiCell2D;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Mesh;

public class Mesh_Agents_Voronoi extends Mesh{
	private static final double tol = 0.001;
	public static final int WID = 5788;
	public static final int HEI = 5788;  //5800-6*2
	public static final double[] SINK= { 2300, 2200};
	private static final int cir_num =59;
	
	public static final int SINK_RAD=500;   
	public static final int SINK_FIXED=310;
	private static final int SINK_SRAD=250;   
	public static final int agent_iteration=80;
	private final Random RAN=new Random(-10005);

	public double[][] cs = new double[cir_num][]; // circle center
	public boolean[] static_circle =new boolean[cir_num]; 
	public double[] rs = new double[cir_num]; // circle radius
	private static double max_diameter=800;  //(900, 0.0015)
	private static final double  logi_ratio=0.002;  //0.0015
	public final int ceiling_h=3544; //3500-6+50
	public boolean[] dead;
	
	public Mesh_Agents_Voronoi() {
		double radi = 0.5 * logistic(WID / 2);
		int col = (int) (WID / (2 * radi));
		int row = (int) (HEI / (2 * radi));
		double gap = WID / col;
		
		for (int i = 0; i < cir_num; i++) {
			double x = gap + RAN.nextDouble() * (WID - 2 * gap);
			double y = gap + RAN.nextDouble() * (HEI - 2 * gap);
			double[] p = { x, y };
			if (M.dist(p, SINK) < SINK_RAD + SINK_SRAD) {
				double[] arrow = M.sub(p, SINK);
				arrow = M.scaleTo(SINK_RAD + SINK_SRAD, arrow);
				p = M.add(SINK, arrow);
			}
			cs[i] = p;
			rs[i] = 0.5 * logistic(M.dist(cs[i], SINK));
		}

		for (int i = 0; i < col; i++) {
			cs[i] = new double[] { WID / 2 + gap * (i + 0.5 - 0.5 * col), gap / 2 };
			rs[i] = gap / 2;
			cs[i+col] = new double[] { WID / 2 + gap * (i + 0.5 - 0.5 * col), HEI-gap / 2 };
			rs[i+col] = gap / 2;
		}
		int static_count = 2*col;
		gap = 0.99*HEI / row;  //exception
		for (int i = 0; i < row - 2; i++) { // /**********
			cs[static_count] = new double[] { gap/2, HEI / 2 + gap * (i + 1.5 - 0.5 * row) };
			rs[static_count ] = gap / 2;
			static_count++;
			cs[static_count ] = new double[] { WID-gap/2, HEI / 2 + gap * (i + 1.5 - 0.5 * row)};
			rs[static_count ] = gap / 2;
			static_count++;
		}
		cs[static_count] = SINK;
		rs[static_count] = SINK_RAD;
		static_count++;
		int ss=5;
		for (int i = 0; i < ss; i++) {
			double ang = i * 2 * Math.PI / ss;
			double[] p= {(SINK_RAD) * Math.cos(ang), (SINK_RAD) * Math.sin(ang) };
			cs[static_count] = M.add(SINK, p);
			rs[static_count]=SINK_SRAD;
			static_count++;
		}
		for (int i = 0; i < static_count; i++)
			static_circle[i] = true;
		
		for(int i=0;i<agent_iteration;i++)
			updateCircles();
		createVoronoi(cs);//****************************************************************
		
		nodesize = nodes.length;
		membsize = members.length;
		nodesize = nodes.length;
		membsize = members.length;
		System.out.println(nodesize+" nodes");
		System.out.println(membsize+" members");

		fixed=new int[nodesize];
		for (int i = 0; i < nodesize; i++) {
			double[] p = nodes[i];
			int ib=  isBoundary(p) ;
			if( 0<ib ){
			  fixed[i] = ib;
			} else {
				Integer type = sinkType(p);
				if (null != type)
					fixed[i] = type;
			}
			if (sinkDist(p) < SINK_FIXED)
				nodes[i][2] = -ceiling_h;// /***************************************************
		}
		for (int i = 0; i < nodesize; i++) {
			if (0 == fixed[i])
				continue;
			nodes[i][2] += ceiling_h;
		}

		fixedsize = 0;
		for (int i = 0; i < nodesize; i++) {
			if (0 < fixed[i])
				fixedsize++;
		}

		dead=new boolean[nodesize];
		for (int i = 0; i < nodesize; i++) 
			 dead[i]=isCorner(nodes[i]);
		nodeEdges();
	}
	
	private static double sinkDist(double[] p) {
		return  M.dist( SINK,p);
	}
	
	private static Integer sinkType(double[] p) {
		if (  M.dist_sq(SINK,p) < SINK_FIXED*SINK_FIXED)
			return 1;
		return null;
	}

	private static double logistic(double dist) {
		double ep = Math.exp(-logi_ratio * dist);
		return max_diameter * (1 - ep) / (1 + ep);
	}

	private void push(int i, int j) {
		double dis = M.dist(cs[i], cs[j]);
		if (dis < rs[i] + rs[j]) {
			double[] arrow = M.sub(cs[i], cs[j]);
			double force =   0.01+ ( rs[i] + rs[j] - dis)/( rs[i] + rs[j]);
			arrow = M.scaleTo(50*force, arrow);  //force=5
			if (!static_circle[i])
				M._add(cs[i], arrow);
			if (!static_circle[j])
				M._sub(cs[j], arrow);
		}
	}

	private void createVoronoi(double[][] cntps) {
		ArrayList<WB_Point> points = new ArrayList<WB_Point>();
		for (double[] p : cntps)
			points.add(new WB_Point(p[0], p[1]));
		List<WB_Point> boundary = new ArrayList<WB_Point>();
		boundary.add(new WB_Point(0, 0));
		boundary.add(new WB_Point(WID, 0));
		boundary.add(new WB_Point(WID, HEI));
		boundary.add(new WB_Point(0, HEI));
		List<WB_VoronoiCell2D> cells =WB_Voronoi. getClippedVoronoi2D(points, boundary)  ;  //WB_Voronoi.getClippedVoronoi2D(points, boundary);

		WB_Polygon[] polys = new WB_Polygon[cells.size()];
		for (int i = 0; i < polys.length; i++)
			polys[i] = cells.get(i).getPolygon();
		HEC_FromPolygons hec = new HEC_FromPolygons();
		hec.setPolygons(polys);
		HE_Mesh mesh = hec.create();

		nodes = mesh.getVerticesAsDouble();
		System.out.println("4 corner (dead) nodes");
//		ArrayList<double[]> nlist=new 	ArrayList<double[]>();
//		for(double[] v: arr){
//			if(isCorner(v)){
//				System.out.println("corner");
//				continue;
//			}
//				nlist.add(v);
//		}
//		nodes=new double[nlist.size()][];
//		for (int i = 0; i < nodes.length; i++) 
//			nodes[i]= nlist.get(i);
			
		int[][] alledges = mesh.getEdgesAsInt();
		ArrayList<int[]> elist = new ArrayList<int[]>();
		for (int i = 0; i < alledges.length; i++) {
			int[] edge = alledges[i];
			if (0 < isBoundary(nodes[edge[0]]) && 0 < isBoundary(nodes[edge[1]]))
				continue;
			if (sinkDist(nodes[edge[0]]) < SINK_FIXED && sinkDist(nodes[edge[1]]) < SINK_FIXED)
				continue;
			elist.add(edge);
		}
		members = new int[elist.size()][];
		for (int i = 0; i < members.length; i++)
			members[i] = elist.get(i);
	}
	
	private static boolean isCorner(double[] p) { 
		if(Math.abs(p[0]) < tol && Math.abs(p[1]) < tol)
			return true;
		if(Math.abs(p[0]) < tol && Math.abs(p[1] - HEI) < tol)
			return true;
		if (Math.abs(p[0] - WID) < tol  && Math.abs(p[1]) < tol )
			return true;
		if (Math.abs(p[0] - WID) < tol  && Math.abs(p[1] - HEI) < tol )
			return true;
		return false;
	}
	
	private static int isBoundary(double[] p) { //-1 not bondary,   3 (along x), 4, 5 (along x), 6
		
		if (Math.abs(p[0]) < tol)
			return 6;
		if (Math.abs(p[1]) < tol)
			return 3;
		if (Math.abs(p[0] - WID) < tol)
			return 4;
		if (Math.abs(p[1] - HEI) < tol)
			return 5;
		return -1;
		// return Math.abs(p[0]) < tol || Math.abs(p[1]) < tol || Math.abs(p[0] - WID) < tol || Math.abs(p[1] - HEI) < tol;
	}
	
	private void updateCircles(){
		for (int i = 0; i < cir_num; i++) 
			for (int j = i + 1; j < cir_num; j++) 
				push(i, j);
		for (int i = 0; i < cir_num; i++) {
			if (static_circle[i])
				continue;
			rs[i] = 0.5 * logistic(sinkDist(cs[i]));
		}
	}
//	private void nodeEdges(){
//		ArrayList<Integer>[] lists = new ArrayList[nodesize];
//		for (int i = 0; i < nodesize; i++)
//			lists[i] = new ArrayList<Integer>();
//		for (int i = 0; i < membsize; i++) {
//			int[] member = members[i];
//			int a = member[0];
//			int b = member[1];
//			lists[a].add(i);
//			lists[b].add(i);
//		}
//		nodeEdges = new int[nodesize][];
//		for (int i = 0; i < nodesize; i++) {
//			int size = lists[i].size();
//			int[] arr = new int[size];
//			for (int j = 0; j < size; j++)
//				arr[j] = lists[i].get(j);
//			nodeEdges[i] = arr;
//		}
//	}
}
