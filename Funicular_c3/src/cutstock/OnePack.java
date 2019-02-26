package cutstock;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class OnePack {

	public static boolean[] solve(double[] cls, double stockLen, boolean[] dead) {
		int cut_num = cls.length;
		boolean[] bs = new boolean[cut_num];
		try {
			GRBEnv env = new GRBEnv("mip1.log");
			GRBModel model = new GRBModel(env);
			GRBVar[] vars = new GRBVar[cut_num];
			if (null == dead) {
				for (int i = 0; i < cut_num; i++)
					vars[i] = model.addVar(0, 1, 0, GRB.BINARY, "");
			} else {
				for (int i = 0; i < cut_num; i++)
					vars[i] = model.addVar(0, dead[i] ? 0 : 1, 0, GRB.BINARY, "");
			}

			GRBLinExpr expr = new GRBLinExpr();
			for (int i = 0; i < cut_num; i++)
				expr.addTerm(    cls[i], vars[i]);    // or  1, or cls[i], or  cls[i] * cls[i]
			model.setObjective(expr, GRB.MAXIMIZE);

			expr = new GRBLinExpr();
			for (int i = 0; i < cut_num; i++)
				expr.addTerm(cls[i], vars[i]);
			model.addConstr(expr, GRB.LESS_EQUAL, stockLen, "");

			model.optimize();
			for (int i = 0; i < cut_num; i++) {
				double x = vars[i].get(GRB.DoubleAttr.X);
				bs[i] = x > 0.5;
			}
			model.dispose();
			env.dispose();
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
		return bs;
	}

}
