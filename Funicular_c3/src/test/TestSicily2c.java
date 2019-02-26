package test;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import dxfExporter.Constants;
import dxfExporter.DXFData;
import dxfExporter.DXFExport;
import dxfExporter.DXFLayer;
import dxfExporter.DXFPoint;
import fabrication.Pavilion;
import fabrication.Sicily;
import funicular.M;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import funicular.Optimizer_Spring;
import processing.core.PApplet;

public class TestSicily2c extends PApplet {
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

		int col = 11;
		for (int i = 0; i < pav.sicis.size(); i++) { // /*********************
			int ox = 50 + xx * (i % col);
			int oy = 50 + yy * (i / col);
			save(ox, -oy, wt, dt,  pav.sicis.get(i));  //***************
		}
		try {
			wt.saveToFile("/Users/huahao/Desktop/Upsilon_Sicily_Jun4.dxf");
		} catch (Exception excpt) {
		} finally {
			wt.finalize();
			println("dxf saved");
		}
	}

	private void save(float cx, float cy, DXFExport wt, DXFData dt, Sicily sici) {
		//Sicily sici = pav.sicis[id];
		double wid_hole=3.5;
		int[] nodeEdges =mesh.nodeEdges[sici.nodeId];
		polyC2D(cx,cy,wt, dt, sici.ps); 
		double[][] ps = sici.holes;
		for (int i = 0; i < 3; i++) {
			double[] v = sici.dir2ds[i];
			double[] d = { v[1], -v[0] };
			double[] p = ps[i];
			rect_arc(cx,cy,wt, dt, wid_hole, p, 0.5 * Pavilion.hole_diameter, d); // *** hole

			int eid = nodeEdges[i]; // 3 digits
			double[][][] arr = CADecimal.digits("" + eid,  -4f, -4.5f);

			arr = transform(p, Math.atan2(d[1], d[0]), 6, arr); // *** edge id
			for (double[][] poly : arr)
				polyC2D(cx,cy,wt, dt, poly);
		}

		double[][][] arr = CADecimal.digits("" + sici.nodeId,  -11, -1.5); // *** node id
		arr = transform(ps[0], 0, 6, arr);
		for (double[][] poly : arr)
			polyC2D(cx,cy,wt, dt, poly);
	}

	public void draw() {
		background(255);

		noFill();
		ArrayList<Sicily> sicis = pav.sicis;
		int col = 11;
		for (int i = 0; i < sicis.size(); i++) { // /*********************
			Sicily sici = sicis.get(i);
			int ox = 50 + xx * (i % col);
			int oy = 50 + yy * (i / col);
			pushMatrix();
			translate(ox, oy);
			draw(sici);
			popMatrix();
		}
	}

	private void draw(Sicily sici) {
		noFill();
		stroke(225, 0, 0);
		beginShape();
		for (double[] p : sici.ps)
			vertex(p);
		endShape(CLOSE);
		for (double[][] arr : sici.reduce_offset) {
			beginShape();
			for (double[] p : arr)
				vertex(p);
			endShape(CLOSE);
		}

		double[][] ps = sici.holes;
		int[] nodeEdges = pav.mesh.nodeEdges[sici.nodeId];
		fill(0, 0, 255);
		text("" + sici.nodeId, 0, 0);
		for (int i = 0; i < 3; i++) {
			double[] p = ps[i];
			float x = (float) (sc * p[0]);
			float y = (float) (-sc * p[1]);
			noFill();
			ellipse(x, y, sc * Pavilion.hole_diameter, sc * Pavilion.hole_diameter);
			fill(0);
			text("" + nodeEdges[i], x, y);

			double[] v = sici.dir2ds[i];
			line(M.scale(60, v));
			double[] d = { v[1], -v[0] };
			M._scale(40, d);
			line(p, M.add(p, d));
			// line(0,0, (float)( 50*v[0]), (float)(50*v[1]));
		}
	}

	private void vertex(double[] p) {
		vertex((float) (sc * p[0]), (float) (-sc * p[1]));
	}

	private void line(double[] p) {
		line(0, 0, (float) (sc * p[0]), (float) (-sc * p[1]));
	}

	private void line(double[] pa, double[] pb) {
		line((float) (sc * pa[0]), (float) (-sc * pa[1]), (float) (sc * pb[0]), (float) (-sc * pb[1]));
	}

	private void println(double[] v) {
		println(df.format(v[0]) + "," + df.format(v[1]) + "," + df.format(v[2]));
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
