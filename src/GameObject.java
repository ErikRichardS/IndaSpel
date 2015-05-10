import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;


public abstract class GameObject {
	
	protected float x;
	protected float y;
	protected int height;
	protected int width;
	protected int maxHealth;
	protected int currentHealth;
	protected int damage;
	protected float range;
	protected boolean alive;
	protected Image portrait;
	
	//To be changed
	protected boolean selected;
	protected Vector2f movePoint;
	protected GameObject target;
	
	public GameObject(float x, float y, int width, int height, 
					int maxHealth, int damage, float range, String image) throws SlickException{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.maxHealth = maxHealth;
		this.currentHealth = maxHealth;
		this.damage = damage;
		this.range = range;
		
		this.portrait = new Image(image);
		this.portrait = portrait.getScaledCopy(width, height);
	}
	
	public abstract void update(GameContainer container, Input input, int delta)
			throws SlickException;
	
	public abstract void render(GameContainer container, Graphics g)
			throws SlickException;
	
	protected void renderPortrait(Graphics g){
		if(selected){
			g.drawString("V", x, y-height/2-20);
		}
		g.drawImage(portrait, x-width/2, y-height/2);
		
	}
	
	public abstract void setTarget(Vector2f newMovePoint);
	
	public void setTarget(GameObject newTarget){
		target = newTarget;
		movePoint = null;
	}
	
	protected boolean targetInRange(){
		if(targetDistance(target.getX(), target.getY()) < range){
			return true;
		}
		return false;
	}
	
	protected float targetDistance(float targetX, float targetY){
		return (float) Math.sqrt(Math.pow(targetX-x, 2) + Math.pow(targetY-y, 2));
	}
	
	public GameObject getTarget(){
		return target;
	}
	
	public float getX(){
		return x;
	}
	
	public float getY(){
		return y;
	}
	
	public int getHeight(){
		return height;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getMaxHealth(){
		return maxHealth;
	}
	
	public int getCurrentHealth(){
		return currentHealth;
	}
	
	public boolean isAlive(){
		return alive;
	}
	
	public void takeDamage(int damage) {
		currentHealth -= damage;
	}
	
	public void select(){ 
		selected = true;
	}
	
	public void deselect(){
		selected = false;
	}
	
}
