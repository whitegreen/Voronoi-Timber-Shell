package test;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;

import dxfExporter.Constants;
import dxfExporter.DXFData;
import dxfExporter.DXFExport;
import dxfExporter.DXFLayer;
import dxfExporter.DXFPoint;
import fabrication.Pavilion;
import fabrication.Timber;
import funicular.M;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import funicular.Optimizer_Spring;
import processing.core.PApplet;

public class TestSicily2Foot extends PApplet {
	private static final DecimalFormat df = new DecimalFormat("#.###");
	private Mesh_Agents_Voronoi mesh;
	private Pavilion pav;
	private float sc = 1f;
	private int xx=125;
	private int yy=135;
	
	public void setup() {
		size(1100, 800);

		 mesh = new Mesh_Agents_Voronoi();
		MiniDist md = new MiniDist(mesh);
		md.solve();
		Optimizer_Spring opt = new Optimizer_Spring(mesh); // Spring Equilibrium
		opt.optimize();
		pav = new Pavilion(mesh);
		pav.intersectHexa();
		pav.timberGeometry();
		pav.reportMesh();
		pav.postReport();

		DXFExport wt = new DXFExport();
		wt.AutoCADVer = Constants.DXFVERSION_R2000;
		DXFLayer layer = new DXFLayer("cut 7mm thick");
		wt.setCurrentLayer(layer);
		DXFData dt = new DXFData();
		dt.LayerName = layer.getName();
		dt.Color = (Constants.convertColorRGBToDXF(Color.WHITE));

		for (Timber tim : pav.tims) {
			double[][] ln = null;
			double[] p=null;
			if (1 == mesh.fixed[tim.m0]) {
                ln= tim.forcuts;
                p= mesh.nodes[tim.m0];
			} else if (1 == mesh.fixed[tim.m1]) {
				 ln= tim.backcuts;
				 p= mesh.nodes[tim.m1];
			}
			if (null != ln) {
				dt.Point = new DXFPoint((float) ln[0][0], (float) ln[0][1], 0);
				dt.Point1 = new DXFPoint((float) ln[1][0], (float) ln[1][1], 0);
				wt.addLine(dt);
			
				double[][][] arr = CADecimal.digits("" + tim.ID,  -4f, -4.5f);
				arr = transform(new double[]{p[0], p[1]}, 0, 10, arr); // *** edge id
				for (double[][] poly : arr)
					polyC2D(0,0,wt, dt, poly);
			}

		}

		dt.Point = new DXFPoint((float)mesh.SINK[0], (float)mesh.SINK[1], 0);
		dt.Radius = mesh.SINK_FIXED-45;
		wt.addCircle(dt);
		dt.Radius = mesh.SINK_FIXED+45;
		wt.addCircle(dt);
		
		dt.Point = new DXFPoint((float)mesh.SINK[0]-mesh.SINK_FIXED-45, (float)mesh.SINK[1], 0);
		dt.Point1 = new DXFPoint((float)mesh.SINK[0]+mesh.SINK_FIXED+45, (float)mesh.SINK[1], 0);
		wt.addLine(dt);  //x
		dt.Point = new DXFPoint((float)mesh.SINK[0], (float)mesh.SINK[1]-mesh.SINK_FIXED, 0);
		dt.Point1 = new DXFPoint((float)mesh.SINK[0], (float)mesh.SINK[1]+mesh.SINK_FIXED, 0);  
		wt.addLine(dt); //y
		
		try {
			wt.saveToFile("/Users/huahao/Desktop/Upsilon_foot_Jun4.dxf");
		} catch (Exception excpt) {
		} finally {
			wt.finalize();
			println("dxf saved");
		}
	}



	//******************************************************************************************
	private static void polyC2D(float cx, float cy, DXFExport wt, DXFData dt, double[][] ps) {
		dt.Count = ps.length + 1;
		dt.Points = new ArrayList<DXFPoint>(ps.length + 1);
		for (double[] p : ps)
			dt.Points.add(new DXFPoint((float) p[0]+cx, (float) p[1]+cy, 0));
		double[] p = ps[0];
		dt.Points.add(new DXFPoint((float) p[0]+cx, (float) p[1]+cy, 0));
		wt.addPolyline(dt);
	}

//	private static void circle(float cx, float cy, DXFExport wt, DXFData dt, double[] p, double radius) {
//		dt.Point = new DXFPoint((float) p[0]+cx, (float) p[1]+cy, 0);
//		dt.Radius = (float) radius;
//		wt.addCircle(dt);
//	}
	
	private static void circle(float cx, float cy, DXFExport wt, DXFData dt, double[] p, double radius) {
	dt.Point = new DXFPoint((float) p[0]+cx, (float) p[1]+cy, 0);
	dt.Radius = (float) radius;
	wt.addCircle(dt);
}
	private static double[][][] transform(double[] pos, double ang, double s, double[][][] ps) {
		double[][][] re = new double[ps.length][][];
		for (int i = 0; i < ps.length; i++)
			re[i] = transform(pos, ang, s, ps[i]);
		return re;
	}

	private static double[][] transform(double[] pos, double ang, double s, double[][] ps) {
		double[][] re = new double[ps.length][];
		for (int i = 0; i < ps.length; i++)
			re[i] = transform(pos, ang, s, ps[i]);
		return re;
	}

	private static double[] transform(double[] pos, double ang, double s, double[] p) { // dir normalized
		double[] re = M.rotate(ang, p);
		M._scale(s, re);
		return M.add(re, pos);
	}

}
