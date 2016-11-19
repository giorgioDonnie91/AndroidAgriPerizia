package org.droidplanner.android.KDTree;

public class KDNode {
    int axis;
    double[] x;
    int id;
    boolean checked;
    boolean orientation;
    int waypointIndex;

    KDNode Parent;
    KDNode Left;
    KDNode Right;

    public KDNode(double[] x0, int wpIndex, int axis0){
        x = new double[2];
        for (int k = 0; k < 2; k++)
            x[k] = x0[k];

        axis = axis0;
        waypointIndex = wpIndex;
        Left = Right = Parent = null;
        checked = false;
        id = 0;
    }

    public KDNode findParent(double[] x0){
        KDNode parent = null;
        KDNode next = this;
        int split;
        while (next != null){
            split = next.axis;
            parent = next;
            if (x0[split] > next.x[split])
                next = next.Right;
            else
                next = next.Left;
        }
        return parent;
    }

    public KDNode insert(double[] p, int wpIndex){
        KDNode parent = findParent(p);
        if (equal(p, parent.x))
            return null;

        KDNode newNode = new KDNode(p, wpIndex, parent.axis + 1 < 2 ? parent.axis + 1 : 0);
        newNode.Parent = parent;

        if (p[parent.axis] > parent.x[parent.axis]){
            parent.Right = newNode;
            newNode.orientation = true;
        } else {
            parent.Left = newNode;
            newNode.orientation = false;
        }

        return newNode;
    }

    static boolean equal(double[] x1, double[] x2){
        for (int k = 0; k < 2; k++){
            if (x1[k] != x2[k])
                return false;
        }
        return true;
    }

    static double distance(double[] x1, double[] x2){
        double s = 0;
        for (int k=0; k<2; k++){
            double delta = (x1[k] - x2[k]);
            s += delta * delta;
        }
        return s;
    }

    public int getWaypointIndex() {
        return waypointIndex;
    }

    public double[] getCoordinates() {
        return x;
    }
}