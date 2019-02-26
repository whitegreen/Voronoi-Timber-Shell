package cutstock;

import fabrication.Pavilion;
import funicular.M;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import funicular.Optimizer_Spring;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import processing.core.PApplet;

public class TestAllPack1 extends PApplet {  
	private static final DecimalFormat df = new DecimalFormat("#.##");
	private final float stockLen = 4000;  //length of the stock material
	//private ArrayList<int[]> result = new ArrayList<int[]>();
	private static final int stock_num = 22;//estimated, 126 stocks are sufficient
	private static final  int margin= 0; //25
    private int[][] result= new int[stock_num][];
    private double[] cls;
    
	public void setup() {
		size(1000,800);
		
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
		
		cls = new double[mesh.membsize]; // 460 pieces
		int[][] members= mesh.members;
		double[][] nodes= mesh.nodes;
		for(int i=0;i<cls.length;i++){
			int[] m=  members[i];
			cls[i]=margin+  M.dist(nodes[m[0]], nodes[m[1]]);
		}
		
		try {
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);
			GRBVar[] vars = new GRBVar[cls.length * stock_num];
			for (int i = 0; i < vars.length; i++)
				vars[i] = model.addVar(0, 1, 0, GRB.BINARY, "");

			GRBLinExpr expr_obj = new GRBLinExpr();
			for (int i = 0; i < stock_num; i++) 
				for (int j = 0; j < cls.length; j++) 
					expr_obj.addTerm(cls[j], vars[i * cls.length + j]);
			model.setObjective(expr_obj, GRB.MAXIMIZE); // objective
			//see https://en.wikipedia.org/wiki/Cutting_stock_problem

			for (int i = 0; i < stock_num; i++) {
				GRBLinExpr  expr = new GRBLinExpr();
				for (int j = 0; j < cls.length; j++) 
					expr.addTerm(cls[j], vars[i * cls.length + j]);
				model.addConstr(expr, GRB.LESS_EQUAL, stockLen, ""); // constraints
			}
			// see https://en.wikipedia.org/wiki/Cutting_stock_problem

			for (int j = 0; j < cls.length; j++) {
				GRBLinExpr expr = new GRBLinExpr();
				for (int i = 0; i < stock_num; i++)
					expr.addTerm(1, vars[i * cls.length + j]);
				model.addConstr(expr, GRB.EQUAL, 1, "");
			}
			model.optimize();
			// ***************************************************print result
			for (int i = 0; i < stock_num; i++) {
				int count=0;
				for (int j = 0; j < cls.length; j++) {
					double x = vars[i * cls.length + j].get(GRB.DoubleAttr.X);
					if (x > 0.5){
						count++;
						print(j + ",");
					}
				}
				println();
				
				result[i]=new int[count];
				count=0;
				for (int j = 0; j < cls.length; j++) {
					double x = vars[i * cls.length + j].get(GRB.DoubleAttr.X);
					if (x > 0.5){
						result[i][count]=j;
						count++;
					}
				}
			}

			model.dispose();
			env.dispose();
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}
	
	public void draw(){
		background(255);
		translate(100,50);
		smooth();
		float sc = 0.2f;
		for (int i = 0; i < stock_num; i++) {
			int[] a = result[i];
			fill(255,0,0);
			rect(0, i*20, sc * stockLen, 15);
			double sum=0;
			for (int j = 0; j < a.length; j++) {
				fill(180);
				rect((float)(sum*sc) ,i*20,  (float)(cls[a[j]]*sc), 15);
				fill(0);
				text(   a[j] + "",   (float)( (sum+0.5*cls[a[j]]) *sc)     ,i*20+12);
				sum+=cls[a[j]];
			}
		}
	}

}