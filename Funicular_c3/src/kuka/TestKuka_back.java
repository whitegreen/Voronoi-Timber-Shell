package kuka;

import java.text.DecimalFormat;
import fabrication.Pavilion;
import fabrication.Timber;
import funicular.M;
import funicular.Mesh_Agents_Voronoi;
import funicular.MiniDist;
import funicular.Optimizer_Spring;
import processing.core.PApplet;

public class TestKuka_back extends PApplet {
	private final DecimalFormat df = new DecimalFormat("###.#");
	private Mesh_Agents_Voronoi mesh;
	private Pavilion pav;
	private int tid =121; // ******
	private Timber tim;
	private final double toolR = 4;
	private final double margin = 8.0;
	private double[] global_shift;
	private final float fast = 0.2f; // 30%
	private final float mid = 0.007f; // 30%
	private final float slow = 0.0025f; // 30%
	
	private KRLwriter wt;
	private double[][] back_cylinder = new double[4][]; // left
	private double[][] back_hex = new double[6][]; // right
	private final double cylinder_thick = 7; // phi8
	private final double hex_thick = 7;
	private final double cylinder_radius = 0.2 + 0.5 * 13 - 0.5 * 3; // +0.15
	private final double hex_radius = 0.2 + 0.5 * 15 - 0.5 * 3;// +0.15

	public void setup() {
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

		tim = pav.tims[tid];
		tim.local();
		double dist = M.dist(tim._p0, tim._p1);
		double base_shift =0.5 * dist - 233;        //0.5 * dist - 233
		//println("memb "+ tim.ID+"  type"+ tim.type);
		println("base shift " + df.format(base_shift));
		global_shift = new double[] {-base_shift, Pavilion.hei / 2, -Pavilion.wid / 2 };// x is measured***************
		 //m36  110-base_shift,

		tim.toolPath(toolR, margin);
		println("tim len "+df.format(M.dist(tim._p0, tim._p1)));
		writeKRL("/Users/huahao/Desktop/Back" + tid + "_Jun28.src");
	}

	private void writeKRL(String filename) {
		wt = new KRLwriter(false, filename); // tool , base
		
		wt.println("tool_change_1_pick()");  //tool 4
		wt.println("PTP  XHOME2 ");  
        wt.TOOL(4);
        wt.BASE(1);
        backCorner();
        wt.println("PTP  XHOME2 ");  
    	wt.println("tool_change_1_put()");  
    	
		wt.println("tool_change_2_pick()");  //tool 5
		wt.println("PTP  XHOME2 ");  
        wt.TOOL(5);
		wt.BASE(1);
		backHole(); // dependent on m1 type
		wt.println("PTP  XHOME2 ");
		wt.println("tool_change_2_put()"); 
		
		if (2 > mesh.fixed[tim.m1]) {
			wt.println("tool_change_4_pick()"); // tool 7
			wt.println("PTP  XHOME2 ");
			wt.TOOL(7);
			wt.BASE(1);
			backDetial();
			wt.println("PTP  XHOME2 ");
			wt.println("tool_change_4_put()");
		}
		wt.close2();
	}
	
	private void backDetial(){
		double[] cnt = { tim._backhole[0], -0.5 * Pavilion.hei + cylinder_thick, tim._backhole[2] };
		back_cylinder[0] = M.add(cnt, new double[] { 0, 0, cylinder_radius });
		back_cylinder[1] = M.add(cnt, new double[] { cylinder_radius, 0, 0 });
		back_cylinder[2] = M.add(cnt, new double[] { 0, 0, -cylinder_radius });
		back_cylinder[3] = M.add(cnt, new double[] { -cylinder_radius, 0, 0 });

		cnt = new double[] { tim._backhole[0], 0.5 * Pavilion.hei - hex_thick, tim._backhole[2] };
		for (int i = 0; i < 6; i++) {
			double a = i * 2 * PI / 6.0;
			double[] v = { hex_radius * Math.sin(a), 0, hex_radius * Math.cos(a) };
			back_hex[i] = M.add(cnt, v);
		}
		
		wt.println("$APO.CDIS=0.1"); // ****************************
		wt.println("$APO.CPTP = 1");
		wt.println("PULSE($OUT[1], TRUE,0.3)");
		wt.println("$OUT[4]=TRUE");

		wt.VEL(fast);
		wt.println("PTP {A1 100, A2 -90, A3 90, A4 0, A5 0, A6 0} C_PTP");
		readypos(2);
		readypos(7);
		readypos(1); // left 7+1, right 8+3

		double[] abc = LA.ABCby2Axis(Timber.upy, Timber.downz);
		double[] readyp = M.add(back_cylinder[0], new double[] { 0, -20, 0 });
		wt.LIN_CDIS(M.add(readyp, global_shift), abc);
		wt.VEL(mid);
		wt.LIN_CDIS(M.add(back_cylinder[0], global_shift), abc);
		wt.VEL(slow);
		wt.CIRC_CDIS(M.add(back_cylinder[1], global_shift), M.add(back_cylinder[2], global_shift));
		wt.CIRC_CDIS(M.add(back_cylinder[3], global_shift), M.add(back_cylinder[0], global_shift));
		wt.VEL(fast);
		wt.LIN_CDIS(M.add(readyp, global_shift), abc);

		readypos(1);
		readypos(7);
		readypos(2);
		readypos(8);
		readypos(3);

		abc = LA.ABCby2Axis(Timber.downy, Timber.upz);
		readyp = M.add(back_hex[0], new double[] { 0, 20, 0 });
		wt.LIN_CDIS(M.add(readyp, global_shift), abc);
		wt.VEL(mid);
		wt.LIN_CDIS(M.add(back_hex[0], global_shift), abc);
		wt.VEL(slow);
		for (int i = 1; i < back_hex.length; i++)
			wt.LIN_CDIS(M.add(back_hex[i], global_shift), abc);
		wt.LIN_CDIS(M.add(back_hex[0], global_shift), abc);
		wt.VEL(fast);
		wt.LIN_CDIS(M.add(readyp, global_shift), abc);

		wt.println("PULSE($OUT[3], TRUE,0.3)");
		wt.println("$OUT[4]=FALSE");
		readypos(3);
		readypos(8);
		readypos(2);
		wt.println("PTP {A1 100, A2 -90, A3 90, A4 0, A5 0, A6 0} C_PTP");
	}

	private void backHole(){
		wt.println("$APO.CDIS=0.2");
		wt.println("$APO.CPTP = 1");
		wt.println("PULSE($OUT[1], TRUE,0.3)");
		wt.println("$OUT[4]=TRUE");

		wt.VEL(fast);
		wt.println("PTP {A1 100, A2 -90, A3 90, A4 0, A5 0, A6 0} C_PTP");
		readypos(2);
		readypos(7);
		readypos(1); // left 7+1, right 8+3

		double[] abc, p;
		if (2 > mesh.fixed[tim.m1]) {
			abc = LA.ABCby2Axis(Timber.upy, Timber.downz);
			p = new double[] { tim._backhole[0], -(0.5 * Pavilion.hei + 10), tim._backhole[2] };
		} else {
			double[] holeOnFace = tim._uphole[1] < 0 ? tim._uphole : tim._dnhole;
			if (holeOnFace[1] > 0)
				throw new RuntimeException();
			double[] dir = M.sub(holeOnFace, tim._backhole);// outward
			abc = LA.ABCby2Axis(M.scale(-1, dir), Timber.downz); // inward
			p = M.add(holeOnFace, M.scaleTo(25, dir)); //outward,  larger offset
			println("******3456");
		}
		wt.LIN_CDIS(M.add(p, global_shift), abc);
		wt.VEL(2*slow); //*******
		wt.LIN_CDIS(M.add(tim._backhole, global_shift), abc);
		wt.VEL(fast/2);
		wt.LIN_CDIS(M.add(p, global_shift), abc);

		readypos(1);
		readypos(7);
		readypos(2);
		readypos(8);
		readypos(3);

		if (2 > mesh.fixed[tim.m1]) {
			abc = LA.ABCby2Axis(Timber.downy, Timber.upz);
			p = new double[] { tim._backhole[0], 0.5 * Pavilion.hei + 10, tim._backhole[2] };
		} else {
			double[] holeOnFace = tim._uphole[1] > 0 ? tim._uphole : tim._dnhole;
			if (holeOnFace[1] < 0)
				throw new RuntimeException();
			double[] dir = M.sub(holeOnFace, tim._backhole);// outward
			abc = LA.ABCby2Axis(M.scale(-1, dir), Timber.upz); // inward***
			p = M.add(holeOnFace, M.scaleTo(25, dir)); //outward
			println("******3456");
		}
		wt.LIN_CDIS(M.add(p, global_shift), abc);
		wt.VEL(2*slow);  //*******
		wt.LIN_CDIS(M.add(tim._backhole, global_shift), abc);
		wt.VEL(fast/2);
		wt.LIN_CDIS(M.add(p, global_shift), abc);

		wt.println("PULSE($OUT[3], TRUE,0.3)");
		wt.println("$OUT[4]=FALSE");
		readypos(3);
		readypos(8);
		readypos(2);
		wt.println("PTP {A1 100, A2 -90, A3 90, A4 0, A5 0, A6 0} C_PTP");
	}
	
	private void backCorner(){
		wt.println("$APO.CDIS=0.2");
		wt.println("$APO.CPTP = 1");
		wt.println("PULSE($OUT[1], TRUE,0.3)");
		wt.println("$OUT[4]=TRUE");

		wt.VEL(fast);
		wt.println("PTP {A1 100, A2 -90, A3 90, A4 0, A5 0, A6 0} C_PTP");
		readypos(2);
		readypos(7);
		readypos(1); // left 7+1

		double[] abc = tim._backleftABC;// ******
		double[][] curve ;
		if(0< mesh.fixed[tim.m1]  )
			curve=new double[][]{tim._backts_hale[0],tim._backts_hale[3]};
		else
			curve= tim._backts_hale; // ***half left tim._backts_hale
		double[] p = curve[0];
		wt.LIN_CDIS(M.add(p, global_shift), abc);
		wt.VEL(slow);
		for (int i = 1; i < curve.length; i++) {
			p = curve[i];
			wt.LIN_CDIS(M.add(p, global_shift), abc);
		}
		
		if(0< mesh.fixed[tim.m1]  )
			curve =new double[][]{tim._backts[0],tim._backts[3]};
		else
		curve = tim._backts;
		p = curve[curve.length - 1];
		wt.VEL(mid);
		wt.LIN_CDIS(M.add(p, global_shift), abc);
		wt.VEL(slow);
		for (int i = curve.length - 2; i >= 0; i--) {
			p = curve[i];
			wt.LIN_CDIS(M.add(p, global_shift), abc);
		}
		wt.VEL(fast);
		double[] endp = M.add(curve[0], new double[] { 0, 0, 150 });
		wt.LIN_CDIS(M.add(endp, global_shift), abc);

		readypos(2);
		readypos(8);
		readypos(3); // right 8+3

		abc = tim._backrightABC; // ********
		if(0< mesh.fixed[tim.m1]  )
			curve= new double[][]{ tim._backts_hari[0], tim._backts_hari[3]};
		else
		curve = tim._backts_hari; // _backts_hari
		p = curve[0];
		wt.LIN_CDIS(M.add(p, global_shift), abc);// ******
		wt.VEL(slow);
		for (int i = 1; i < curve.length; i++) {
			p = curve[i];
			wt.LIN_CDIS(M.add(p, global_shift), abc);// ******
		}
		if(0< mesh.fixed[tim.m1]  )
			curve =new double[][]{tim._backts[0],tim._backts[3]};
		else
		curve = tim._backts;
		p = curve[curve.length - 1];
		wt.VEL(mid);
		wt.LIN_CDIS(M.add(p, global_shift), abc);// ******
		wt.VEL(slow);
		for (int i = curve.length - 2; i >= 0; i--) {
			p = curve[i];
			wt.LIN_CDIS(M.add(p, global_shift), abc);// ******
		}
		wt.VEL(fast);
		endp = M.add(curve[0], new double[] { 0, 0, 150 });
		wt.LIN_CDIS(M.add(endp, global_shift), abc);

		wt.println("PTP {A1 100, A2 -90, A3 90, A4 0, A5 0, A6 0} C_PTP");
		readypos(6); // hexa cut

		abc = tim._backcutABC;// ******
		p = tim._backcutpb_half;
		double[] upp = M.add(p, new double[] { 0, 0, 50 });
		wt.LIN_CDIS(M.add(upp, global_shift), abc);

		wt.LIN_CDIS(M.add(p, global_shift), abc);
		wt.VEL(slow);
		p = tim._backcutpa_half;
		wt.LIN_CDIS(M.add(p, global_shift), abc);

		wt.VEL(mid);
		p = tim._backcutpa;
		wt.LIN_CDIS(M.add(p, global_shift), abc);
		wt.VEL(slow);
		p = tim._backcutpb;
		wt.LIN_CDIS(M.add(p, global_shift), abc);

		wt.VEL(fast);
		wt.println("PULSE($OUT[3], TRUE,0.3)");
		wt.println("$OUT[4]=FALSE");
		upp = M.add(p, new double[] { 0, 0, 50 });
		wt.LIN_CDIS(M.add(upp, global_shift), abc);
		readypos(6);
		wt.println("PTP {A1 100, A2 -90, A3 90, A4 0, A5 0, A6 0} C_PTP");
	}

	private void readypos(int i) {
		switch (i) {
		case 0:
			wt.println("PTP {A1 83, A2 -51, A3 104, A4 -15, A5 -54, A6 99} C_PTP");
			break;
		case 1:
			wt.println("PTP {A1 82.78, A2 -56.26, A3 102.83, A4 -16.37, A5 -47.63, A6 100.70} C_PTP");
			break;
		case 2:
			wt.println("PTP {A1 100, A2 -74, A3 98, A4 9, A5 -23, A6 -7} C_PTP");
			break;
		case 3:
			wt.println("PTP {A1 115.37, A2 -53.53, A3 97.47, A4 26.75, A5 -47.15, A6 -107.21} C_PTP");
			break;
		case 4:
			wt.println("PTP {A1 115, A2 -50, A3 98, A4 25, A5 -51, A6 -105} C_PTP");
			break;
		case 5:
			wt.println("PTP {A1 101, A2 -71, A3 113, A4 0, A5 49, A6 189} C_PTP");
			break;
		case 6:
			wt.println("PTP {A1 101, A2 -77, A3 111, A4 0, A5 58, A6 189} C_PTP");
			break;
		case 7:
			wt.println("PTP {A1 86, A2 -67, A3 101, A4 -16, A5 -35, A6 62} C_PTP");
			break;
		case 8:
			wt.println("PTP {A1 112, A2 -66, A3 98, A4 28, A5 -35, A6 -69} C_PTP");
			break;
		}
	}
	// 1, wt.println("PTP {A1 83, A2 -56, A3 103, A4 -16, A5 -48, A6 101} C_PTP");
	// 2, wt.println("PTP {A1 100, A2 -72, A3 100, A4 8, A5 -27, A6 -6} C_PTP");
	// 3wt.println("PTP {A1 115, A2 -53, A3 98, A4 26, A5 -48, A6 -107} C_PTP");

}
