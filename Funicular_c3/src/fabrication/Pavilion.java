package fabrication;

import java.text.DecimalFormat;
import java.util.ArrayList;

import funicular.M;
import funicular.Mesh_Agents_Voronoi;

public class Pavilion {
	private static final DecimalFormat df = new DecimalFormat("#.###");
	private static final DecimalFormat idf = new DecimalFormat("####");
	public static final float wid = 68;
	public static final float hei = 93f;
	public double[][] node_normals;
	public double[][][] node_between_dirs;
	public double[] mem_lens;

	public double minlen, maxlen;
	public int minlen_id;
	public double min_nor_dy_cos;// angle between node normal and dy
	public int max_nor_dy_id;//angle between node normal and dy
	public double max_normal_dx_cos;  //angle between  projection and dx, near 90
	public int max_normal_dx_id;
	
	public double[][] mem_cnts;
	public double[][] mem_dxs; // from member0 (for) to member 1(back), along len
	public double[][] mem_dys; // (fornormal+ backmormal)/2 and orthogonal to dx, along hei
	public double[][] mem_dzs; // //along wid
	
	public static final int hexa_depth = 58;//60;
	public static final int hexa_depth_sicily = hexa_depth-4;  //-4 cut with 8&6mm,
	public static final float hole_diameter =11;  //10 was tested Apri23
	public static final int hexa_hole_depth =47;//-11;
	public static final int fix_hexa_depth =37;
	public static final int fix_hole_depth =26;//37-11
	public double[][][] nodeHexa;    //hexa_depth
	public double[][][] nodepros;    //from node to holes
	public double[][][] nodeTriHoles; //hexa_hole_depth, direction instead of position
	//public Sicily[] sicis;   
	public ArrayList<Sicily> sicis;
	
	public final Mesh_Agents_Voronoi mesh;
	public Timber[] tims;

	public Pavilion(Mesh_Agents_Voronoi mesh) {
		this.mesh = mesh;
		int nodesize = mesh.nodesize;
		int membsize = mesh.membsize;
		int[][] members = mesh.members;
		double[][] nodes = mesh.nodes;
		int[][] nodeEdges = mesh.nodeEdges;

		// ************************************************************************ node normals, node_between_dirs
		node_normals = new double[nodesize][];
		node_between_dirs=new double[nodesize][][];
		for (int i = 0; i < nodesize; i++) {
			if (mesh.dead[i] || mesh.fixed(i)) { //    critical
				continue;
			}

			int[] edges =nodeEdges[i];
			if (3 == edges.length) {
				double[][] dirs = new double[edges.length][];  //3
				for (int j = 0; j < edges.length; j++) {
					int eid = edges[j];
					int jd = i == members[eid][0] ? members[eid][1] : members[eid][0];
					dirs[j] = M.sub(nodes[jd], nodes[i]);
				}
				
				double[][] nors = new double[edges.length][];    //********************************* node normals
				for (int j = 0; j < edges.length; j++) {
					double[] nor = M.crossProduct(dirs[j], dirs[(j + 1) % edges.length]);
					nors[j] = M.normalize(nor);
				}
				double[] sumv = M.add(nors);
				if (0 < M.area(dirs))
					M._scale(-1, sumv);
				node_normals[i] = M.normalize(sumv);
				
				double[][] betweens = new double[dirs.length][]; // ************************************** node_between_dirs
				for (int j = 0; j < dirs.length; j++)
					M._normalize(dirs[j]);
				for (int j = 0; j < dirs.length; j++) {
					double[] v1 = dirs[j];
					double[] v2 = dirs[(j + 1) % dirs.length];
					double theta = Math.acos(M.dotProduct(v1, v2));
					double[] orthdir = M.normalize(M.crossProduct(v1, v2));  //which v1  rotate about, the three could be all up, or, all down
					
					double[] nv =  M.rotate_Rodriguez(v1, 0.5 * theta, orthdir); 
					double[] project = M.scale(M.dotProduct(nv, node_normals[i] ), node_normals[i] );
					betweens[j] =M.normalize(M.sub(nv, project));
				}
				node_between_dirs[i]= betweens;
			} //end	for (int i = 0; i < nodesize; i++) {
		}
				
		//************************************************************************************ cnt dx dy dz
		mem_cnts=new double[membsize][]; 
		mem_dxs=new double[membsize][]; 
		mem_dys=new double[membsize][]; 
		mem_dzs=new double[membsize][]; 
		for(int i=0;i<membsize;i++){
			int[] edge=members[i];
			int m0 = edge[0];
			int m1 = edge[1];
			double[] nd0 = nodes[m0]; //for
			double[] nd1 = nodes[m1];  //back
			mem_cnts[i] = M.between(0.5, nd0, nd1);

			double[] dx = M.sub(nd1, nd0);
			M._normalize(dx);
			mem_dxs[i] =dx;
			double[] nor0 = node_normals[m0];  //for
			double[] nor1 = node_normals[m1];  //back
			double[] dy;
			if (mesh.fixed(m0))
				dy = nor1;
			else if (mesh.fixed(m1))
				dy = nor0;
			else
				dy = M.between(0.5, nor0, nor1);
			double[] proj = M.scale(M.dotProduct(dy, dx), dx); // scalar = a dot b, b must be normalized, should be scaleTo(if |dx| !=1 )
			dy = M.normalize(M.sub(dy, proj));
			mem_dys[i] = dy;
			mem_dzs[i] = M.crossProduct(dx, dy); // len==1
		}
		
		double[] sk = { Mesh_Agents_Voronoi.SINK[0], Mesh_Agents_Voronoi.SINK[1], 0 };
		for (int i = 0; i < nodesize; i++) {
			if(mesh.dead[i])
				continue;
			if (mesh.fixed(i)) { // critical
				int[] edges = nodeEdges[i];
				Integer eid = edges[0];
				double[] dz= mem_dzs[eid];
				double[] lined = new double[3];
				if (1 == mesh.fixed[i]) {
					M.planes_Intersect(nodes[i], dz, nodes[i], new double[] { 0, 0, 1 }, new double[3], lined);
					double[] dir = M.sub(nodes[i], sk);
					if (M.dotProduct(dir, lined) > 0)
						M._scale(-1, lined);
				} else if (3 == mesh.fixed[i] || 5 == mesh.fixed[i]) {
					M.planes_Intersect(nodes[i], dz, nodes[i], new double[] { 0, 1, 0 }, new double[3], lined);
					if (lined[2] < 0)
						M._scale(-1, lined);
				} else if (4 == mesh.fixed[i] || 6 == mesh.fixed[i]) {
					M.planes_Intersect(nodes[i], dz, nodes[i], new double[] { 1, 0, 0 }, new double[3], lined);
					if (lined[2] < 0)
						M._scale(-1, lined);
				}
				node_normals[i] = M.normalize(lined);
			}
		}
		// reportPavilion() ;
	}
	
	public void intersectHexa() {
		int nodesize = mesh.nodesize;
		nodeHexa = new double[nodesize][][];
		nodeTriHoles = new double[nodesize][][];
		double[][] nodes = mesh.nodes;
		int[][] nodeEdges = mesh.nodeEdges;
		int[][] members = mesh.members;

		sicis =  new ArrayList<Sicily> ();//new Sicily[nodesize];
		for (int i = 0; i < nodesize; i++) {
			if(mesh.dead[i]|| mesh.fixed(i))
				continue;
			double[] node = nodes[i];
			double[] node_nor = node_normals[i];
			
			int[] edges = nodeEdges[i];//*************the order
			double[][] hexa = new double[2 * edges.length][];
			double[][] pros_node = new double[edges.length][];
			nodeTriHoles[i] = new double[edges.length][];
			for (int j = 0; j < edges.length; j++) {
				int eid = edges[j];
				int jd = i == members[eid][0] ? members[eid][1] : members[eid][0];
				double[] pro = M.point_Project_Plane(nodes[jd], node, node_nor);
				pros_node[j] = M.sub(pro, node);
				double[] dir = M.scaleTo(hexa_depth_sicily, pros_node[j]);
				nodeTriHoles[i][j] = M.scaleTo(hexa_hole_depth, pros_node[j]);
				double[] line_ori = M.add(node, dir);
				double[] line_dir = M.crossProduct(pros_node[j], node_nor);
				M._normalize(line_dir);

				double[] cnt = mem_cnts[eid];
				double[] dz = mem_dzs[eid];
				double[] plane_p1 = M.add(cnt, M.scale(0.5 * wid, dz));
				double[] plane_p2 = M.add(cnt, M.scale(-0.5 * wid, dz));

				hexa[2 * j] = M.planeLine_Intersect(dz, plane_p1, line_ori, line_dir); // plane n, plane point, line p, line dir
				hexa[2 * j + 1] = M.planeLine_Intersect(dz, plane_p2, line_ori, line_dir);
			}
			for (int j = 0; j < edges.length; j++) {
				double[] cross = M.crossProduct(M.sub(hexa[2 * j], node), M.sub(hexa[2 * j + 1], node));
				if (0 < M.dotProduct(cross, node_nor))
					M.swap(hexa[2 * j], hexa[2 * j + 1]);
			}
			if (M.dotProduct(node_nor, M.crossProduct(pros_node[0], pros_node[1])) > 0) {
				for (int j = 0; j < edges.length; j++)
					M.swap(hexa[2 * j], hexa[2 * j + 1]);
			}

			nodeHexa[i] = hexa;
			for (int j = 0; j < edges.length; j++)
				M._normalize(pros_node[j]);
			//sicis[i] = new Sicily(i,node, node_nor, pros_node, hexa);
			sicis.add(new Sicily(i,node, node_nor, pros_node, hexa));
		}
//		int count=0;
//		for(int i=0;i<nodesize;i++){
//			if(null!=sicis[i])
//				count++;
//		}
		System.out.println(sicis.size()+" Sicily + " + mesh.fixedsize+" fixed (4 dead)");
	}
	
	public void postReport() {
		min_nor_dy_cos=1;  //report max angle between  node_normal and dy
		max_nor_dy_id=-1;
		for(int i=0;i<mesh.membsize;i++){
			int[] edge=mesh.members[i];
			int m0 = edge[0];
			int m1 = edge[1];
			if (!mesh.fixed(m0)){
				double dot= M.dotProduct(mem_dys[i],  node_normals[m0]);
				if(dot<0)
					throw new RuntimeException();
				if( min_nor_dy_cos>dot){
					min_nor_dy_cos=dot;
					max_nor_dy_id=i;
				}
			}
			if (!mesh.fixed(m1)){
				double dot= M.dotProduct(mem_dys[i],  node_normals[m1]);
				if(dot<0)
					throw new RuntimeException();
				if( min_nor_dy_cos>dot){
					min_nor_dy_cos=dot;
					max_nor_dy_id=i;
				}
			}
		}
		System.out.println("normal-dy "+df.format(   180*Math.acos(min_nor_dy_cos)/Math.PI  )   +  " degree   "+df.format( -1+ 1/min_nor_dy_cos));
		
		max_normal_dx_cos=-1;  //report max angle between  node_normal and dy
		max_normal_dx_id=-1;
		for(int i=0;i<mesh.nodesize;i++){
			if(mesh.fixed(i))
				continue;
			int[] edges = mesh.nodeEdges[i];
			double[] normal= node_normals[i];
			for (int j = 0; j < edges.length; j++) {
				int eid = edges[j];
				double dot = Math.abs(M.dotProduct(mem_dxs[eid], normal));  //near 90
				if(dot> max_normal_dx_cos){
					max_normal_dx_cos=dot;
					max_normal_dx_id= eid;
				}
			}
		}
		double ang= Math.acos(max_normal_dx_cos) - 0.5*Math.PI;
		System.out.println("normal-dx "+df.format(180*ang / Math.PI) + " degree");
	}

	public void reportMesh( ) {
		double minz= 100000;
		double maxz=-minz;
		for(double[] p: mesh.nodes){
			if(minz>p[2])
				minz=p[2];
			if(maxz<p[2])
				maxz = p[2];
		}
		System.out.println("z: (" + df.format(minz) + "," + df.format(maxz) + ") mm");
		
		minz = 100000;
		maxz = -minz;
		double sum = 0;
		double[][] nodes = mesh.nodes;
		mem_lens=new double[mesh.membsize];
		for (int i=0;i<mesh.membsize;i++) {
			int[] edge =mesh.members[i];
			double[] pa = nodes[edge[0]];
			double[] pb = nodes[edge[1]];
			double dist = M.dist(pa, pb);
			mem_lens[i]=dist;
			sum += dist;
			if (minz > dist){
				minz = dist;
				minlen_id=i;
			}
			if (maxz < dist)
				maxz = dist;
		}
		minlen=minz;
		maxlen=maxz;
		System.out.println("member len: (" + idf.format(minlen) + "~" + idf.format(maxlen) + ")mm");
		System.out.println("average len: " + df.format(sum / mesh.membsize)+" mm" );
		//System.out.println("min member: " +minid);
		System.out.println("total len: " + df.format(sum/1000)+" m   vol " +df.format( (hei*0.001)* (wid*0.001)*(sum*0.001))+" m3");
	}
	public void timberGeometry() {
		tims = new Timber[mesh.membsize];
		for (int i = 0; i < mesh.membsize; i++) {
			tims[i] = new Timber(i, mesh, this);
			tims[i].createVolume();
		}
	}

}
