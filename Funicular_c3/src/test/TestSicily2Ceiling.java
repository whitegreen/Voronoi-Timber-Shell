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

public class TestSicily2Ceiling extends PApplet {
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

		int count=0;
		for (Timber tim : pav.tims) {
			double delta = 50;
			if (2 < mesh.fixed[tim.m0]) {
				delta += (mesh.ceiling_h - tim.forhole[2]);
			} else if (2 < mesh.fixed[tim.m1]) {
				delta += (mesh.ceiling_h - tim.backhole[2]);
			} else {
				continue;
			}
			double[][] rect = { { Pavilion.wid * 0.5, 60 }, { Pavilion.wid * 0.5, -delta - 33 }, { -Pavilion.wid * 0.5, -delta - 33 }, { -Pavilion.wid * 0.5, 60 } };
			float cx=100* (count%6);
			float cy= 250*(count/6);
			polyC2D(cx,cy, wt, dt, rect);
			
			circle(cx,cy, wt, dt, new double[]{20, 0} , 8.5*0.5);
			circle(cx,cy, wt, dt, new double[]{-20, 0} , 8.5*0.5);
			circle(cx,cy, wt, dt, new double[]{20, 40} , 8.5*0.5);
			circle(cx,cy, wt, dt, new double[]{-20, 40} , 8.5*0.5);
			circle(cx,cy, wt, dt, new double[]{0, -delta-Pavilion.fix_hole_depth} , Pavilion.hole_diameter*0.5);
			
			double[][][] arr = CADecimal.digits("" + tim.ID,  -4f, -4.5f);
			arr = transform(new double[]{0, 40}, 0,6, arr); // *** edge id
			for (double[][] poly : arr)
				polyC2D(cx,cy,wt, dt, poly);
			
			count++;
		}

		try {
			wt.saveToFile("/Users/huahao/Desktop/Upsilon_ceiling_Jun5.dxf");
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

	
	private static void circle(float cx, float cy, DXFExport wt, DXFData dt, double[] p, double radius) {
		dt.Point = new DXFPoint((float) p[0] + cx, (float) p[1] + cy, 0);
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
