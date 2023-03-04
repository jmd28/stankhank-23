public class BoundingBox {
    int x_pos;
    int y_pos;
    int width;
    int height;
    boolean is_wall;

    int color = 0;

    public BoundingBox(int x_position, int y_position, int x_size, int y_size, boolean is_wall) {
        this.x_pos = x_position;
        this.y_pos = y_position;
        this.width = x_size;
        this.height = y_size;
        this.is_wall = is_wall;
    }

    public void resetColor() {
        color = 0;
    }


    public boolean checkCollision(BoundingBox b) {
        if(b.is_wall && this.is_wall) return false;
        if(((this.x_pos < (b.x_pos + b.width)) && (this.x_pos + this.width) > b.x_pos) &&
                (this.y_pos < (b.y_pos + b.height) && ((this.y_pos + this.height) > b.y_pos))) {
            this.color = 125;
            b.color = 125;
            return true;
        }
        return false;
    }
}
