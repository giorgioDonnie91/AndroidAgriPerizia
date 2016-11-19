package org.droidplanner.android.KDTree;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class KDTree{
    KDNode root;

    double d_min;
    KDNode nearest_neighbour;

    int KD_id;

    KDNode checkedNodes[];
    int checked_nodes;

    KDNode list[];
    int nList;

    double x_min[], x_max[];
    boolean max_boundary[], min_boundary[];
    int n_boundary;

    public KDTree(int i){
        root = null;
        KD_id = 1;
        nList = 0;
        list = new KDNode[i];
        checkedNodes = new KDNode[i];
        max_boundary = new boolean[2];
        min_boundary = new boolean[2];
        x_min = new double[2];
        x_max = new double[2];
    }

    public void add(double[] x, int wpIndex){
        if (root == null){
            root = new KDNode(x, wpIndex, 0);
            root.id = KD_id++;
            list[nList++] = root;
        } else {
            KDNode pNode;
            if ((pNode = root.insert(x, wpIndex)) != null){
                pNode.id = KD_id++;
                list[nList++] = pNode;
            }
        }
    }

    public KDNode findNearest(double[] x){
        if (root == null)
            return null;

        checked_nodes = 0;
        KDNode parent = root.findParent(x);
        nearest_neighbour = parent;
        d_min = KDNode.distance(x, parent.x);

        if (KDNode.equal(x, parent.x))
            return nearest_neighbour;

        searchParent(parent, x);
        uncheck();

        return nearest_neighbour;
    }

    public void checkSubtree(KDNode node, double[] x){
        if ((node == null) || node.checked)
            return;

        checkedNodes[checked_nodes++] = node;
        node.checked = true;
        setBoundingCube(node, x);

        int dim = node.axis;
        double d = node.x[dim] - x[dim];

        if (d * d > d_min){
            if (node.x[dim] > x[dim])
                checkSubtree(node.Left, x);
            else
                checkSubtree(node.Right, x);
        } else {
            checkSubtree(node.Left, x);
            checkSubtree(node.Right, x);
        }
    }

    public void setBoundingCube(KDNode node, double[] x){
        if (node == null)
            return;
        int d = 0;
        double dx;
        for (int k = 0; k < 2; k++){
            dx = node.x[k] - x[k];
            if (dx > 0){
                dx *= dx;
                if (!max_boundary[k]){
                    if (dx > x_max[k])
                        x_max[k] = dx;
                    if (x_max[k] > d_min){
                        max_boundary[k] = true;
                        n_boundary++;
                    }
                }
            } else {
                dx *= dx;
                if (!min_boundary[k]){
                    if (dx > x_min[k])
                        x_min[k] = dx;
                    if (x_min[k] > d_min){
                        min_boundary[k] = true;
                        n_boundary++;
                    }
                }
            }
            d += dx;
            if (d > d_min)
                return;

        }

        if (d < d_min){
            d_min = d;
            nearest_neighbour = node;
        }
    }

    public KDNode searchParent(KDNode parent, double[] x){
        for (int k = 0; k < 2; k++){
            x_min[k] = x_max[k] = 0;
            max_boundary[k] = min_boundary[k] = false; //
        }
        n_boundary = 0;

        KDNode search_root = parent;
        while (parent != null && (n_boundary != 2 * 2)){
            checkSubtree(parent, x);
            search_root = parent;
            parent = parent.Parent;
        }

        return search_root;
    }

    public void uncheck(){
        for (int n = 0; n < checked_nodes; n++)
            checkedNodes[n].checked = false;
    }

    public static void main(String args[]) throws IOException {
        int numPoints = 900;

        KDTree kdt = new KDTree(numPoints);
        double x[] = new double[2];

        long start = System.currentTimeMillis();

        int tot = 0;
        for(int i=0; i<30; i++){
            for(int j=0; j<30; j++){
                x[0] = i;
                x[1] = j;
                kdt.add(x, tot++);
            }
        }
        System.out.println("loading millisec: " + (System.currentTimeMillis() - start));

        System.out.println("Enter the co-ordinates of the point: (one after the other)");
        InputStreamReader reader = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(reader);
        double sx = Double.parseDouble(br.readLine());
        double sy = Double.parseDouble(br.readLine());

        double s[] = { sx, sy };
        KDNode kdn = kdt.findNearest(s);
        System.out.println("The nearest neighbor is: ");
        System.out.println("(" + kdn.x[0] + " , " + kdn.x[1] + ")");
    }

}