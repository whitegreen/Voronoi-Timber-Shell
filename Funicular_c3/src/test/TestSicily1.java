package test;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import dxfExporter.Constants;
import dxfExporter.DXFData;
import dxfExporter.DXFExport;
import dxfExporter.DXFLayer;
import dxfExporter.DXFPoint;
import funicular.M;
import processing.core.PApplet;

public class TestSicily1 extends PApplet {
	private static final DecimalFormat df = new DecimalFormat("#.###");
	
	public void setup() {
		DXFExport wt = new DXFExport();
		wt.AutoCADVer = Constants.DXFVERSION_R2000;
		DXFLayer layer = new DXFLayer("cut 7mm thick");
		wt.setCurrentLayer(layer);
		DXFData dt = new DXFData();
		dt.LayerName = layer.getName();
		dt.Color = (Constants.convertColorRGBToDXF(Color.WHITE));

		for (int i = 0; i < 12; i++) {
			int wid = 140;
			int radi = 70;
			double angle= i*Math.PI*2.0/12;
			double[] dir=  { Math.cos(angle), Math.sin(angle)};
			rect_arc(i*200, 0, wt, dt, wid, new double[] { 0, 0 }, radi, dir);
			// (float cx, float cy, DXFExport wt, DXFData dt, double wid, double[] p, double radius, double[] dir) {
		}
		try {
			wt.saveToFile("/Users/huahao/Desktop/dxf_Jun3.dxf");
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
		dt.Point = new DXFPoint((float) p[0]+cx, (float) p[1]+cy, 0);
		dt.Radius = (float) radius;
		wt.addCircle(dt);
	}
	
	private static void rect_arc(float cx, float cy, DXFExport wt, DXFData dt, double wid, double[] p, double radius, double[] dir) {
		double[] arrow = M.scaleTo(0.5 * wid, dir);
		double[] pa = { p[0] + cx + arrow[0], p[1] + cy + arrow[1], };
		double[] pb = { p[0] + cx - arrow[0], p[1] + cy - arrow[1] };

		double[] ortho = { -dir[1], dir[0] };
		ortho = M.scaleTo(radius, ortho);
		double[] p0 = M.add(pa, ortho);
		double[] p1 = M.sub(pa, ortho);
		double[] p2 = M.add(pb, ortho);
		double[] p3 = M.sub(pb, ortho);
		dt.Point = new DXFPoint((float) p0[0], (float) p0[1], 0);
		dt.Point1 = new DXFPoint((float) p2[0], (float) p2[1], 0);
		wt.addLine(dt);
		dt.Point = new DXFPoint((float) p3[0], (float) p3[1], 0);
		dt.Point1 = new DXFPoint((float) p1[0], (float) p1[1], 0);
		wt.addLine(dt);

		dt.Point = new DXFPoint((float) pa[0], (float) pa[1], 0);
		dt.Radius = (float) radius;
		double angle= Math.atan2(ortho[1], ortho[0]);
		float deg= (float)(angle*180/Math.PI);
		dt.StartAngle = deg-180;
		dt.EndAngle = deg;
		wt.addArc(dt);
		
		dt.Point = new DXFPoint((float) pb[0], (float) pb[1], 0);
		dt.Radius = (float) radius;
		dt.StartAngle = deg;
		dt.EndAngle = deg-180;
		wt.addArc(dt);
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
