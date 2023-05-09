import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
public class Voronoy {

    private List<Point> sites;
    private List <Edge> edges;
    private PriorityQueue<Event> events;
    private Parabola root;

    private double width = 1;
    private double height = 1;

    private double ycurr;

    public Voronoy (List <Point> sites) {
        this.sites = sites;
        this.edges = new ArrayList<>();
        generateVoronoi();
    }

    public static void main(String[] args) {
        int N = 30;

        ArrayList<Point> points = new ArrayList<>();

        Random gen = new Random();

        for (int i = 0; i < N; i++){
            double x = gen.nextDouble();
            double y = gen.nextDouble();
            points.add(new Point(x, y));
        }

        Voronoy diagram = new Voronoy(points);

        // draw results
        StdDraw.setPenRadius(.01);
        for (Point p: points) {
            StdDraw.point(p.x, p.y);
        }
        StdDraw.setPenRadius(.002);
        for (Edge e: diagram.edges) {
            StdDraw.line(e.start.x, e.start.y, e.end.x, e.end.y);
        }
    }

    private void generateVoronoi() {

        events = new PriorityQueue <>();
        for (Point point : sites) {
            events.add(new Event(point, Event.SITE_EVENT));
        }

        while (!events.isEmpty()) {
            Event event = events.remove();
            ycurr = event.point.y;
            if (event.type == Event.SITE_EVENT) {
                handleSite(event.point);
            }
            else {
                handleCircle(event);
            }
        }

        ycurr = width+height;

        endEdges(root);

        for (Edge edge: edges){
            if (edge.neighbor != null) {
                edge.start = edge.neighbor.end;
                edge.neighbor = null;
            }
        }
    }

    private void endEdges(Parabola p) {
        if (p.type == Parabola.IS_FOCUS) {
            p = null;
            return;
        }

        double x = getXofEdge(p);
        p.edge.end = new Point (x, p.edge.slope*x+p.edge.yint);
        edges.add(p.edge);

        endEdges(p.child_left);
        endEdges(p.child_right);

        p = null;
    }

    private void handleSite(Point point) {
        if (root == null) {
            root = new Parabola(point);
            return;
        }
////we need to find the parabola that contains this point. This allows us to determine where to insert new parabolas and update the struct
        Parabola parabola = getParabolaByX(point.x);
        if (parabola.event != null) {
            events.remove(parabola.event);
            parabola.event = null;
        }

        Point start = new Point(point.x, getY(parabola.point, point.x));
        Edge edge_left = new Edge(start, parabola.point, point);
        Edge edge_right = new Edge(start, point, parabola.point);
        edge_left.neighbor = edge_right;
        edge_right.neighbor = edge_left;
        parabola.edge = edge_left;
        parabola.type = Parabola.IS_VERTEX;

        Parabola p0 = new Parabola (parabola.point);
        Parabola p1 = new Parabola (point);
        Parabola p2 = new Parabola (parabola.point);

        parabola.setLeftChild(p0);
        parabola.setRightChild(new Parabola());
        parabola.child_right.edge = edge_right;
        parabola.child_right.setLeftChild(p1);
        parabola.child_right.setRightChild(p2);

        checkCircleEvent(p0);
        checkCircleEvent(p2);
    }

    private void handleCircle(Event e) {
        Parabola p1 = e.arc;
        Parabola xl = Parabola.getLeftParent(p1);
        Parabola xr = Parabola.getRightParent(p1);
        Parabola p0 = Parabola.getLeftChild(xl);
        Parabola p2 = Parabola.getRightChild(xr);
        if (xr == null)
            System.out.println("NULL");
// // This checks are needed to ensure that there is only one circle event for each parabola in the events list, and that this event is always the newest and most current.
        if (p0.event != null) {
            events.remove(p0.event);
            p0.event = null;
        }
        if (p2.event != null) {
            events.remove(p2.event);
            p2.event = null;
        }
        //Create new vertex
        Point p = new Point(e.point.x, getY(p1.point, e.point.x));
//update information about the edges that are connected to the parabola
        xl.edge.end = p;
        xr.edge.end = p;
        edges.add(xl.edge);
        edges.add(xr.edge);
        //find a parabola that is above parabola p1 and update the corresponding edge of this parabola with new points
        Parabola higher = new Parabola();
        Parabola par = p1;
        while (par != root) {
            par = par.parent;
            if (par == xl) higher = xl;
            if (par == xr) higher = xr;
        }
        higher.edge = new Edge(p, p0.point, p2.point);
////we ensure that references to parent parabolas are correctly updated after deleting parabola p1.
        Parabola gparent = p1.parent.parent;
        if (p1.parent.child_left == p1) {
            if(gparent.child_left  == p1.parent) gparent.setLeftChild(p1.parent.child_right);
            if(gparent.child_right == p1.parent) gparent.setRightChild(p1.parent.child_right);
        }
        else {
            if(gparent.child_left  == p1.parent) gparent.setLeftChild(p1.parent.child_left);
            if(gparent.child_right == p1.parent) gparent.setRightChild(p1.parent.child_left);
        }

        p1.parent = null;
        p1 = null;

        checkCircleEvent(p0);
        checkCircleEvent(p2);
    }

    private void checkCircleEvent(Parabola b) {

        Parabola lp = Parabola.getLeftParent(b);
        Parabola rp = Parabola.getRightParent(b);

        if (lp == null || rp == null) return;

        Parabola a = Parabola.getLeftChild(lp);
        Parabola c = Parabola.getRightChild(rp);

        if (a == null || c == null || a.point == c.point) return;

        if (ccw(a.point,b.point,c.point) != 1) return;

        Point start = getEdgeIntersection(lp.edge, rp.edge);
        if (start == null) return;

        double dx = b.point.x - start.x;
        double dy = b.point.y - start.y;
        double d = Math.sqrt((dx*dx) + (dy*dy));
        if (start.y + d < ycurr) return;
        //new edge-intersection event’s y-coordinate is therefore just the y-coordinate of the intersection point, plus the distance between the focus of any of the three involved arcs and the intersection point.
        Point ep = new Point(start.x, start.y + d);

        Event e = new Event (ep, Event.CIRCLE_EVENT);
        e.arc = b;
        b.event = e;
        events.add(e);
    }

    public int ccw(Point a, Point b, Point c) {
        double area2 = (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x); //double area of a triangle
        if (area2 < 0) return -1;
        else if (area2 > 0) return 1; //points must be bypassed counterclockwise
        else return  0;
    }

    private Point getEdgeIntersection(Edge a, Edge b) {
//to intersect two lines (edges) at one point, their y-intersections must be different
        if (b.slope == a.slope && b.yint != a.yint) return null; // //these edges are parallel because they have the same slope

        double x = (b.yint - a.yint)/(a.slope - b.slope);
        double y = a.slope*x + a.yint;

        return new Point(x, y);
    }

    //calculate the X coordinate of the intersection point of the edge between two parabolas based on their parameters and coordinates
    private double getXofEdge (Parabola par) {
        Parabola left = Parabola.getLeftChild(par);
        Parabola right = Parabola.getRightChild(par);

        Point p = left.point;
        Point r = right.point;

        double dp = 2*(p.y - ycurr);
        double a1 = 1/dp;
        double b1 = -2*p.x/dp;
        double c1 = (p.x*p.x + p.y*p.y - ycurr*ycurr)/dp;

        double dp2 = 2*(r.y - ycurr);
        double a2 = 1/dp2;
        double b2 = -2*r.x/dp2;
        double c2 = (r.x*r.x + r.y*r.y - ycurr*ycurr)/dp2;

        double a = a1-a2;
        double b = b1-b2;
        double c = c1-c2;

        double disc = b*b - 4*a*c;
        double x1 = (-b + Math.sqrt(disc))/(2*a);
        double x2 = (-b - Math.sqrt(disc))/(2*a);

        double ry;
        if (p.y > r.y) ry = Math.max(x1, x2);
        else ry = Math.min(x1, x2);

        return ry;
    }

    //this function allows you to find a parabola in a Voronoi diagram at a given x-coordinate, by descending from the root along the parabola tree.
    private Parabola getParabolaByX (double xx) {
        Parabola par = root;
        double x = 0;
        while (par.type == Parabola.IS_VERTEX) {
            x = getXofEdge(par);
            if (x>xx) par = par.child_left;
            else par = par.child_right;
        }
        return par;
    }

    private double getY(Point p, double x) {
        double dp = 2*(p.y - ycurr);
        double a1 = 1/dp;
        double b1 = -2*p.x/dp;
        double c1 = (p.x * p.x + p.y*p.y - ycurr*ycurr)/dp;
        return (a1*x*x + b1*x + c1);
    }
}