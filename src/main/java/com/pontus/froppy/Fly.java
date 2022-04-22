package com.pontus.froppy;

import java.io.IOException;

/**
 * Created by gabriel yibirin on 6/14/2016.
 */
 public class Fly extends TileObject {


    private int step = 10;
    private int heading;

    public Fly(int index, float x, float y, float width, float height) {
        super(index, x, y, width, height);
    }


    /*
      Heading will have values of
      1 = right45
      2 = right
      3 = right135
      4 = left135
      5 = left
      6 = left45
       */
    public void setHeading(int heading)throws IOException{

        if (heading<1 || heading>6 ){
            throw new IOException("Heading should have values (1-6)");
        }
        this.heading = heading;
        this.setIndex(heading+1);
    }

    public void move(){
        switch (heading){
            case 1:
                x = x + step;
                y = y + step;
                break;
            case 2:
                x = x + step;
                break;
            case 3:
                x = x + step;
                y = y - step;
                break;
            case 4:
                x = x - step;
                y = y - step;
                break;
            case 5:
                x = x - step;
                break;
            case 6:
                x = x - step;
                y = y + step;
                break;
            default:
                //do nothing
                break;

        }
    }

    public void setStep(int step){
        this.step = step;
    }

    public int getHeading(){
        return heading;
    }

}
