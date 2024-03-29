import java.awt.GraphicsConfiguration;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.*;

import src.*;
/*
*   Class Game which is the main class for the game 2048
*   By running make in cmd the game will initiate with this class
*   main method.
*/
public class Game extends JFrame implements MouseListener, KeyListener {

    /*
    *   Private variables related to a Game object
    *   The game object manages the creation of the base
    *   components such as the GameBoard, Settings, Tutorial,
    *   LayoutHandler, and CommandManager.
    *   These are then passed along to sub components which
    *   needs the Components in different ways.
    */
    private GameBoard gb;
    private Settings st;
    private SettingsScreen sts;
    private Tutorial tut;
    private TutorialScreen tutS;

    private LayoutHandler lh;
    private CommandManager commandManager;

    /*
    *   Base constructor
    */
    public Game() {
        setTitle("2048");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();

        this.setBackground(Color.WHITE);

        this.setLocation(150, 150);
        this.setMinimumSize(new Dimension(500, 500));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        width = getWidth();
        int height = getHeight();
        int recHei = height / 8;

        this.gb = new GameBoard(4);
        this.st = new Settings();
        this.sts = new SettingsScreen(this.st);
        this.tut = new Tutorial(this.st);
        this.tutS = new TutorialScreen(this.st);

        this.lh = new LayoutHandler(this.gb, this.st, this.sts, this.tut, this.tutS, width);
        this.lh.setOpaque(true);
        this.setContentPane(this.lh);

        this.commandManager = new CommandManager();

        addMouseListener(this);
        addKeyListener(this);

        this.pack();
        this.setVisible(true);
    }

    public Game getGame() {
        return this;
    }

    /*
    *   Override function to handle mouseclicks inside the
    *   window.
    *   -- Settings pressed = Open Settings
    *   -- Tutorial pressed = Start Tutorial
    */
    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        y = y - this.getInsets().top;

        if (!this.st.getActiveMenu() && (!(this.tut.getActive()))) {
            if(this.gb.checkInsideGB(x,y)) {
            }
            else if (this.st.checkInsideSetting(x,y)) {
                this.lh.setActiveMenu();
            }
            else if (this.tut.checkInsideTutorial(x,y)) {
                this.tut.changeActive();
                this.lh.setTutorialActive();
                this.tutS.nextPhase();
                this.initiateTutorial();

                AnimatedActionListener taskPerformer = new AnimatedActionListener(this.tutS, this.lh);
                Timer timer = new Timer(25 ,taskPerformer);

                taskPerformer.setTimer(timer);

                timer.setRepeats(true);
                timer.start();


                this.revalidate();
                this.repaint();
            }
        }
        else if (this.st.getActiveMenu()){
            int checkVal = this.sts.checkInsideSTS(x,y);
            switch (checkVal) {
                case 0:
                    if (this.st.getLanguage() == 0) {
                        this.st.setLanguage(1);
                    }
                    else {
                        this.st.setLanguage(0);
                    }
                    this.revalidate();
                    this.repaint();
                    break;
                case 1:
                    this.st.changeSound();
                    this.revalidate();
                    this.repaint();
                    break;
                case 2:
                    this.initiateGame();
                    this.lh.disableActiveMenu();
                    this.st.setActiveMenu(false);
                    this.revalidate();
                    this.repaint();
                    break;
                default:
                    this.lh.disableActiveMenu();
                    this.st.setActiveMenu(false);
                    break;
            }
        }
        else {
            //TODO: Run Tutorial
            /*this.tutS.nextPhase();

            if (this.tutS.getPhase() == 0) {
                this.tut.changeActive();
                this.lh.disableTutorialActive();
            }
            this.revalidate();
            this.repaint();*/
        }
    }

    public void initiateGame() {
        this.gb.resetState();

        this.commandManager.executeCommand(new SpawnTile(this.gb.state, this.lh));
        this.commandManager.executeCommand(new SpawnTile(this.gb.state, this.lh));

        this.commandManager.clearUndos();

        this.lh.drawTiles();
    }

    public void initiateTutorial() {
        this.gb.resetState();

        Tile tile1 = new Tile(0, 2, 2);
        Tile tile2 = new Tile(3, 2, 2);

        this.gb.state.addTile(0, 2, tile1);
        this.gb.state.addTile(3, 2, tile2);

        this.lh.addTileToLayout(tile1);
        this.lh.addTileToLayout(tile2);

        this.lh.drawTiles();
    }

    /*
    *   Function move which handles the operations when a
    *   button in a direction has been pressed
    */
    public void move(Direction direction) {
        // Should use Swipe in `direction`, and then `SpawnTile`
        // gb.state should as a side effect be updated by the executed commands
        Swipe swipe = new Swipe(direction, this.gb.state, this.st, this.lh);
        commandManager.executeCommand(swipe);
        if (swipe.stateChanged()) {
            commandManager.executeCommand(new SpawnTile(this.gb.state, this.lh));
            if (gameOver()) {
                System.out.println("Game over! Press 'N' to start a new game.");
            }
        } else commandManager.undoCommand();
    }

    /*
    *   Override function necessary for KeyListener implement
    */
    @Override
    public void keyTyped(KeyEvent e) {}

    /*
    *   Override function for Key pressed handler
    *   -- Right Key pressed    = Swipe Right
    *   -- Left Key pressed     = Swipe Left
    *   -- Up Key pressed       = Swipe Up
    *   -- Down Key pressed     = Swipe Down
    */
    @Override
    public void keyPressed(KeyEvent arrow){
        if (!this.st.getActiveMenu() && (!(this.tut.getActive()))) {
            switch (arrow.getKeyCode()){
                case KeyEvent.VK_UP:
                    move(Direction.UP);
                    this.lh.drawTiles();
                    break;
                case KeyEvent.VK_DOWN:
                    move(Direction.DOWN);
                    this.lh.drawTiles();
                    break;
                case KeyEvent.VK_RIGHT:
                    move(Direction.RIGHT);
                    this.lh.drawTiles();
                    break;
                case KeyEvent.VK_LEFT:
                    move(Direction.LEFT);
                    this.lh.drawTiles();
                    break;
                case KeyEvent.VK_R:
                    if(commandManager.isRedoAvailable()) {
                        commandManager.redoCommand();
                        commandManager.redoCommand();
                        this.lh.drawTiles();
                    }
                    break;
                case KeyEvent.VK_U:
                    if (commandManager.isUndoAvailable()) {
                        commandManager.undoCommand();
                        commandManager.undoCommand();
                        this.lh.drawTiles();
                        this.lh.drawTiles(); // Without running this twice, sometimes not all tiles are drawn on undo
                    }
                    break;
                case KeyEvent.VK_N:
                    initiateGame();
            }
        }
        else if (this.tut.getActive()) {
            //this.tutS.checkPhase(arrow.getKeyCode(), getGame(), this.lh, this.tut, this.commandManager);
            switch (this.tutS.getPhase()) {
                case 1:
                    if (arrow.getKeyCode() == KeyEvent.VK_RIGHT) {
                        move(Direction.RIGHT);
                        this.lh.drawTiles();
                        this.tutS.nextPhase();
                        this.lh.disableArrowActive();
                    }
                    else if (arrow.getKeyCode() == KeyEvent.VK_LEFT) {
                        move(Direction.LEFT);
                        this.lh.drawTiles();
                        this.tutS.nextPhase();
                        this.lh.disableArrowActive();
                    }
                    break;
                case 2:
                    if (arrow.getKeyCode() == KeyEvent.VK_ENTER) {
                        this.tutS.nextPhase();
                    }
                    break;
                case 3:
                    if (arrow.getKeyCode() == KeyEvent.VK_U) {
                        commandManager.undoCommand();
                        commandManager.undoCommand();
                        this.lh.drawTiles();
                        this.lh.drawTiles(); // Without running this twice, sometimes not all tiles are drawn on undo
                        this.tutS.nextPhase();
                    }
                    break;
                case 4:
                    if (arrow.getKeyCode() == KeyEvent.VK_R) {
                        commandManager.redoCommand();
                        commandManager.redoCommand();
                        this.lh.drawTiles();
                        this.tutS.nextPhase();
                    }
                    break;
                case 5:
                    if (arrow.getKeyCode() == KeyEvent.VK_ENTER) {
                        this.tutS.nextPhase();
                    }
                    break;
            }
            if (this.tutS.getPhase() == 0) {
                this.tut.changeActive();
                this.lh.disableTutorialActive();
            }
            this.revalidate();
            this.repaint();
        }
    }

    /*
    *   Override function necessary for KeyListener implement
    */
    @Override
    public void keyReleased(KeyEvent e) {
    }

    /*
    *   Checks if undo is available in CommandManager.
    */
    public boolean isUndoAvailable() {
        return commandManager.isUndoAvailable();
    }

    /*
    *   Calls undo function in CommandManager.
    */
    public void undo() {
        commandManager.undoCommand();
    }

    /*
    *   Checks if redo is available in CommandManager.
    */
    public boolean isRedoAvailable() {
        return commandManager.isRedoAvailable();
    }

    /*
    *   Calls redo function in CommandManager.
    */
    public void redo() {
        commandManager.redoCommand();
    }


    /*
    *   Override function necessary for MouseListener implement
    */
    @Override
    public void mouseEntered(MouseEvent e) {}

    /*
    *   Override function necessary for MouseListener implement
    */
    @Override
    public void mouseExited(MouseEvent e) {}

    /*
    *   Override function necessary for MouseListener implement
    */
    @Override
    public void mousePressed(MouseEvent e) {}

    /*
    *   Override function necessary for MouseListener implement
    */
    @Override
    public void mouseReleased(MouseEvent e) {}

    /*
    *   Function to check if a winning situation has occurred?
    *   A win should occur when a Tile has reached a value 2048.
    *   A loss should occur when the GameBoard is full and no,
    *   actions are possible.
    */
    public boolean gameOver() {
        if (!gb.state.hasEmptyTile()) {
            for (Direction d : Direction.values()) {
                commandManager.executeCommand(new Swipe(d, gb.state, this.st, this.lh));
                if (gb.state.hasEmptyTile()) {
                    commandManager.undoCommand();
                    commandManager.clearRedos();
                    return false ;
                } else commandManager.undoCommand();
            };
            return true; //Skriv ut "Game Over!!!"
        } else return false;
    }

    /*
    *   Main function which runs the program
    */
    public static void main(String[] args) {
        Game frame = new Game();
    }
}
