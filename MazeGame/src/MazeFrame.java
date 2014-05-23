import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


public class MazeFrame extends JFrame implements ActionListener {
	//Private Fields
	private static final long serialVersionUID = 1L;

	private int height;
	private int width;
	
	private Game g;
	
	//Frame components
	private JPanel mazeGrid;
	private JLayeredPane[][] mazeGridComp;
	
	private JPanel sidePanel;
	private JButton exitButton;

	private Tile lastPlayerPos;
	private Dimension blockSize;
	
	//Store sprites once
	private HashMap<String, PlayerPanel> sprites;
	
	private String wallSprite;
	private String pathSprite;
	private String playerSprite;
	private String doorSprite;
	private String keySprite;
	private String enemySprite;
	private String coinSprite;
	private String swordSprite;
	
	public MazeFrame(Game g, int width, int height)
	{
		//Initilisation
		this.height = height + 2; 	//add 2 for border around maze
		this.width = width + 2;
		this.g = g;
		
		this.mazeGrid = new JPanel();
		this.mazeGridComp = new JLayeredPane[this.width][this.height];
		this.sidePanel = new JPanel();
		this.exitButton = new JButton("Exit");
		this.exitButton.addActionListener(this);
		
		//Initilise character spites
		this.blockSize = new Dimension(48, 48);
		this.sprites = new HashMap<String, PlayerPanel>();
		
		this.wallSprite = "steel_wall";
		this.pathSprite = "grass";
		this.doorSprite = "locked_door";
		this.playerSprite = "link";
		this.keySprite = "key";
		this.coinSprite = "coin";
		this.enemySprite = "dead_pacman_monster";
		this.swordSprite = "sword";
		
		//Add sprites to hashmap
		PlayerPanel sprite = new PlayerPanel(wallSprite);	
		sprites.put(wallSprite, sprite);
		
		sprite = new PlayerPanel(pathSprite);	
		sprites.put(pathSprite, sprite);
		sprite = new PlayerPanel(playerSprite);	
		sprites.put(playerSprite, sprite);
		sprite = new PlayerPanel(doorSprite);	
		sprites.put(doorSprite, sprite);
		sprite = new PlayerPanel(keySprite);	
		sprites.put(keySprite, sprite);
		sprite = new PlayerPanel(enemySprite);	
		sprites.put(enemySprite, sprite);
		sprite = new PlayerPanel(coinSprite);	
		sprites.put(coinSprite, sprite);
		sprite = new PlayerPanel(swordSprite);
		sprites.put(swordSprite, sprite);
		
		//Initilise side panel looks
		this.sidePanel.setPreferredSize(new Dimension( (int) ((this.width * blockSize.getWidth()) *0.4) ,	//side panel is based on mazes size, width is 40% of maze width 
														(int) (this.height * blockSize.getHeight()))); //height is matched exactly
		this.sidePanel.setBackground(new Color(240, 240, 240));
		this.sidePanel.setLayout(new GridBagLayout());
		this.sidePanel.setBorder(new LineBorder(Color.black, 2));
		
		//Make maze take up full screen
		Toolkit tk = Toolkit.getDefaultToolkit();  
		int xSize = ((int) tk.getScreenSize().getWidth());  
		int ySize = ((int) tk.getScreenSize().getHeight()); 
		Dimension fullscreen = new Dimension(xSize, ySize);
		this.setPreferredSize(fullscreen);
		
		//Update state
		this.setExtendedState(Frame.MAXIMIZED_BOTH);  

		//Fix window size
		this.setResizable(false);
		
		//Set frame layout
		this.setLayout(new GridBagLayout());

		//Set close operation
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Pack
		this.setUndecorated(true);
		this.pack();
		this.setVisible(true);
	}

	//Perform actions based on user actions
	public void actionPerformed(ActionEvent e)
	{
		//Detect object who performed action
		if (e.getSource() == this.exitButton) {
			int dialogResult = JOptionPane.showConfirmDialog (null, "Are you sure you want to exit to the main menu?\n\nAll game progress will be lost.","Exit Warning", JOptionPane.YES_NO_OPTION);
			
			//If user wishes to quit
			if (dialogResult == JOptionPane.YES_OPTION) {
				g.setIsGameOver(true);
				g.setIsInGame(false);
			} else {
				this.requestFocus();	//request focus again
			}
		}
	}

	//Update only the player position in maze
	public void update(Maze m) 
	{
		if (m.playerDied()) {
			mazeGridComp[lastPlayerPos.getX()][lastPlayerPos.getY()].remove(0);
			return;
		}
		
		if (m.getPlayerLoc().getX() == lastPlayerPos.getX() &&
			m.getPlayerLoc().getY() == lastPlayerPos.getY() )
			return;
		
		Component[] components = mazeGridComp[lastPlayerPos.getX()][lastPlayerPos.getY()].getComponentsInLayer(0);
		//if the JLayeredPane consists of move then one element, the top layer should be the player
		mazeGridComp[lastPlayerPos.getX()][lastPlayerPos.getY()].remove(0);
		
		if (!m.playerDied()) {
			mazeGridComp[m.getPlayerLoc().getX()][m.getPlayerLoc().getY()].add(components[0],0);
		}
		
		lastPlayerPos = m.getPlayerLoc();
	}
	
	//Initilise maze GUI and pack it
	public void init(Maze m) 
	{
		//Clear panels
		mazeGrid.removeAll();
		sidePanel.removeAll();
		
		//Make new GridBagLayout for maze itself
		mazeGrid.setLayout(new GridLayout(width, height));
		
		//Constraints
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		
		//Fill All blocks
		for (int y = 0; y < height; y++)
		{
			gbc.gridx = -y;	//update grid y pos
			for (int x = 0; x < width; x++)
			{
		
				gbc.gridy = x; //update grid x pos
				
				//Get information about current tile
				Tile t = m.getTile(x, y);
				
				//Use JLayeredPane for each block
				JLayeredPane block = new JLayeredPane();
				this.mazeGridComp[x][y] = block;	//set mazegrid to represent this block
				
				//Set size and layout
				block.setPreferredSize(blockSize);
				block.setLayout(new OverlayLayout(block));
				
				String blockSprite = "";	//base (bottom) block sprite
				String overLaySprite = "";	//Overlay sprite that goes on top of block sprite
				
				//Determine block graphics based on type of tile
				//If player is at this tile
				if (m.getPlayerTile().equals(t)) {
					blockSprite = this.pathSprite;
					overLaySprite = this.playerSprite;
					
					this.lastPlayerPos = t; //update last position
				}
				//Check if enemy unit
				else if (m.getEnemyTile().equals(t)) {
					blockSprite = this.pathSprite;
					overLaySprite = this.enemySprite;
				}
				//Check if this is a door
				else if (t.getType() == Tile.DOOR) {
					blockSprite = this.wallSprite;
					overLaySprite = this.doorSprite;
				}
				//Check for key
				else if (t.getType() == Tile.KEY) {
					blockSprite = this.pathSprite;
					overLaySprite = this.keySprite;
				}
				else if (t.getType() == Tile.TREASURE) {
					blockSprite = this.pathSprite;
					overLaySprite = this.coinSprite;
				}
				else if (t.getType() == Tile.SWORD){
					blockSprite = this.pathSprite;
					overLaySprite = this.swordSprite;
				}
				//Else if walkable path
				else if (t.getType() == Tile.PATH) {
					blockSprite = this.pathSprite;
				} 
				//Else must be wall
				else if (t.getType() == Tile.WALL){
					blockSprite = this.wallSprite;
				}
			

				//Always add block sprite
				JLabel spriteImage = new JLabel(sprites.get(blockSprite).getPlayerSprite());
				block.add(spriteImage, 1);
				
				//Add overlay sprite if required
				if (overLaySprite != "") {
					JLabel overlayImage = new JLabel(sprites.get(overLaySprite).getPlayerSprite());
					block.add(overlayImage, 0);
				}
				
				//Add block as hole to maze
				mazeGrid.add(block, gbc);
			}
		}
		
		//Add maze to this frame
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.CENTER;
		this.add(mazeGrid, gbc);
		
		//Create SidePanel components using gridbag layout 
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		//Add Score Panel
		JPanel scorePanel = new JPanel(new GridLayout(2,1));
		scorePanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		
		//Add player image
		PlayerPanel player = new PlayerPanel(this.playerSprite);	
		JLabel playerImage = new JLabel(player.getPlayerSprite());
		playerImage.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		scorePanel.add(playerImage);
		
		
		//Add Score for player
		JPanel playerScore = new JPanel(new GridLayout(3,1));
		JLabel name = new JLabel("Name: " + g.getPlayer().getName());
		JLabel character = new JLabel("Character: " + g.getPlayer().getCharacter().substring(0, 1).toUpperCase() + g.getPlayer().getCharacter().substring(1));
		JLabel score = new JLabel("Score: " + Integer.toString(g.getScore()));
		
		Font font = new Font("Arial", Font.PLAIN, 16);
		name.setFont(font);
		character.setFont(font);
		score.setFont(new Font("Arial", Font.BOLD, 18));
		
		playerScore.add(name);
		playerScore.add(character);
		playerScore.add(score);
		
		scorePanel.add(playerScore);
		
		sidePanel.add(scorePanel);
		
		gbc.gridx = 0;
		gbc.gridy = -2;
		
		//Add exit button at very bottom
		exitButton.setMargin(new Insets(5, 10, 5, 10));
		exitButton.setToolTipText("Click here to exit to main menu.");
		sidePanel.add(exitButton, gbc);

		
		//Add sidePanel to this frame
		gbc.gridx = 1;
		gbc.gridy = 0;
		this.add(sidePanel, gbc);
		
		//Repack frame
		this.pack();
	}
	
}
