package rbadia.voidspace.model;

import rbadia.voidspace.main.GameScreen;

import java.awt.Rectangle;
import java.util.Random;

/**
 * Represents a ship/space craft.
 *
 */
public class EnemyShip extends Rectangle {
    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_SPEED = 2 ;
    private static final int Y_OFFSET = 5; // initial y distance of the ship from the bottom of the screen

    private int enemyShipWidth = 25;
    private int enemyShipHeight = 25;
    private int speed = DEFAULT_SPEED;

    private Random rand = new Random();

    /**
     * Creates a new ship at the default initial location.
     * @param screen the game screen
     */
    public EnemyShip(GameScreen screen){
        this.setLocation(
                rand.nextInt(screen.getWidth() - enemyShipWidth),
                5);
        this.setSize(enemyShipWidth, enemyShipHeight);
    }

    /**
     * Get the default ship width
     * @return the default ship width
     */
    public int getShipWidth() {
        return enemyShipWidth;
    }

    /**
     * Get the default ship height
     * @return the default ship height
     */
    public int getShipHeight() {
        return enemyShipHeight;
    }

    /**
     * Returns the current ship speed
     * @return the current ship speed
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Set the current ship speed
     * @param speed the speed to set
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * Returns the default ship speed.
     * @return the default ship speed
     */
    public int getDefaultSpeed(){
        return DEFAULT_SPEED;
    }

}
