public class Edge {

    Point start;
    Point end;
    Point site_left;
    Point site_right;
    Edge neighbor;

    double slope;
    double yint;

    public Edge (Point first, Point left, Point right) {
        start = first;
        site_left = left;
        site_right = right;
        end = null;
        slope = (right.x - left.x)/(left.y - right.y);
        Point mid = new Point ((right.x + left.x)/2, (left.y+right.y)/2);
        yint = mid.y - slope*mid.x;
    }

    public String toString() {
        return start + ", " + end;
    }
}