import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;


public class MainGame extends BasicGameState {
	
	Image background;
	Rectangle selectRect;
	Rectangle hudbg;

	boolean test;
	static int minerals;
	
	float cameraX;
	float cameraY;
	
	float mouseX = -1;
	float mouseY = -1;
	
	int waveIntervall;
	int waveTime;
	int wave;
	
	static ArrayList<Building> resources;
	static ArrayList<Building> buildings;
	static ArrayList<Character> colonists;
	static ArrayList<Character> enemies;
	static ArrayList<GameObject> selected;

	static final int FIELDSIZE = 10000;
	
	public MainGame() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		cameraX = -5000+container.getWidth()/2;
		cameraY = -5000+container.getHeight()/2;
		minerals = 0;
		
		resources = new ArrayList<Building>();
		buildings = new ArrayList<Building>();
		colonists = new ArrayList<Character>();
		enemies = new ArrayList<Character>();
		selected = new ArrayList<GameObject>();
		
		buildings.add(new CommandCenter(4900,4900));
		colonists.add(new Worker(4900,5100));
		colonists.add(new Worker(4900,5000));
		colonists.add(new Worker(5000,5000));
		colonists.add(new Worker(4800,5000));
		
		colonists.add(new Fighter(5000,4800, 1));
		colonists.add(new Fighter(4800,4800, 1));
		colonists.add(new Tank(4900,4800, 1));
		
		resources.add(new MineralOre(5210,4800));
		resources.add(new MineralOre(5230,4850));
		resources.add(new MineralOre(5220,4900));
		resources.add(new MineralOre(5200,4950));
		
		enemies.add(new Pirate(5000,1000, 2));
		enemies.add(new Pirate(5100,1000, 2));
		enemies.add(new Pirate(5200,1000, 2));
		
		createMap();
		
		waveIntervall = 60000;
		waveTime = 0;
		wave = 0;
		
		hudbg = new Rectangle(0, container.getHeight()-container.getHeight()/4, container.getWidth(), container.getHeight()/4);
	}
	

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		Input input = container.getInput();
		
		if (waveTime >= waveIntervall) {
			float xDist = randomDistance();
			float yDist = randomDistance();
			float dist = (float) Math.sqrt(xDist*xDist + yDist*yDist);
			
			float xSpawnPoint = 11000*(xDist/dist);
			float ySpawnPoint = 11000*(yDist/dist);
			
			for(int i = 0; i < 5+StartGame.getDifficulty()*wave; i++){
				enemies.add(new Pirate(xSpawnPoint+50*i,ySpawnPoint+50*i, 2));
			}
			
			waveTime = 0;
			wave++;
		} else {
			waveTime += delta;
		}
		
		updateList(resources, container, delta);
		updateList(buildings, container, delta);
		updateList(colonists, container, delta);
		updateList(enemies, container, delta);
		
		if(input.isKeyDown(Input.KEY_DOWN)){
			cameraY -= delta*0.5;
		}
		if(input.isKeyDown(Input.KEY_UP)){
			cameraY += delta*0.5;
		}
		if(input.isKeyDown(Input.KEY_LEFT)){
			cameraX += delta*0.5;
		}
		if(input.isKeyDown(Input.KEY_RIGHT)){
			cameraX -= delta*0.5;
		}
		
		if(input.isKeyPressed(Input.KEY_SPACE)){
			game.enterState(0);
		}
		
		if(input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {	
			mouseX = input.getMouseX();
			mouseY = input.getMouseY();
			if (hudbg.contains(mouseX, mouseY)) { //Check if interface was pressed
				if ((new Rectangle(0, container.getHeight()-hudbg.getHeight(), hudbg.getHeight(), hudbg.getHeight())).contains(mouseX, mouseY))
					return;
				if ((!(selected.isEmpty())) && selected.get(0) instanceof Builder) {
					Builder b = (Builder) selected.get(0);
					for (int i=0;i<b.getBuildOptions().size();i++) {
						if ((new Rectangle(container.getWidth()-container.getWidth()/5-(container.getWidth()/5-hudbg.getHeight()), container.getHeight()-hudbg.getHeight()+i*hudbg.getHeight()/3, hudbg.getHeight()*2, hudbg.getHeight()/3)).contains(mouseX,  mouseY)) {
							b.queueSpawn(i);
							mouseX = -1;
							mouseY = -1;
						}
					}
				}
				mouseX = -1;
				mouseY = -1;
				return;
			}
			mouseX -= cameraX; // Store mouse position when starting rectangle
			mouseY -= cameraY;
			selectRect = new Rectangle(mouseX, mouseY, 1, 1);
		} else if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
			if (mouseX != -1 && mouseY != -1 && selectRect != null) // Store rectangle size for drawing
				selectRect.setBounds(Math.min(input.getMouseX() - cameraX, mouseX), Math.min(input.getMouseY() - cameraY, mouseY), Math.abs(input.getMouseX() - cameraX - mouseX), Math.abs(input.getMouseY() - cameraY - mouseY));
			else if ((new Rectangle(0, container.getHeight()-hudbg.getHeight(), hudbg.getHeight(), hudbg.getHeight())).contains(input.getMouseX(), input.getMouseY())) {
				cameraX = -(MainGame.FIELDSIZE*input.getMouseX()/hudbg.getHeight())+container.getWidth()/2;
				cameraY = -((MainGame.FIELDSIZE*(input.getMouseY()-container.getHeight()+hudbg.getHeight())/hudbg.getHeight()))+container.getHeight()/2;
				mouseX = -1;
				mouseY = -1;
			}
		} else if (mouseX != -1 && mouseY != -1 && !input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
			for (GameObject gob : selected) {
				gob.deselect();
			}
			selected.clear();
			selectFromList(colonists); // Try to select colonists
			if (selected.isEmpty()) // If that fails (if there aren't any colonists in the select box)
				selectFromList(buildings); // Try to select buildings
			selectRect = null;
			mouseX = -1;
			mouseY = -1;
		}
		if(input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)){
			float mouseX = input.getMouseX();
			float mouseY = input.getMouseY();
			if (hudbg.contains(input.getMouseX(), input.getMouseY())) {
				if (input.getMouseX() < hudbg.getHeight()) {
					mouseX = mapToRealCord(mouseX);
					mouseY = mapToRealCord(mouseY-3*container.getHeight()/4);
				}
				else 
					return;
			} else {
				mouseX -= cameraX;
				mouseY -= cameraY;
			}
			Vector2f newMovePoint = new Vector2f(mouseX, mouseY);
			for(GameObject sel : selected){
				
				GameObject target = mouseTarget(enemies, input.getMouseX(), input.getMouseY()); 
				if(target == null){
					target = mouseTarget(buildings, input.getMouseX(), input.getMouseY());
					if(target == null){
						target = mouseTarget(resources, input.getMouseX(), input.getMouseY());
						if(target == null){
							target = mouseTarget(colonists, input.getMouseX(), input.getMouseY());
						}
					}
				}
				if(target != null){
					sel.setTarget(target);
				}
				else{
					sel.setTarget(newMovePoint);
				}
			}
		}
	}

	private float mapToRealCord(float cord) {
		float r = MainGame.FIELDSIZE*cord/hudbg.getHeight();
		return r;
	}
	
	private void selectFromList(ArrayList<? extends GameObject> list) {
		for(GameObject gob : list){
			gob.deselect();
			selected.remove(gob);
			if (selectRect.intersects(gob.getRect())) {
				gob.select();
				selected.add(gob);
			}
		}
	}
	
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
				

		
		//Translates the coordinates of view, must be first in render
		g.translate(cameraX, cameraY);

		if (selectRect != null)
			g.draw(selectRect);
		//g.drawString("Press SPACE to go to main menu", 400, 200);

		renderList(resources, container, g);
		renderList(buildings, container, g);
		renderList(colonists, container, g);
		renderList(enemies, container, g);

		//Translate back so hud will be rendered on top
		g.translate(-cameraX,-cameraY);
		g.drawString("Minerals: "+minerals, 850, 50);
		

		drawHud(container, g);

		renderListOnMap(resources, container, g);
		renderListOnMap(buildings, container, g);
		renderListOnMap(colonists, container, g);
		renderListOnMap(enemies, container, g);
		
	}
	
	private void drawHud(GameContainer container, Graphics g) {
		float hudHeight = hudbg.getHeight();
		float width = hudbg.getWidth()/5;
		g.setColor(Color.black);
		g.fill(hudbg);
		g.setColor(Color.white);
		g.draw(hudbg);
		if (!(selected.isEmpty())) {
			g.draw(new Rectangle(container.getWidth()-width*2, container.getHeight()-hudHeight, hudHeight, hudHeight));
			g.drawImage(selected.get(0).portrait.getScaledCopy((float)hudHeight/selected.get(0).portrait.getHeight()), container.getWidth()-width*2, container.getHeight()-hudHeight);
			if (selected.get(0) instanceof Builder) {
				Builder b = (Builder) selected.get(0);
				ArrayList<String> buildOptions = b.getBuildOptions();
				for (int i=0;i<buildOptions.size();i++) { //Build options
					g.draw(new Rectangle(container.getWidth()-width-(width-hudHeight), container.getHeight()-hudHeight+i*hudHeight/3, hudHeight*2, hudHeight/3));
					g.drawString(buildOptions.get(i) + "  cost:" + b.getBuildCosts()[i], container.getWidth()-width-(width-hudHeight)+5, container.getHeight()-hudHeight+hudHeight/8+i*hudHeight/3);
				}
				if (!(b.getBuildQueue().isEmpty())) { //Build progress
					g.setColor(Color.red);
					g.fill(new Rectangle(hudHeight, container.getHeight()-hudHeight+hudHeight/16, width*b.getProgress()/b.getBuildTime()[b.getBuildQueue().get(0)], 3));
					g.setColor(Color.white);
				}
				for (int i=0;i<b.getBuildQueue().size();i++) { //Build queue
					g.draw(new Rectangle(hudHeight, container.getHeight()-hudHeight+i*hudHeight/3, width, hudHeight/3));
					g.drawString(b.getBuildOptions().get(b.getBuildQueue().get(i)), hudHeight+hudHeight/8, container.getHeight()-hudHeight+hudHeight/8+i*hudHeight/3);
				}	
			}
		}
		Rectangle map = new Rectangle(0, container.getHeight()-hudHeight, hudHeight, hudHeight);
		Rectangle currentPos = new Rectangle(cameraX*-hudHeight/MainGame.FIELDSIZE, container.getHeight()-hudHeight+cameraY*-hudHeight/MainGame.FIELDSIZE, hudHeight*container.getWidth()/MainGame.FIELDSIZE, hudHeight*container.getHeight()/MainGame.FIELDSIZE);
		g.draw(map);
		g.draw(currentPos);		
	}
	
	
	private void updateList(ArrayList<? extends GameObject> gameList, GameContainer container, int delta) throws SlickException{
		if(!gameList.isEmpty()){
			for(Iterator<GameObject> iter = (Iterator<GameObject>) gameList.iterator(); iter.hasNext(); ){
				GameObject gob = (GameObject) iter.next();
				if(gob.isAlive()){
					gob.update(container, delta);
					
					if(Character.class.isAssignableFrom(gob.getClass()) && !gob.hasTarget()){
						collision((ArrayList<Character>) gameList, (Character) gob, delta);
					}
				}
				else{
					iter.remove();
				}
			}
		}
	}
	
	private void collision(ArrayList<Character> gameList, Character gob,  int delta){
		for(Character col : gameList){
			if(col.getRect().intersects(gob.getRect()) && col != gob && !col.hasTarget()){
				float distance = gob.targetDistance(col.getX(), col.getY());
				
				float cos;
				float sin;
				if(distance > 1){
					cos = (gob.getX() - col.getX())/distance;
					sin = (gob.getY() - col.getY())/distance;
				}
				else{
					cos = new Random().nextFloat();
					sin = (float) Math.sqrt(1-cos*cos);
				}
				gob.push(cos, sin, 0.1, delta);
			}
		}
	}
	
	private void renderList(ArrayList<? extends GameObject> characterList, GameContainer container, Graphics g) throws SlickException{
		if(!characterList.isEmpty()){
			for(GameObject gob : characterList){
				gob.render(container, g);
			}
		}
	}
	
	private void renderListOnMap(ArrayList<? extends GameObject> characterList, GameContainer container, Graphics g) throws SlickException{
		if(!characterList.isEmpty()){
			for(GameObject gob : characterList){
				gob.drawOnMap(container, g, cameraX, cameraY);
			}
		}
	}
	
	private GameObject mouseTarget(ArrayList<? extends GameObject> list, float mouseX, float mouseY){
		if(!list.isEmpty()){
			for(GameObject gob : list){
				if(isPointingAt(gob, mouseX, mouseY)){
					return gob;
				}
			}
		}
		return null;
	}


	private boolean isPointingAt(GameObject gob, float mouseX, float mouseY){
		if(mouseX-cameraX > gob.getX()-gob.getWidth()/2 && mouseX-cameraX < gob.getX()+gob.getWidth()/2 &&
				mouseY-cameraY > gob.getY()-gob.getHeight()/2 && mouseY-cameraY < gob.getY()+gob.getHeight()/2){
			return true;
		}
		
		return false;
	}
	
	public static Character nearestEnemy(float charX, float charY, int faction){
		
		Character nearestEnemy = null;
		ArrayList<Character> possibleTargets;
		if(faction == 1){
			possibleTargets = enemies;
		} else{
			possibleTargets = colonists;
		}
		for(Character col : possibleTargets){
			if(nearestEnemy != null){
				if(col.targetDistance(charX, charY) < nearestEnemy.targetDistance(charX, charY)){
					nearestEnemy = col;
				}
			}
			else{
				nearestEnemy = col;
			}
		}
		return nearestEnemy;
	}

	public static CommandCenter nearestCommandCenter(float charX, float charY){
		CommandCenter nearestCC = null;
		
		for(Building comc : buildings){
			if(comc.getClass() == CommandCenter.class){
				if(nearestCC == null){
					nearestCC = (CommandCenter) comc;
				}
				else if(comc.targetDistance(charX, charY) < nearestCC.targetDistance(charX, charY)){
					nearestCC = (CommandCenter) comc;
				}
			}
		}
		return nearestCC;		
	}
	
	private void createMap() throws SlickException{
		Random rand = new Random();
		
		int mineralClusters = 9-StartGame.getDifficulty();
		int clusterSize = 20-StartGame.getDifficulty();
		
		boolean[][] mapGrid = new boolean[10][10];
		
		//Randomly flag grid squares for mineral-cluster
		while(mineralClusters > 0){
			int xg = rand.nextInt(10);
			int yg = rand.nextInt(10);
			
			if(!mapGrid[xg][yg] && xg != 4 && xg != 5 && yg != 4 && yg != 5){
				mapGrid[xg][yg] = true;
				mineralClusters--;
			}
		}
		
		//Place mineral-clusters on the map
		for(int i = 0; i < mapGrid.length; i++){
			for(int j = 0; j < mapGrid[i].length; j++){
				if(mapGrid[i][j]){
					float xCenter = 500+(1000*i);
					float yCenter = 500+(1000*j);
					
					for(int c = 0; c < clusterSize; c++){
						
						float xDist = randomDistance();
						float yDist = randomDistance();
						float dist = (float) Math.sqrt(xDist*xDist + yDist*yDist);
						
						resources.add(new MineralOre(xCenter+(100+rand.nextInt(400))*xDist/dist, yCenter+(100+rand.nextInt(400))*yDist/dist));
					}
				}
			}
		}
	}

	public static void build(Building construction, Building finished) {
		buildings.remove(construction);
		buildings.add(finished);
	}
	
	private int randomDistance(){
		Random rand = new Random();
		
		int dist = 1+rand.nextInt(10);
		
		if(rand.nextBoolean()){
			dist = dist*-1;
		}
		return dist;
	}
	
	
	@Override
	public int getID() {
		return 1;
	}

}
