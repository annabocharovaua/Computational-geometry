public class Event implements Comparable <Event>{

    public static int SITE_EVENT = 0;
    public static int CIRCLE_EVENT = 1;

    Point point;
    int type;
    Parabola arc;

    public Event (Point p, int type) {
        this.point = p;
        this.type = type;
        arc = null;
    }

    public int compareTo(Event other) {
        return this.point.compareTo(other.point);
    }

}