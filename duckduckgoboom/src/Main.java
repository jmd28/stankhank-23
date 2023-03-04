import processing.core.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Main extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Main");
    }


    final int NUM_DUCKS = 5;
    final int NUM_BULLETS = 5;

    static final int MY_HEIGHT = 1000;
    static final int MY_WIDTH = 1600;
    final int DUCK_X_SIZE = MY_WIDTH / 40;
    final int DUCK_Y_SIZE = MY_HEIGHT / 30;

    final int BULLET_X_SIZE = MY_WIDTH / 90;
    final int BULLET_Y_SIZE = MY_HEIGHT / 90;


    final int BORDER_SIZE = MY_HEIGHT / 10;
    final int BORDER_OFFSET_X = MY_WIDTH / 100;
    final int BORDER_OFFSET_Y = BORDER_OFFSET_X;

    long last_check;
    final float INTERVAL = 20f;



    public void settings() {
        size(MY_WIDTH, MY_HEIGHT);
    }

    ArrayList<BoundingBox> borders = new ArrayList<>();
    ArrayList<BoundingBox> bullets = new ArrayList<>();
    ArrayList<BoundingBox> ducks = new ArrayList<>();
    ArrayList<BoundingBox> boxes = new ArrayList<>();


    ArrayList<PImage> images = new ArrayList<>();
    ArrayList<ArrayList<String>> collisions = new ArrayList<>();

    int counter = 0;
    Random rand = new Random();

    Yolo yolo = new Yolo();

    public int getRandX() {
        return rand.nextInt(MY_WIDTH-DUCK_X_SIZE);
    }

    public int getRandY() {
        return rand.nextInt(MY_HEIGHT-DUCK_Y_SIZE);
    }
    public void setup() {
        last_check = System.currentTimeMillis();

        // Cursed
        BoundingBox top_wall = new BoundingBox(0,-BORDER_OFFSET_Y, MY_WIDTH, BORDER_SIZE, true);
        BoundingBox bottom_wall = new BoundingBox(0,MY_HEIGHT-BORDER_SIZE+BORDER_OFFSET_Y, MY_WIDTH, BORDER_SIZE, true);
//        BoundingBox left_wall = new BoundingBox(-BORDER_OFFSET_X,0, BORDER_SIZE, MY_HEIGHT, true);
        BoundingBox left_wall = new BoundingBox(-BORDER_OFFSET_X,BORDER_SIZE-BORDER_OFFSET_Y, BORDER_SIZE, MY_HEIGHT-BORDER_SIZE+BORDER_OFFSET_Y-(BORDER_SIZE-BORDER_OFFSET_Y), true);
        BoundingBox right_wall = new BoundingBox(MY_WIDTH-BORDER_SIZE+BORDER_OFFSET_X,BORDER_SIZE-BORDER_OFFSET_Y, BORDER_SIZE, MY_HEIGHT-BORDER_SIZE+BORDER_OFFSET_Y-(BORDER_SIZE-BORDER_OFFSET_Y), true);
//        BoundingBox right_wall = new BoundingBox(MY_WIDTH-BORDER_SIZE+BORDER_OFFSET_X,0, BORDER_SIZE, MY_HEIGHT, true);

        borders.add(top_wall);
        borders.add(bottom_wall);
        borders.add(left_wall);
        borders.add(right_wall);

        addBoxes();
//        compute();
    }

    public void addBoxes() {
        for(int i = 0 ; i < NUM_DUCKS ; i++) {
            ducks.add(new BoundingBox(getRandX(), getRandY(), DUCK_X_SIZE, DUCK_Y_SIZE, false));
        }

        for(int i = 0 ; i < NUM_BULLETS ; i++) {
            bullets.add(new BoundingBox(getRandX(), getRandY(), BULLET_X_SIZE, BULLET_Y_SIZE, false));
        }

        boxes.addAll(borders);
        boxes.addAll(ducks);
        boxes.addAll(bullets);
    }


    public void compute() {
        ArrayList<String> yoloCollisions = new ArrayList<>();

        for(BoundingBox b : boxes) {
            b.resetColor();
        }

        for(int i = 0 ; i < boxes.size()-1 ; i++) {
            for(int j = i+1 ; j < boxes.size(); j++) {
                BoundingBox a = boxes.get(i);
                BoundingBox b = boxes.get(j);
                if(a.checkCollision(b)) {
                    int minX ;
                    int minY;
                    int maxX;
                    int maxY;
                    minX = Math.min(a.x_pos, b.x_pos);
                    minX = Math.max(0, minX);
                    minY = Math.min(a.y_pos, b.y_pos);
                    minY = Math.max(0, minY);
                    maxX = Math.max(a.x_pos+a.width, b.x_pos+b.width);
                    maxX = Math.min(maxX, MY_WIDTH);
                    maxY = Math.max(a.y_pos+a.height, b.y_pos+b.height);
                    maxY = Math.min(maxY, MY_HEIGHT);

                    yoloCollisions.add(yolo.getLine(new CollisionBox(minX, minY, maxX, maxY)));
                }
            }
        }

        collisions.add(yoloCollisions);
    }

    final int padding = 2;

    public void render() {
        background(255);
        strokeWeight(1);
        stroke(255,0,0);
        for(BoundingBox box : boxes) {
//            fill(box.color);
            fill(0);
//            rect(box.x_pos, box.y_pos, box.width, box.height);

            beginShape();
            vertex(box.x_pos, box.y_pos);
            vertex(box.x_pos+box.width, box.y_pos);
            vertex(box.x_pos+box.width, box.y_pos+box.height);
            vertex(box.x_pos, box.y_pos+box.height);
            stroke(0);
            beginContour();
            vertex(box.x_pos+padding, box.y_pos+padding);
            vertex(box.x_pos+padding, box.y_pos+box.height-padding);
            vertex(box.x_pos+box.width-padding, box.y_pos+box.height-padding);
            vertex(box.x_pos+box.width-padding, box.y_pos+padding);
            endContour();
            endShape(CLOSE);
            stroke(255,0,0);
        }
    }
    public void draw() {
        // DEBUGGING
//        ArrayList<String> yolo = collisions.get(collisions.size()-1);
//        for(String s : yolo) {
//            String[] split = s.split(" ");
//            fill(125, 100, 25);
//            circle(parseFloat(split[1])*MY_WIDTH, parseFloat(split[2])*MY_HEIGHT, 10);
//        }

        if(abs(System.currentTimeMillis() - last_check) > INTERVAL) {
            boxes.clear();
            ducks.clear();
            bullets.clear();
            addBoxes();
            compute();
            render();
            images.add(get());
            writeData();
            last_check = System.currentTimeMillis();
            System.out.println(counter);
        }
    }

    public void writeData() {
        ArrayList<String> objs = collisions.get(collisions.size()-1);
        collisions.remove(collisions.size()-1);
        PImage img = images.get(images.size()-1);
        images.remove(images.size()-1);
        int index = counter++;
        try{
            BufferedWriter write = new BufferedWriter(new FileWriter("../data/" + index + ".txt"));
            for(String s : objs) {
                write.write(s + "\n");
            }
            write.close();
        } catch(IOException e) {
            System.out.println(e + "error");
        }
        img.save("../data/" + index + ".jpg");
        if(index == 1500) {
            System.exit(0);
        }
    }

    public void keyPressed() {

    }

    public void keyReleased() {

    }



}
