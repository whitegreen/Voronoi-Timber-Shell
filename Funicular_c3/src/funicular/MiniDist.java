package funicular;

import org.ujmp.core.Matrix;
import org.ujmp.core.doublematrix.SparseDoubleMatrix2D;

public class MiniDist {
	int nvar;
	int ncon;
	int n ;
	private static final int dim = 3;
	private Integer[] var_ids;
	private int[] conids;
	private double[][] convs;
	private double[][] nvs;
	Mesh mesh;

	public MiniDist(Mesh me) {
		mesh= me;

		n = mesh.nodesize;
		ncon = mesh.fixedsize;
		nvar = n - ncon;
		conids = new int[ncon];
		
		int count = 0;
		for (int i = 0; i < n; i++) {
			if (mesh.fixed(i)) {
				conids[count] = i;
				count++;
			}
		}
		convs = new double[ncon][];
		for (int i = 0; i < ncon; i++) 
			convs[i] = mesh.nodes[ conids[i]  ];

		var_ids = new Integer[n];
		count=0;
		for (int i = 0; i < n; i++) {
			if (null != constOrder(i))
				continue;
			var_ids[i] = count;
			count++;
		}
	}

	private Integer constOrder(int number) {
		for (int i = 0; i < conids.length; i++) {
			if (conids[i] == number)
				return i;
		}
		return null;
	}
	
	public void solve() {
		double[][] all = new double[n][n];
		double[][] allbs = new double[n][dim];  //vector valued

		for (int[] memb :  mesh.members) {
			int a=memb[0];
			int b=memb[1];
			all[a][a] += 1;
			all[b][b] += 1;
			all[a][b] += -1;
			all[b][a] += -1;
		}
//		for (int i = 0; i < n/5; i++) {
//			for (int j = 0; j<n/5; j++) {
//				System.out.print(df.format(all[i][j])+",");
//			}
//			System.out.println();
//		}
		for (int i = 0; i < ncon; i++) {
			int cid = conids[i];
			int[] edges = mesh.nodeEdges[cid];

			for (int k = 0; k < edges.length; k++) {
				int[] memb = mesh.members[edges[k]];
				int jid = cid == memb[0] ? memb[1] : memb[0];
				for (int j = 0; j < dim; j++) {
					allbs[jid][j] += convs[i][j];
				}
//				for (int j = 0; j < dim; j++) {
//					if (cid - 1 >= 0)
//						allbs[cid - 1][j] += convs[i][j];
//					if (cid + 1 < n)
//						allbs[cid + 1][j] += convs[i][j];
//				}
			}
		}

		SparseDoubleMatrix2D has = SparseDoubleMatrix2D.Factory.zeros(nvar, nvar);
		for (int i = 0; i < n; i++) {
			Integer id = var_ids[i];
			if (null == id)
				continue;
			for (int j = 0; j < n; j++) {
				Integer jd = var_ids[j];
				if (null == jd)
					continue;
//			if(0!=all[i][j])
				//System.out.println(df.format(all[i][j])+": "+id+","+jd);
				has.setAsDouble(all[i][j] , id, jd);
			}
		}
//		for (int i = 0; i < nvar; i++) {
//			for (int j = 0; j < nvar; j++) {
//				System.out.print(df.format( has.getAsDouble(i,j)) + ",");
//			}
//			System.out.println();
//		}

		SparseDoubleMatrix2D[] bmats =new SparseDoubleMatrix2D[dim];
		for (int j = 0; j < dim; j++)
			bmats[j]=	SparseDoubleMatrix2D.Factory.zeros(nvar, 1);
		for (int i = 0; i < n; i++) {
			Integer id = var_ids[i];
			if (null == id)
				continue;
			for (int j = 0; j < dim; j++)
			    bmats[j].setAsDouble(allbs[i][j] , id, 0);
		}

		Matrix[] matDs = new 	Matrix[dim] ;
		for (int j = 0; j < dim; j++)
			matDs[j] = has.solveSPD(bmats[j]); // quick

		nvs = new double[n][dim];
		for (int i = 0; i < n; i++) {
			Integer constId = constOrder(i);
			if (null != constId) {
				nvs[i] = convs[constId];
			} else {
				for (int j = 0; j < dim; j++) {
					nvs[i][j] = matDs[j].getAsDouble(var_ids[i], 0);
				}
			}
		}
		
		for(int i=0;i<n;i++){
			if(mesh.fixed(i))
				continue;
			mesh.nodes[i]= nvs[i];
		}
	}

	

}
