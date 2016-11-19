package org.droidplanner.android.KDTree;


import org.droidplanner.android.WaypointUtils;

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
        KDTree kdt = new KDTree(20);

        kdt.add(new double[]{7.6391296386719, 45.130867004395}, 1);
        kdt.add(new double[]{7.6389619827271, 45.131087493896}, 2);
        kdt.add(new double[]{7.6387943267822, 45.131307983398}, 3);
        kdt.add(new double[]{7.6386266708374, 45.1315284729}, 4);
        kdt.add(new double[]{7.6384590148926, 45.131748962402}, 5);
        kdt.add(new double[]{7.6382913589478, 45.131969451904}, 6);
        kdt.add(new double[]{7.6388618946075, 45.130737304688}, 7);
        kdt.add(new double[]{7.6387059211731, 45.130970001221}, 8);
        kdt.add(new double[]{7.6385499477386, 45.131202697754}, 9);
        kdt.add(new double[]{7.6383939743042, 45.131435394287}, 10);
        kdt.add(new double[]{7.6382380008698, 45.13166809082}, 11);
        kdt.add(new double[]{7.6380820274353, 45.131900787354}, 12);
        kdt.add(new double[]{7.6385941505432, 45.13060760498}, 13);
        kdt.add(new double[]{7.6384498596191, 45.130852508545}, 14);
        kdt.add(new double[]{7.6383055686951, 45.131097412109}, 15);
        kdt.add(new double[]{7.638161277771, 45.131342315674}, 16);
        kdt.add(new double[]{7.6380169868469, 45.131587219238}, 17);
        kdt.add(new double[]{7.6378726959229, 45.131832122803}, 18);

        while(true) {
            System.out.println("Enter the co-ordinates of the point: (one after the other)");
            InputStreamReader reader = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(reader);
            double sx = Double.parseDouble(br.readLine());
            double sy = Double.parseDouble(br.readLine());

            double s[] = {sx, sy};
            KDNode kdn = kdt.findNearest(s);
            System.out.println("The nearest neighbor is: " + kdn.getWaypointIndex());
            //System.out.println("The distance is: " + WaypointUtils.distanza(sy, sx, kdn.x[1], kdn.x[0]));
        }
    }

}