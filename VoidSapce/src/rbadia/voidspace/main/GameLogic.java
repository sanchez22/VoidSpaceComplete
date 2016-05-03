package rbadia.voidspace.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import rbadia.voidspace.model.Asteroid;
import rbadia.voidspace.model.Bullet;
import rbadia.voidspace.model.EnemyShip;
import rbadia.voidspace.model.Ship;
import rbadia.voidspace.sounds.SoundManager;


/**
 * Handles general game logic and status.
 */
public class GameLogic {
	private GameScreen gameScreen;
	private GameStatus status;
	private SoundManager soundMan;
	
	private Ship ship;
	private Asteroid asteroid;
	private List<Bullet> bullets;
	private List<Asteroid> asteroids;
	private EnemyShip enemyShip;
	private List<Bullet> enemyBullets;
	
	/**
	 * Create a new game logic handler
	 * @param gameScreen the game screen
	 */
	public GameLogic(GameScreen gameScreen){
		this.gameScreen = gameScreen;
		
		// initialize game status information
		status = new GameStatus();
		// initialize the sound manager
		soundMan = new SoundManager();
		
		// init some variables
		bullets = new ArrayList<Bullet>();
	}

	/**
	 * Returns the game status
	 * @return the game status 
	 */
	public GameStatus getStatus() {
		return status;
	}

	public SoundManager getSoundMan() {
		return soundMan;
	}

	public GameScreen getGameScreen() {
		return gameScreen;
	}

	/**
	 * Prepare for a new game.
	 */
	public void newGame(){
		status.setGameStarting(true);
		
		// init game variables
		bullets = new ArrayList<Bullet>();

		status.setShipsLeft(3);
		status.setGameOver(false);
		status.setAsteroidsDestroyed(0);
		status.setNewAsteroid(false);
				
		// init the ship and the asteroid
        newShip(gameScreen);
        newAsteroid(gameScreen);
        newAsteroids(gameScreen, 6);
		newEnemyShip(gameScreen);
		newEnemyBullets(3, enemyShip);
        
        // prepare game screen
        gameScreen.doNewGame();
        
        // delay to display "Get Ready" message for 1.5 seconds
		Timer timer = new Timer(1500, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				status.setGameStarting(false);
				status.setGameStarted(true);
			}
		});
		timer.setRepeats(false);
		timer.start();
	}
	
	/**
	 * Check game or level ending conditions.
	 */
	public void checkConditions(){
		// check game over conditions
		if(!status.isGameOver() && status.isGameStarted()){
			if(status.getShipsLeft() == 0){
				gameOver();
			}
		}
	}
	
	/**
	 * Actions to take when the game is over.
	 */
	public void gameOver(){
		status.setGameStarted(false);
		status.setGameOver(true);
		gameScreen.doGameOver();
		
        // delay to display "Game Over" message for 3 seconds
		Timer timer = new Timer(3000, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				status.setGameOver(false);
			}
		});
		timer.setRepeats(false);
		timer.start();
	}
	
	/**
	 * Fire a bullet from ship.
	 */
	public void fireBullet(){
		Bullet bullet = new Bullet(ship);
		bullets.add(bullet);
		soundMan.playBulletSound();
	}
	
	/**
	 * Move a bullet once fired.
	 * @param bullet the bullet to move
	 * @return if the bullet should be removed from screen
	 */
	public boolean moveBullet(Bullet bullet){
		if(bullet.getY() - bullet.getSpeed() >= 0){
			bullet.translate(0, -bullet.getSpeed());
			return false;
		}
		else{
			return true;
		}
	}
/**
 * Move an enemy bullet once fired.
 * @param bullet the enemy bullet to move
 * @param enemyShip the enemy ship that has the bullets
 */
	public void moveEnemyBullet(Bullet bullet,EnemyShip enemyShip){
		if(bullet.getY() + bullet.getSpeed() <= gameScreen.getHeight()){
			bullet.translate(0, bullet.getSpeed());
		}
		else{
			bullet.setLocation(enemyShip.x +enemyShip.width/2, enemyShip.y + enemyShip.height);
		}
	}
	
	/**
	 * Create a new ship (and replace current one).
	 */
	public Ship newShip(GameScreen screen){
		this.ship = new Ship(screen);
		return ship;
	}

	/**
	 *Create a new enemy ship.
     */
	public EnemyShip newEnemyShip(GameScreen screen){
		this.enemyShip = new EnemyShip(screen);
		return enemyShip;
	}
	
	/**
	 * Create a new asteroid.
	 */
	public Asteroid newAsteroid(GameScreen screen){
		this.asteroid = new Asteroid(screen);
		return asteroid;
	}
	
	/**
	 * Creates a new arraylist of asteroids
	 * @param screen the game screen
	 * @param numAsteroids number of Asteroids to be added
	 * @return a new arraylist of asteroids
	 */
	public List<Asteroid> newAsteroids(GameScreen screen,int numAsteroids){
		asteroids = new ArrayList<Asteroid>();
		for(int i=0; i<numAsteroids;i++){
		asteroids.add(this.newAsteroid(screen));	
		}
		return asteroids;
	}

	/**
	 * Creates a new arraylist of enemyBullets
	 * @param numBullets number of enemy bullets to be added
	 * @param enemyShip the ship that is going to have the bullets
	 * @return a new arraylist of enemy bullets
	 */
	public List<Bullet> newEnemyBullets(int numBullets, EnemyShip enemyShip){
		enemyBullets = new ArrayList<Bullet>();
		for(int i=0; i<numBullets;i++){
			Bullet bullet = new Bullet(enemyShip);
			enemyBullets.add(bullet);
		}
		return enemyBullets;
	}
	
	/**
	 * Returns the ship.
	 * @return the ship
	 */
	public Ship getShip() {return ship;}

	/**
	 * Returns the asteroid.
	 * @return the asteroid
	 */
	public Asteroid getAsteroid() {return asteroid;}

	/**
	 * Returns the list of bullets.
	 * @return the list of bullets
	 */
	public List<Bullet> getBullets() {return bullets;}

	/**
	 * Returns the list of enemy bullets
	 * @return the list of enemy bullets
	 */
	public List<Bullet> getEnemyBullets() {return enemyBullets;}
	
	/**
	 * Returns the list of asteroids
	 * @return the list of asteroids
	 */
	public List<Asteroid> getAsteroids(){return asteroids;}

	/**
	 * returns the enemy ship
	 * @return the enemy ship
	 */
	public EnemyShip getEnemyShip(){return enemyShip;}
}
