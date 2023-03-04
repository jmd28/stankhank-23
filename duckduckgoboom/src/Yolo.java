public class Yolo {
    // <object-class> <x> <y> <width> <height>
    // x, y is CENTER!!
    // x y width height scaled 0-1 relative to image size

    public String getLine(CollisionBox b) {
        float width = b.xmax-b.xmin;
        float height = b.ymax-b.ymin;
        float center_x = width/2f + b.xmin;
        float center_y = height/2f + b.ymin;

        float relative_height = height/Main.MY_HEIGHT;
        float relative_width = width/Main.MY_WIDTH;
        float relative_center_x = center_x/Main.MY_WIDTH;
        float relative_center_y = center_y/Main.MY_HEIGHT;
        return(0 + " " + relative_center_x + " " + relative_center_y + " " + relative_width + " " + relative_height);
    }

}
