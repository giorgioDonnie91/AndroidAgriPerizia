package org.droidplanner.android.KDTree;

import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

public class LocationKDTree {
    private static final int K = 3; // 3-d tree
    private final Node tree;

    public LocationKDTree(final List<Waypoint> locations) {
        final List<Node> nodes = new ArrayList<>(locations.size());
        int i=1;
        for (final Waypoint waypoint : locations) {
            nodes.add(new Node(waypoint, i++));
        }
        tree = buildTree(nodes, 0);
    }

    public Node findNearest(final double latitude, final double longitude) {
        return findNearest(tree, new Node(latitude, longitude, -1), 0);
    }

    private static Node findNearest(final Node current, final Node target, final int depth) {
        final int axis = depth % K;
        final int direction = getComparator(axis).compare(target, current);
        final Node next = (direction < 0) ? current.left : current.right;
        final Node other = (direction < 0) ? current.right : current.left;
        Node best = (next == null) ? current : findNearest(next, target, depth + 1);
        if (current.euclideanDistance(target) < best.euclideanDistance(target)) {
            best = current;
        }
        if (other != null) {
            if (current.verticalDistance(target, axis) < best.euclideanDistance(target)) {
                final Node possibleBest = findNearest(other, target, depth + 1);
                if (possibleBest.euclideanDistance(target) < best.euclideanDistance(target)) {
                    best = possibleBest;
                }
            }
        }
        return best;
    }

    private static Node buildTree(final List<Node> items, final int depth) {
        if (items.isEmpty()) {
            return null;
        }

        Collections.sort(items, getComparator(depth % K));
        final int index = items.size() / 2;
        final Node root = items.get(index);
        root.left = buildTree(items.subList(0, index), depth + 1);
        root.right = buildTree(items.subList(index + 1, items.size()), depth + 1);
        return root;
    }

    public static class Node {
        Node left;
        Node right;
        Waypoint location;
        int waypointIndex;
        final double[] point = new double[K];

        public int getWaypointIndex() {
            return waypointIndex;
        }

        public LatLong getCoordinates(){
            return location.getCoordinate();
        }

        Node(final double latitude, final double longitude, int wpIndex) {
            point[0] = cos(toRadians(latitude)) * cos(toRadians(longitude));
            point[1] = cos(toRadians(latitude)) * sin(toRadians(longitude));
            point[2] = sin(toRadians(latitude));
            waypointIndex = wpIndex;
        }

        Node(final Waypoint location, int wpIndex) {
            this(location.getCoordinate().getLatitude(), location.getCoordinate().getLongitude(), wpIndex);
            this.location = location;
        }

        double euclideanDistance(final Node that) {
            final double x = this.point[0] - that.point[0];
            final double y = this.point[1] - that.point[1];
            final double z = this.point[2] - that.point[2];
            return x * x + y * y + z * z;
        }

        double verticalDistance(final Node that, final int axis) {
            final double d = this.point[axis] - that.point[axis];
            return d * d;
        }
    }

    private static Comparator<Node> getComparator(final int i) {
        return NodeComparator.values()[i];
    }

    private enum NodeComparator implements Comparator<Node> {
        x {
            @Override
            public int compare(final Node a, final Node b) {
                return Double.compare(a.point[0], b.point[0]);
            }
        },
        y {
            @Override
            public int compare(final Node a, final Node b) {
                return Double.compare(a.point[1], b.point[1]);
            }
        },
        z {
            @Override
            public int compare(final Node a, final Node b) {
                return Double.compare(a.point[2], b.point[2]);
            }
        }
    }

    public static void main(String args[]) throws IOException {

        ArrayList<LatLng> list = new ArrayList<>();
        /*
        list.add(new LatLng(7.6391296386719, 45.130867004395));
        list.add(new LatLng(7.6389619827271, 45.131087493896));
        list.add(new LatLng(7.6387943267822, 45.131307983398));
        list.add(new LatLng(7.6386266708374, 45.1315284729));
        list.add(new LatLng(7.6384590148926, 45.131748962402));
        list.add(new LatLng(7.6382913589478, 45.131969451904));
        list.add(new LatLng(7.6388618946075, 45.130737304688));
        list.add(new LatLng(7.6387059211731, 45.130970001221));
        list.add(new LatLng(7.6385499477386, 45.131202697754));
        list.add(new LatLng(7.6383939743042, 45.131435394287));
        list.add(new LatLng(7.6382380008698, 45.13166809082));
        list.add(new LatLng(7.6380820274353, 45.131900787354));
        list.add(new LatLng(7.6385941505432, 45.13060760498));
        list.add(new LatLng(7.6384498596191, 45.130852508545));
        list.add(new LatLng(7.6383055686951, 45.131097412109));
        list.add(new LatLng(7.638161277771, 45.131342315674));
        list.add(new LatLng(7.6380169868469, 45.131587219238));
        list.add(new LatLng(7.6378726959229, 45.131832122803));

        LocationKDTree kdt = new LocationKDTree(list);

        while(true) {
            System.out.println("Enter the co-ordinates of the point: (one after the other)");
            InputStreamReader reader = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(reader);
            double sx = Double.parseDouble(br.readLine());
            double sy = Double.parseDouble(br.readLine());

            Node kdn = kdt.findNearest(sx, sy);
            System.out.println("The nearest neighbor is: " + kdn.waypointIndex);
            //System.out.println("The distance is: " + WaypointUtils.distanza(sy, sx, kdn.x[1], kdn.x[0]));
        }
        */
    }

}
