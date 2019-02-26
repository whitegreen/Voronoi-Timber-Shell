package funicular;

import java.util.ArrayList;

public abstract class Mesh {
	public int nodesize;
	public double[][] nodes;
	public int membsize;
	public int[][] members;
	public int[][] nodeEdges;
	public int[] fixed; // 0: unfixed, 1:sink1, 2:sink 3(along x), 4(along x), 5, 6 ceiling
	public int fixedsize;

	public boolean fixed(int id) {
		return 0 < fixed[id];
	}

	public void nodeEdges(){
		ArrayList<Integer>[] lists = new ArrayList[nodesize];
		for (int i = 0; i < nodesize; i++)
			lists[i] = new ArrayList<Integer>();
		for (int i = 0; i < membsize; i++) {
			int[] member = members[i];
			int a = member[0];
			int b = member[1];
			lists[a].add(i);
			lists[b].add(i);
		}
		nodeEdges = new int[nodesize][];
		for (int i = 0; i < nodesize; i++) {
			int size = lists[i].size();
			int[] arr = new int[size];
			for (int j = 0; j < size; j++)
				arr[j] = lists[i].get(j);
			nodeEdges[i] = arr;
		}
	}
}
