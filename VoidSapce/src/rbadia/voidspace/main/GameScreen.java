package rbadia.voidspace.main;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;

import rbadia.voidspace.graphics.GraphicsManager;
import rbadia.voidspace.model.Asteroid;
import rbadia.voidspace.model.Bullet;
import rbadia.voidspace.model.EnemyShip;
import rbadia.voidspace.model.Ship;
import rbadia.voidspace.sounds.SoundManager;

/**
 * Main game screen. Handles all game graphics updates and some of the game logic.
 */
public class GameScreen extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private BufferedImage backBuffer;
	private Graphics2D g2d;
	
	private static final int NEW_SHIP_DELAY = 500;
	private static final int NEW_ASTEROID_DELAY = 500;
	private static final int firstLevel = 5;
	private static final int secondLevel = 15;
	private static final int thirdLevel = 25;
	
	private long lastShipTime;
	private long lastAsteroidTime;
	
	private Rectangle asteroidExplosion;
	private Rectangle shipExplosion;
	private Rectangle enemyShipExplosion;
	
	private JLabel shipsValueLabel;
	private JLabel destroyedValueLabel;
	private JLabel scoreValueLabel;

	private Random rand;
	
	private Font originalFont;
	private Font bigFont;
	private Font biggestFont;
	
	private GameStatus status;
	private SoundManager soundMan;
	private GraphicsManager graphicsMan;
	private GameLogic gameLogic;

	/**
	 * This method initializes 
	 * 
	 */
	public GameScreen() {
		super();
		// initialize random number generator
		rand = new Random();
		
		initialize();
		
		// init graphics manager
		graphicsMan = new GraphicsManager();
		
		// init back buffer image
		backBuffer = new BufferedImage(500, 400, BufferedImage.TYPE_INT_RGB);
		g2d = backBuffer.createGraphics();
	}

	/**
	 * Initialization method (for VE compatibility).
	 */
	private void initialize() {
		// set panel properties
        this.setSize(new Dimension(500, 400));
        this.setPreferredSize(new Dimension(500, 400));
        this.setBackground(Color.BLACK);
	}

	/**
	 * Update the game screen.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// draw current backbuffer to the actual game screen
		g.drawImage(backBuffer, 0, 0, this);
	}
	
	/**
	 * Update the game screen's backbuffer image.
	 */
	public void updateScreen(){
		Ship ship = gameLogic.getShip();
		Asteroid asteroid = gameLogic.getAsteroid();
		List<Bullet> bullets = gameLogic.getBullets();
		List<Asteroid> asteroids = gameLogic.getAsteroids();
		EnemyShip enemyShip = gameLogic.getEnemyShip();
		List<Bullet> enemyBullets = gameLogic.getEnemyBullets();
		
		// set orignal font - for later use
		if(this.originalFont == null){
			this.originalFont = g2d.getFont();
			this.bigFont = originalFont;
		}
		
		// erase screen
		g2d.setPaint(Color.BLACK);
		g2d.fillRect(0, 0, getSize().width, getSize().height);

		// draw 50 random stars
		drawStars(50);
		
		// if the game is starting, draw "Get Ready" message
		if(status.isGameStarting()){
			drawGetReady();
			return;
		}
		
		// if the game is over, draw the "Game Over" message
		if(status.isGameOver()){
			// draw the message
			drawGameOver();
			
			long currentTime = System.currentTimeMillis();
			// draw the explosions until their time passes
			if((currentTime - lastAsteroidTime) < NEW_ASTEROID_DELAY){
				graphicsMan.drawAsteroidExplosion(asteroidExplosion, g2d, this);
			}
			if((currentTime - lastShipTime) < NEW_SHIP_DELAY){
				graphicsMan.drawShipExplosion(shipExplosion, g2d, this);
			}
			return;
		}
		
		// the game has not started yet
		if(!status.isGameStarted()){
			// draw game title screen
			initialMessage();
			return;
		}

		// draw asteroid or enemy ships
		if(status.getAsteroidsDestroyed() < firstLevel){
			this.drawAsteroid(asteroid);
		}
		else if(status.getAsteroidsDestroyed() < secondLevel){
			// draws multiple asteroids
			for(int i=0; i<asteroids.size();i++){
				this.drawAsteroid(asteroids.get(i));
			}
		}

		else if(status.getAsteroidsDestroyed() < thirdLevel) {
			// draws multiple asteroids moving on a non-vertical form
			switch (rand.nextInt(2)) {
				case 0:
					for (int i = 0; i < asteroids.size(); i++) {
						this.drawAsteroid(asteroids.get(i));
					}
					break;
				case 1:
					for (int i = 0; i < asteroids.size(); i++) {
						this.drawAsteroidDiagonal(asteroids.get(i), asteroid.getSpeed());
					}
					break;
			}
		} 
		else{
			// draws multiple asteroids moving on a non-vertical form
			// it also adds an enemy ship
			switch (rand.nextInt(2)) {
				case 0:
					for (int i = 0; i < asteroids.size(); i++) {
						this.drawAsteroid(asteroids.get(i));
					}
					break;
				case 1:
					for (int i = 0; i < asteroids.size(); i++) {
						this.drawAsteroidDiagonal(asteroids.get(i),2*asteroid.getSpeed());
					}
					break;
			}
			this.drawEnemyShip(enemyShip, enemyBullets);
		}

		// draw bullets
		for(int i=0; i<bullets.size(); i++){
			Bullet bullet = bullets.get(i);
			graphicsMan.drawBullet(bullet, g2d, this);
			
			boolean remove = gameLogic.moveBullet(bullet);
			if(remove){
				bullets.remove(i);
				i--;
			}
		}
		
		// check bullet-asteroid collisions
		if(status.getAsteroidsDestroyed() < firstLevel){
			this.BulletAsteroidCollision(asteroid, bullets);
		}
		else{
			for(int i=0;i<asteroids.size();i++){
				this.BulletAsteroidCollision(asteroids.get(i), bullets);
			}
		}
		
		// draw ship
		this.drawShip(ship);
		
		// check ship-asteroid collision
		if(status.getAsteroidsDestroyed() < firstLevel) {
			this.ShipAsteroidCollision(asteroid, ship);
		}
		else{
			for(int i=0;i<asteroids.size();i++){
				this.ShipAsteroidCollision(asteroids.get(i), ship);
			}
		}

		//check bullet-enemyShip collisions
		this.BulletEnemyShipCollision(enemyShip,bullets);

		// check ship-enemyShip collisions
		this.ShipEnemyShipCollision(enemyShip,ship);

		// check enemybullet-ship collisions
		this.EnemyBulletShipCollision(enemyBullets,ship);

		
		// update asteroids destroyed label
		destroyedValueLabel.setText(Long.toString(status.getAsteroidsDestroyed()));
		
		// update ships left label
		shipsValueLabel.setText(Integer.toString(status.getShipsLeft()));

		//update score value
		scoreValueLabel.setText(Long.toString(status.getScore()));
	}

	/**
	 * Draws the "Game Over" message.
	 */
	private void drawGameOver() {
		String gameOverStr = "GAME OVER";
		Font currentFont = biggestFont == null? bigFont : biggestFont;
		float fontSize = currentFont.getSize2D();
		bigFont = currentFont.deriveFont(fontSize + 1).deriveFont(Font.BOLD);
		FontMetrics fm = g2d.getFontMetrics(bigFont);
		int strWidth = fm.stringWidth(gameOverStr);
		if(strWidth > this.getWidth() - 10){
			biggestFont = currentFont;
			bigFont = biggestFont;
			fm = g2d.getFontMetrics(bigFont);
			strWidth = fm.stringWidth(gameOverStr);
		}
		int ascent = fm.getAscent();
		int strX = (this.getWidth() - strWidth)/2;
		int strY = (this.getHeight() + ascent)/2;
		g2d.setFont(bigFont);
		g2d.setPaint(Color.WHITE);
		g2d.drawString(gameOverStr, strX, strY);
	}

	/**
	 * Draws the initial "Get Ready!" message.
	 */
	private void drawGetReady() {
		String readyStr = "Get Ready!";
		g2d.setFont(originalFont.deriveFont(originalFont.getSize2D() + 1));
		FontMetrics fm = g2d.getFontMetrics();
		int ascent = fm.getAscent();
		int strWidth = fm.stringWidth(readyStr);
		int strX = (this.getWidth() - strWidth)/2;
		int strY = (this.getHeight() + ascent)/2;
		g2d.setPaint(Color.WHITE);
		g2d.drawString(readyStr, strX, strY);
	}

	/**
	 * Draws the specified number of stars randomly on the game screen.
	 * @param numberOfStars the number of stars to draw
	 */
	private void drawStars(int numberOfStars) {
		g2d.setColor(Color.WHITE);
		for(int i=0; i<numberOfStars; i++){
			int x = (int)(Math.random() * this.getWidth());
			int y = (int)(Math.random() * this.getHeight());
			g2d.drawLine(x, y, x, y);
		}
	}

	/**
	 * Display initial game title screen.
	 */
	private void initialMessage() {
		String gameTitleStr = "Void Space";
		
		Font currentFont = biggestFont == null? bigFont : biggestFont;
		float fontSize = currentFont.getSize2D();
		bigFont = currentFont.deriveFont(fontSize + 1).deriveFont(Font.BOLD).deriveFont(Font.ITALIC);
		FontMetrics fm = g2d.getFontMetrics(bigFont);
		int strWidth = fm.stringWidth(gameTitleStr);
		if(strWidth > this.getWidth() - 10){
			bigFont = currentFont;
			biggestFont = currentFont;
			fm = g2d.getFontMetrics(currentFont);
			strWidth = fm.stringWidth(gameTitleStr);
		}
		g2d.setFont(bigFont);
		int ascent = fm.getAscent();
		int strX = (this.getWidth() - strWidth)/2;
		int strY = (this.getHeight() + ascent)/2 - ascent;
		g2d.setPaint(Color.YELLOW);
		g2d.drawString(gameTitleStr, strX, strY);
		
		g2d.setFont(originalFont);
		fm = g2d.getFontMetrics();
		String newGameStr = "Press <Space> to Start a New Game.";
		strWidth = fm.stringWidth(newGameStr);
		strX = (this.getWidth() - strWidth)/2;
		strY = (this.getHeight() + fm.getAscent())/2 + ascent + 16;
		g2d.setPaint(Color.WHITE);
		g2d.drawString(newGameStr, strX, strY);
		
		fm = g2d.getFontMetrics();
		String exitGameStr = "Press <Esc> to Exit the Game.";
		strWidth = fm.stringWidth(exitGameStr);
		strX = (this.getWidth() - strWidth)/2;
		strY = strY + 16;
		g2d.drawString(exitGameStr, strX, strY);
	}
	
	/**
	 * Prepare screen for game over.
	 */
	public void doGameOver(){
		shipsValueLabel.setForeground(new Color(49, 51, 53));
	}
	
	/**
	 * Prepare screen for a new game.
	 */
	public void doNewGame(){		
		lastAsteroidTime = -NEW_ASTEROID_DELAY;
		lastShipTime = -NEW_SHIP_DELAY;
				
		bigFont = originalFont;
		biggestFont = null;
				
        // set labels' text
		shipsValueLabel.setForeground(Color.BLACK);
		shipsValueLabel.setText(Integer.toString(status.getShipsLeft()));
		destroyedValueLabel.setText(Long.toString(status.getAsteroidsDestroyed()));
	}

	/**
	 * Sets the game graphics manager.
	 * @param graphicsMan the graphics manager
	 */
	public void setGraphicsMan(GraphicsManager graphicsMan) {
		this.graphicsMan = graphicsMan;
	}

	/**
	 * Sets the game logic handler
	 * @param gameLogic the game logic handler
	 */
	public void setGameLogic(GameLogic gameLogic) {
		this.gameLogic = gameLogic;
		this.status = gameLogic.getStatus();
		this.soundMan = gameLogic.getSoundMan();
	}

	/**
	 * Sets the label that displays the value for asteroids destroyed.
	 * @param destroyedValueLabel the label to set
	 */
	public void setDestroyedValueLabel(JLabel destroyedValueLabel) {
		this.destroyedValueLabel = destroyedValueLabel;
	}
	
	/**
	 * Sets the label that displays the value for ship (lives) left
	 * @param shipsValueLabel the label to set
	 */
	public void setShipsValueLabel(JLabel shipsValueLabel) {
		this.shipsValueLabel = shipsValueLabel;
	}

	public void setScoreValueLabel(JLabel scoreValueLabel) {this.scoreValueLabel = scoreValueLabel;}
	
	/**
	 * it draws a single asteroid
	 * @param asteroid the asteroid 
	 */
	public void drawAsteroid(Asteroid asteroid){
		if(!status.isNewAsteroid()){
			// draw the asteroid until it reaches the bottom of the screen
			if(asteroid.getY() + asteroid.getSpeed() < this.getHeight()){
				asteroid.translate(0, asteroid.getSpeed());
				graphicsMan.drawAsteroid(asteroid, g2d, this);
			}
			else{
				asteroid.setLocation(rand.nextInt(getWidth() - asteroid.width), 0);
			}
		}
		else{
			long currentTime = System.currentTimeMillis();
			if((currentTime - lastAsteroidTime) > NEW_ASTEROID_DELAY){
				// draw a new asteroid
				lastAsteroidTime = currentTime;
				status.setNewAsteroid(false);
				asteroid.setLocation(rand.nextInt(getWidth() - asteroid.width), 0);
			}
			else{
				// draw explosion
				graphicsMan.drawAsteroidExplosion(asteroidExplosion, g2d, this);
			}
		}
	}
	
	/**
	 * checks if there has been any collisions between asteroids and bullets
	 * @param asteroid the asteroid on screen 
	 * @param bullets the bullets on screen
	 * @return true if there has been any collisions false if there has not been collisions
	 */
	public boolean BulletAsteroidCollision(Asteroid asteroid, List<Bullet> bullets){
		for(int i=0; i<bullets.size(); i++){
			Bullet bullet = bullets.get(i);
			if(asteroid.intersects(bullet)){
				// increase asteroids destroyed count
				status.setAsteroidsDestroyed(status.getAsteroidsDestroyed() + 1);
				status.setScore(status.getScore() + 150);
				// "remove" asteroid
		        asteroidExplosion = new Rectangle(
		        		asteroid.x,
		        		asteroid.y,
		        		asteroid.width,
		        		asteroid.height);
				asteroid.setLocation(-asteroid.width, -asteroid.height);
				lastAsteroidTime = System.currentTimeMillis();
				graphicsMan.drawAsteroidExplosion(asteroidExplosion, g2d, this);
				if(status.getAsteroidsDestroyed() < firstLevel) {
					status.setNewAsteroid(true);
				}
				// play asteroid explosion sound
				soundMan.playAsteroidExplosionSound();
				
				// remove bullet
				bullets.remove(i);
				return true;
			}
		}
		return false; 
	}
	
	/**
	 * it draws the players ship
	 * @param ship the ship
	 */
	public void drawShip(Ship ship){
		if(!status.isNewShip()){
			// draw it in its current location
			graphicsMan.drawShip(ship, g2d, this);
		}
		else{
			// draw a new one
			long currentTime = System.currentTimeMillis();
			if((currentTime - lastShipTime) > NEW_SHIP_DELAY){
				lastShipTime = currentTime;
				status.setNewShip(false);
				ship = gameLogic.newShip(this);
			}
			else{
				// draw explosion
				graphicsMan.drawShipExplosion(shipExplosion, g2d, this);
			}
		}
	}
	
	/**
	 * it draws the enemy ship
	 * @param enemyShip the enemy ship
	 * @param enemyBullets it creates bullets for the enemy ship
	 */
	public void drawEnemyShip(EnemyShip enemyShip, List<Bullet> enemyBullets) {
		if (!status.isNewEnemyShip()) {
			// draw it in its current location
			// draw the asteroid until it reaches the bottom of the screen
			if(enemyShip.getX() + enemyShip.getSpeed() + enemyShip.getWidth() < this.getWidth()) {
				enemyShip.translate(enemyShip.getSpeed(), 0);
				graphicsMan.drawEnemyShip(enemyShip, g2d, this);
				for(int i=0; i<enemyBullets.size(); i++){
					Bullet bullet = enemyBullets.get(i);
					graphicsMan.drawBullet(bullet, g2d, this);
					gameLogic.moveEnemyBullet(bullet, enemyShip);
				}
			}
			else if(enemyShip.getX() - enemyShip.getSpeed() > 0){
				while(enemyShip.getX() - enemyShip.getSpeed() > 0) {
					enemyShip.translate(-enemyShip.getSpeed(), 0);
				}
			}

		} else {
			// draw a new one
			long currentTime = System.currentTimeMillis();
			if ((currentTime - lastShipTime) > NEW_SHIP_DELAY) {
				lastAsteroidTime = currentTime;
				status.setNewEnemyShip(false);
				enemyShip = gameLogic.newEnemyShip(this);
				enemyShip.setLocation(rand.nextInt(getWidth() - enemyShip.width), 0);
			} else {
				// draw explosion
				graphicsMan.drawEnemyShipExplosion(shipExplosion, g2d, this);
					}
			}
		}

	/**
	 * checks if there has been any collisions between asteroids and the ship
	 * @param asteroid the asteroid on screen
	 * @param ship the player's ship
	 */
	public void ShipAsteroidCollision(Asteroid asteroid, Ship ship){
		if(asteroid.intersects(ship)){
			// decrease number of ships left
			status.setShipsLeft(status.getShipsLeft() - 1);
			
			status.setAsteroidsDestroyed(status.getAsteroidsDestroyed() + 1);

			status.setScore(status.getScore() + 150);

			// "remove" asteroid
	        asteroidExplosion = new Rectangle(
	        		asteroid.x,
	        		asteroid.y,
	        		asteroid.width,
	        		asteroid.height);
			asteroid.setLocation(-asteroid.width, -asteroid.height);
			status.setNewAsteroid(true);
			lastAsteroidTime = System.currentTimeMillis();
			
			// "remove" ship
	        shipExplosion = new Rectangle(
	        		ship.x,
	        		ship.y,
	        		ship.width,
	        		ship.height);
			ship.setLocation(this.getWidth() + ship.width, -ship.height);
			graphicsMan.drawShipExplosion(shipExplosion, g2d, this);
			status.setNewShip(true);
			lastShipTime = System.currentTimeMillis();
			
			// play ship explosion sound
			soundMan.playShipExplosionSound();
			// play asteroid explosion sound
			soundMan.playAsteroidExplosionSound();
		}
	}
	
	/**
	 * it draws the asteroids diagonally
	 * @param asteroid the asteroid
	 * @param speed how fast the asteroid "moves"
	 */
	public void drawAsteroidDiagonal(Asteroid asteroid, int speed){
		if(!status.isNewAsteroid()){
			// draw the asteroid until it reaches the bottom of the screen
			if(asteroid.getY() + asteroid.getSpeed() < this.getHeight()){
				switch(rand.nextInt(2)){
					case 0:
						asteroid.translate(speed, asteroid.getSpeed());
						break;
					case 1:
						asteroid.translate(-speed, asteroid.getSpeed());
						break;
				}
				graphicsMan.drawAsteroid(asteroid, g2d, this);
			}
			else{
				asteroid.setLocation(rand.nextInt(getWidth() - asteroid.width), 0);
			}
		}
		else{
			long currentTime = System.currentTimeMillis();
			if((currentTime - lastAsteroidTime) > NEW_ASTEROID_DELAY){
				// draw a new asteroid
				lastAsteroidTime = currentTime;
				status.setNewAsteroid(false);
				asteroid.setLocation(rand.nextInt(getWidth() - asteroid.width), 0);
			}
			else{
				// draw explosion
				graphicsMan.drawAsteroidExplosion(asteroidExplosion, g2d, this);
			}
		}
	}
	
	/**
	 * Checks for any collisions between the enemy ship and bullets
	 * @param enemyShip the enemy ship on screen
	 * @param bullets the bullets on screen
	 * @return true if there has been a collision false if there has not been a collision
	 */
	public boolean BulletEnemyShipCollision(EnemyShip enemyShip, List<Bullet> bullets){
		for(int i=0; i<bullets.size(); i++){
			Bullet bullet = bullets.get(i);
			if(enemyShip.intersects(bullet)){
				//increase score
				status.setScore(status.getScore() + 300);
				// "remove" enemyShip
				enemyShipExplosion = new Rectangle(
						enemyShip.x,
						enemyShip.y,
						enemyShip.width,
						enemyShip.height);
				enemyShip.setLocation(-enemyShip.width, -enemyShip.height);
				lastAsteroidTime = System.currentTimeMillis();
				graphicsMan.drawEnemyShipExplosion(enemyShipExplosion, g2d, this);
				status.setNewEnemyShip(true);

				// play ship explosion sound
				soundMan.playShipExplosionSound();

				// remove bullet
				bullets.remove(i);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * checks for any collisions between the enemy ship and the player's ship
	 * @param enemyShip the enemy ship on screen
	 * @param ship the player's screen
	 */
	public void ShipEnemyShipCollision(EnemyShip enemyShip, Ship ship){
		if(enemyShip.intersects(ship)){
			// decrease number of ships left
			status.setShipsLeft(status.getShipsLeft() - 1);
			// increase score
			status.setScore(status.getScore() + 300);

			// "remove" enemyShip
			asteroidExplosion = new Rectangle(
					enemyShip.x,
					enemyShip.y,
					enemyShip.width,
					enemyShip.height);
			enemyShip.setLocation(-enemyShip.width, -enemyShip.height);
			status.setNewAsteroid(true);
			lastAsteroidTime = System.currentTimeMillis();

			// "remove" ship
			shipExplosion = new Rectangle(
					ship.x,
					ship.y,
					ship.width,
					ship.height);
			ship.setLocation(this.getWidth() + ship.width, -ship.height);
			graphicsMan.drawShipExplosion(shipExplosion, g2d, this);
			status.setNewShip(true);
			status.setNewEnemyShip(true);
			lastShipTime = System.currentTimeMillis();

			// play ship explosion sound
			soundMan.playShipExplosionSound();
		}
	}

	/**
	 * checks for any collisions between enemy bullets and the player's ship
	 * @param enemyBullets enemy bullets 
	 * @param ship the player's ship
	 */
	public void EnemyBulletShipCollision(List<Bullet> enemyBullets, Ship ship){
		for(int i=0; i<enemyBullets.size();i++) {
			if (enemyBullets.get(i).intersects(ship)) {
				// decrease number of ships left
				status.setShipsLeft(status.getShipsLeft() - 1);

				// "remove" bullet
				enemyBullets.remove(i);
				lastAsteroidTime = System.currentTimeMillis();

				// "remove" ship
				shipExplosion = new Rectangle(
						ship.x,
						ship.y,
						ship.width,
						ship.height);
				ship.setLocation(this.getWidth() + ship.width, -ship.height);
				graphicsMan.drawShipExplosion(shipExplosion, g2d, this);
				status.setNewShip(true);
				lastShipTime = System.currentTimeMillis();

				// play ship explosion sound
				soundMan.playShipExplosionSound();
			}
		}
	}
}


