package com.thecolony.tractus.game;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.ui.Picture;
import com.thecolony.tractus.audio.AudioManager;
import com.thecolony.tractus.economics.Firm;
import com.thecolony.tractus.economics.Market;
import com.thecolony.tractus.graphics.GUI.PauseMenu;
import com.thecolony.tractus.graphics.GUI.ScrollText;
import com.thecolony.tractus.graphics.GraphicsManager;
import com.thecolony.tractus.graphics.drawableobjects.DrawableObject3d;
import com.thecolony.tractus.input.InputLoader;
import com.thecolony.tractus.worldgen.SpatialEntities.*;
import com.thecolony.tractus.networking.ClientMain;
import com.thecolony.tractus.player.Player;
import com.thecolony.tractus.military.battle.BattleObject;
import com.thecolony.tractus.military.battle.FlotillaBattler;
import com.thecolony.tractus.military.battle.MoveableBattleObject;
import com.thecolony.tractus.military.ships.Flotilla;
import com.thecolony.tractus.military.ships.SelectedFamily;
import com.thecolony.tractus.military.ships.Ship;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Joe Pagliuco
 */
public class Game extends SimpleApplication
{

    public static int M_WIDTH, M_HEIGHT;
    private final float M_COMPRESS_SPEED = 1.0f;
    private final float M_ATTACK_DISTANCE = 75.0f;
    private final float M_PLANET_DISTANCE = 50.0f;
    private Planet[] mPlanets;
    private Market market;
    private Star[] mSuns;
    private ArrayList<Ship> loneShips;
    private ArrayList<Flotilla> flotillas;
    private Node planetsNode;
    private Node starsNode;
    private Node loneShipsNode;
    private Node flotillasNode;
    private SelectedFamily selectedObjects;
    private Plane mMovementPlane;
    private boolean loadSave;
    private boolean mIsShiftPressed;
    private boolean isMoveToggleOn;
    private boolean isRotateToggleOn;
    private boolean isBoxSelectToggleOn;
    private boolean isAttackToggleOn;
    private JmeCursor mCursorSmiley;
    private Picture mPictureBoxSelect;
    private ScrollText mInfoHubText;
    private boolean isRunning;
    private ClientMain client;
    private ArrayList<FlotillaBattler> flotillaBattles;
//<<<<<<< HEAD
    private int timer;
    
    private static final int economicTimeStep = 120;
//=======
    private PauseMenu pauseMenu;
    
    private float cameraSpeed = 75.0f;
    private Vector3f cameraTargetPoint;
    private boolean cameraMoving = false;
//>>>>>>> origin/master

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// START INITIALIZATION METHODS /////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Game()
    {
        this(false);
    }
    public Game (boolean loadSave){
        super(new FlyCamAppState());
        this.loadSave=loadSave;  //true==loadGame
        Logger.getLogger("").setLevel(Level.SEVERE);
    }

    @Override
    public void destroy()
    {
        if (client != null)
        {
            client.stop();
        }
        super.destroy();
    }

    public void addClient(ClientMain c)
    {
        client = c;
    }

    @Override
    public void simpleInitApp()
    {
        M_WIDTH = settings.getWidth();
        M_HEIGHT = settings.getHeight();

        inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        setDisplayFps(false);
        setDisplayStatView(false);
        isRunning = true;

        adjustCameraSettings();

        unpack(GameLoader.loadGame(loadSave, assetManager, inputManager, guiNode, rootNode, audioRenderer, guiViewPort, guiFont, M_WIDTH, M_HEIGHT));
        initializeListeners();
        
        pauseMenu = new PauseMenu(this);
    }

    public void unpack(Object[] arr) //Such a horrible, horrible method. If someone can think of anything better, please replace this monstrosity
    {
        int index = 0;
        rootNode = (arr[index] instanceof Node) ? (Node) arr[index++] : null;
        guiNode = (arr[index] instanceof Node) ? (Node) arr[index++] : null;
        guiFont = (arr[index] instanceof BitmapFont) ? (BitmapFont) arr[index++] : null;
        inputManager = (arr[index] instanceof InputManager) ? (InputManager) arr[index++] : null;
        planetsNode = (arr[index] instanceof Node) ? (Node) arr[index++] : null;
        mPlanets = (arr[index] instanceof Planet[]) ? (Planet[]) arr[index++] : null;
        starsNode = (arr[index] instanceof Node) ? (Node) arr[index++] : null;
        mSuns = (arr[index] instanceof Star[]) ? (Star[]) arr[index++] : null;
        loneShipsNode = (arr[index] instanceof Node) ? (Node) arr[index++] : null;
        loneShips = (arr[index] instanceof ArrayList) ? (ArrayList<Ship>) arr[index++] : null;
        flotillasNode = (arr[index] instanceof Node) ? (Node) arr[index++] : null;
        flotillas = (arr[index] instanceof ArrayList) ? (ArrayList<Flotilla>) arr[index++] : null;
        flotillaBattles = (arr[index] instanceof ArrayList) ? (ArrayList<FlotillaBattler>) arr[index++] : null;
        mMovementPlane = (arr[index] instanceof Plane) ? (Plane) arr[index++] : null;
        mCursorSmiley = (arr[index] instanceof JmeCursor) ? (JmeCursor) arr[index++] : null;
        mPictureBoxSelect = (arr[index] instanceof Picture) ? (Picture) arr[index++] : null;
        mInfoHubText = (arr[index] instanceof ScrollText) ? (ScrollText) arr[index++] : null;
        selectedObjects = (arr[index] instanceof SelectedFamily) ? (SelectedFamily) arr[index++] : null;
        market = (arr[index] instanceof Market) ? (Market) arr[index++] : null;
        // I agree. This method is terrible. I'm not sure how to fix it though.
        // Maybe just create an instance of game in GameLoader and copy the data here?
    }

    private void adjustCameraSettings()
    {
        cam.setLocation(new Vector3f(75.0f, 75.0f, -75.0f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        cam.setFrustumPerspective((float) Math.toDegrees(FastMath.PI / 4.0f), (float) M_WIDTH / (float) M_HEIGHT, 0.1f, 2000.0f);

        flyCam.setDragToRotate(true);
        flyCam.setZoomSpeed(15.0f);
        flyCam.setMoveSpeed(50.0f);
    }

    private void initializeListeners()
    {
        mIsShiftPressed = false;
        isMoveToggleOn = false;
        isRotateToggleOn = false;
        isBoxSelectToggleOn = false;
        isAttackToggleOn = false;

        InputLoader.loadInputMappings(inputManager);
        
        inputManager.addListener(mMouseActionListener, InputLoader.getMouseActionMappings());
        inputManager.addListener(mKeyboardActionListener, InputLoader.getKeyboardActionMappings());
        inputManager.addListener(mKeyboardAnalogListener, InputLoader.getKeyboardAnalogMappings());
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////
// END INITIALIZATION METHODS ///////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
// START LISTENERS //////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    private ActionListener mMouseActionListener = new ActionListener()
    {
        public void onAction(String name, boolean isPressed, float tpf)
        {
            if (name.equals("Right Click") && isPressed)
            {
                if (!mIsShiftPressed)
                    selectedObjects.clearFamily();

                Ray r = getMouseRay();

                boolean selectedSomething = false;
                for (int i = 0; i < loneShips.size(); i++)
                {
                    Ship s = loneShips.get(i);
                    selectedSomething = ((BoundingBox) s.getMoveableObject3d().getModel().getWorldBound()).intersects(r);
                    if (selectedSomething)
                    {
                        selectedObjects.addObjects(s);
                        break;
                    }
                }

                if (!selectedSomething)
                {
                    for (int i = 0; i < flotillas.size(); i++)
                    {
                        Flotilla f = flotillas.get(i);
                        selectedSomething = f.getBoundingBox().intersects(r);
                        if (selectedSomething)
                        {
                            selectedObjects.addObjects(f);
                            break;
                        }
                    }
                }

                if (!selectedSomething)
                    selectedObjects.clearFamily();
            }

            if (name.equals("Left Click") && isPressed)
            {
                if (isMoveToggleOn || isRotateToggleOn)
                {
                    selectedObjects.setTargetPoint(getMouseRayIntersectionPoint(), isMoveToggleOn);

                    isMoveToggleOn = isRotateToggleOn = false;
                    inputManager.setMouseCursor(null);
                }
                else if (isAttackToggleOn)
                {
                    Ray r = getMouseRay();
                    for (int i = 0; i < flotillas.size(); i++)
                    {
                        Flotilla f = flotillas.get(i);
                        if (f.isSelected())
                        {
                            continue;
                        }

                        boolean hover = f.getBoundingBox().intersects(r);
                        if (hover)
                        {
                            Vector3f targetDirection = f.getCenterPosition().subtract(selectedObjects.getCenterPosition());
                            float change = M_ATTACK_DISTANCE / targetDirection.length();
                            Vector3f targetPoint = f.getCenterPosition().clone().interpolate(selectedObjects.getCenterPosition(), change);

                            f.setTargetPoint(targetPoint, false);

                            // Add attacker
                            Flotilla attacker = null;
                            for (int j = 0; j < flotillas.size(); j++)
                            {
                                Flotilla f2 = flotillas.get(j);
                                if (f2.isSelected())
                                {
                                    attacker = f2;
                                    f2.setTargetPoint(targetPoint, true);
                                    break;
                                }
                            }

                            flotillaBattles.add(new FlotillaBattler(attacker, f));
                        }
                    }

                    isAttackToggleOn = false;
                    inputManager.setMouseCursor(null);
                }
                else if (isBoxSelectToggleOn)
                {
                    Vector2f cursorPos = inputManager.getCursorPosition();
                    mPictureBoxSelect.setUserData("Initial Position", cursorPos.clone());
                    guiNode.attachChild(mPictureBoxSelect);
                }
                
                else
                {
                    Ray r = getMouseRay();
                    for (int i = 0; i < mPlanets.length; i++)
                    {
                        Planet p = mPlanets[i];
                        if (p.getBoundingSphere().intersects(r))
                        {
                            Vector3f targetDirection = p.getDrawableObject().getPosition().subtract(cam.getLocation());
                            float change = M_PLANET_DISTANCE / targetDirection.length();
                            cameraTargetPoint = p.getDrawableObject().getPosition().clone().interpolate(cam.getLocation(), change);
                            
                            cameraMoving = true;
                            
                            break;
                        }
                    }
                }
            }
            else
            {
                if (name.equals("Left Click") && !isPressed)
                {
                    if (isBoxSelectToggleOn)
                    {
                        if (!mIsShiftPressed)
                            selectedObjects.clearFamily();

                        // Create big ass selector cube...
                        Vector3f min = getMouseRayIntersectionPoint();
                        Vector3f max = Vector3f.ZERO;
                        Ray r = createRayFromPoint((Vector2f) mPictureBoxSelect.getUserData("Initial Position"));
                        r.intersectsWherePlane(mMovementPlane, max);
                        Box bigAssSelectorCube = new Box(min, max);

                        // Loop through ships to see if they're in the selected area...
                        for (int i = 0; i < loneShips.size(); i++)
                        {
                            BoundingVolume shipBound = loneShips.get(i).getMoveableObject3d().getModel().getWorldBound();
                            if (bigAssSelectorCube.getBound().intersects(shipBound))
                            {
                                selectedObjects.addObjects(loneShips.get(i));
                            }
                        }
                        for (int i = 0; i < flotillas.size(); i++)
                        {
                            if (bigAssSelectorCube.getBound().intersectsBoundingBox(flotillas.get(i).getBoundingBox()))
                            {
                                selectedObjects.addObjects(flotillas.get(i));
                            }
                        }

                        guiNode.detachChild(mPictureBoxSelect);
                        isBoxSelectToggleOn = false;
                        inputManager.setMouseCursor(null);
                        flyCam.setEnabled(true);
                    }
                }
            }
        }
    };
    private ActionListener mKeyboardActionListener = new ActionListener()
    {
        public void onAction(String name, boolean isPressed, float tpf)
        {
            if (name.equals("Pause") && isPressed)
            {
                isRunning = !isRunning;

                flyCam.setEnabled(isRunning);
                
                if (isRunning)
                    stateManager.detach(pauseMenu);
                else
                    stateManager.attach(pauseMenu);
            }

            if (isRunning)
            {
                if (name.equals("More Ships") && isPressed)
                {
                    AudioManager.getCreate().playInstance();
                    AudioManager.getHammer().playInstance();
                    AudioManager.getDCWielding().playInstance();
                    AudioManager.getGearBursts().playInstance();
                    double[] stats = new double[19];
                    stats[BattleObject.BATTLE_STAT_MOVEMENT_SPEED] = 5.0;

                    loneShips.add(new Ship(new Player(4), Ship.SHIP_TYPE.Fighter, "Fighter " + loneShips.size(), loneShipsNode, new Vector3f(0.0f, 0.0f, -(30 + loneShips.size() * 3)),
                            stats, 0, 0, 0, 0.0));
                    loneShips.get(loneShips.size() - 1).getMoveableObject3d().getModel().setMaterial(GraphicsManager.generateMaterial(loneShips.get(loneShips.size() - 1).getPlayer().getColor()));
                }

                if (name.equals("Shift"))
                {
                    mIsShiftPressed = isPressed;
                }


                if (name.equals("Move") && isPressed)
                {
                    isMoveToggleOn = !isMoveToggleOn;
                    isRotateToggleOn = false;
                    isBoxSelectToggleOn = false;
                    isAttackToggleOn = false;
                }
                else
                {
                    if (name.equals("Rotate") && isPressed)
                    {
                        isMoveToggleOn = false;
                        isRotateToggleOn = !isRotateToggleOn;
                        isBoxSelectToggleOn = false;
                        isAttackToggleOn = false;
                    }
                    else
                    {
                        if (name.equals("Box Select") && isPressed)
                        {
                            isMoveToggleOn = false;
                            isRotateToggleOn = false;
                            isBoxSelectToggleOn = !isBoxSelectToggleOn;
                            isAttackToggleOn = false;

                            flyCam.setEnabled(!flyCam.isEnabled());
                        }
                        else
                        {
                            if (name.equals("Attack") && selectedObjects.getFamily().size() > 0 && isPressed)
                            {
                                isMoveToggleOn = false;
                                isRotateToggleOn = false;
                                isBoxSelectToggleOn = false;
                                isAttackToggleOn = !isAttackToggleOn;
                            }
                        }
                    }
                }


                if (isMoveToggleOn || isRotateToggleOn || isBoxSelectToggleOn || isAttackToggleOn)
                {
                    inputManager.setMouseCursor(mCursorSmiley);
                }
                else
                {
                    inputManager.setMouseCursor(null);
                }

                if (name.contains("Scroll") && isPressed)
                {
                    mInfoHubText.scroll(name.contains("Up"));
                }
            }

            if (name.equals("Exit"))
            {
                stop();
            }
        }
    };
    private AnalogListener mKeyboardAnalogListener = new AnalogListener()
    {
        public void onAnalog(String name, float value, float tpf)
        {
            if (name.equals("Compress") || name.equals("Decompress"))
            {
                if (selectedObjects.getFamily().size() > 1)
                {
                    float speed = M_COMPRESS_SPEED * ((name.charAt(0) == 'D') ? -1 : 1);

                    for (int i = 0; i < selectedObjects.getFamily().size(); i++)
                    {
                        MoveableBattleObject b = selectedObjects.getFamily().get(i);
                        if (b.isSelected() && !b.isTransforming())
                        {
                            Vector3f direction = selectedObjects.getCenterPosition().subtract(b.getPosition()).normalize();
                            b.move(direction.mult(speed * tpf));
                        }
                    }
                }
            }
        }
    };

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// END LISTENERS ////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
// START UPDATE METHODS /////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void simpleUpdate(float tpf)
    {
        if (isRunning)
        {
            // Update box select picture as needed...
            if (isBoxSelectToggleOn)
            {
                Vector2f initPos = mPictureBoxSelect.getUserData("Initial Position");
                Vector2f cursorPos = inputManager.getCursorPosition();
                if (initPos.x < cursorPos.x && initPos.y > cursorPos.y)
                {
                    mPictureBoxSelect.setPosition(initPos.x, cursorPos.y);
                }
                else
                {
                    if (initPos.x > cursorPos.x && initPos.y < cursorPos.y)
                    {
                        mPictureBoxSelect.setPosition(cursorPos.x, initPos.y);
                    }
                    else
                    {
                        if (initPos.x > cursorPos.x && initPos.y > cursorPos.y)
                        {
                            mPictureBoxSelect.setPosition(cursorPos.x, cursorPos.y);
                        }
                        else
                        {
                            mPictureBoxSelect.setPosition(initPos.x, initPos.y);
                        }
                    }
                }

                mPictureBoxSelect.setWidth(Math.abs(cursorPos.x - initPos.x));
                mPictureBoxSelect.setHeight(Math.abs(initPos.y - cursorPos.y));
            }
            
            selectedObjects.update(tpf);

            // Update ships...
            for (int i = 0; i < loneShips.size(); i++)
            {
                loneShips.get(i).update(tpf);
            }
            // Update flotillas...
            for (int i = 0; i < flotillas.size(); i++)
            {
                flotillas.get(i).update(tpf);
            }
            // Update firms
            if(timer < economicTimeStep)
                timer++;
            else
            {
                timer = 0;
                for(Planet p: mPlanets)
                {
                    for(Firm f: p.getFirms())
                        f.step();
                }
            }
            for (int i = 0; i < mPlanets.length; i++)
                mPlanets[i].update();


            Ray r = getMouseRay();
            boolean somethingSelected = false;

            // Check if mouse hovering over anything to update info hub...
            for (int i = 0; i < loneShips.size(); i++)
            {
                somethingSelected = checkTextUpdates(loneShips.get(i).getMoveableObject3d(), false);
                if (somethingSelected)
                    break;
            }
            if (!somethingSelected)
            {
                // Check if mouse hovering over anything to update info hub...
                for (int i = 0; i < flotillas.size(); i++)
                {
                    somethingSelected = checkTextUpdates(flotillas.get(i).getDrawableObject3d(), false);
                    if (somethingSelected)
                        break;
                }
            }
            if (!somethingSelected) // Check Planets and Stars...
            {
                for (int i = 0; i < mPlanets.length; i++)
                {
                    somethingSelected = checkTextUpdates(mPlanets[i].getDrawableObject(), true);
                    if (somethingSelected)
                        break;
                }
            }
            if (!somethingSelected)
            {
                for (int i = 0; i < mSuns.length; i++)
                {
                    somethingSelected = checkTextUpdates(mSuns[i].getDrawableObject(), true);
                    if (somethingSelected)
                        break;
                }
            }
            if (!somethingSelected)
                mInfoHubText.reset();

            // Update battles...
            for (int i = 0; i < flotillaBattles.size(); i++)
            {
                int battle = flotillaBattles.get(i).update(tpf);
                if (battle != 0)
                {
                    if (battle == -1)
                    {
                        Flotilla f = flotillaBattles.get(i).getDefender();
                        if (f.isSelected())
                            selectedObjects.removeObject(f);
                        flotillas.remove(f);
                    }
                    else if (battle == 1)
                    {
                        Flotilla f = flotillaBattles.get(i).getAttacker();
                        if (f.isSelected())
                            selectedObjects.removeObject(f);
                        flotillas.remove(f);
                    }
                    
                    flotillaBattles.remove(i--);
                }
            }
            
            if (cameraMoving)
            {
                cam.setLocation(cam.getLocation().add((cameraTargetPoint.subtract(cam.getLocation()).normalize()).mult(cameraSpeed * tpf)));
                if (cam.getLocation().distance(cameraTargetPoint) < 1.0f)
                    cameraMoving = false;
            }
        }
    }

    /**
     * Creates a ray based off the 2d coordinate.
     *
     * @param point The screen coordinates that make the base of the ray.
     * @return The created ray.
     */
    private Ray createRayFromPoint(Vector2f point)
    {
        Vector2f point2d = point;
        Vector3f point3d = cam.getWorldCoordinates(new Vector2f(point2d.x, point2d.y), 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(point2d.x, point2d.y), 1f).subtractLocal(point3d).normalizeLocal();
        return new Ray(point3d, dir);
    }

    /**
     * @return A ray from the current cursor position.
     */
    private Ray getMouseRay()
    {
        return createRayFromPoint(inputManager.getCursorPosition());
    }

    /**
     * @return The spot where the mouse intersects the plane at y = 0.
     */
    private Vector3f getMouseRayIntersectionPoint()
    {
        Ray r = getMouseRay();
        Vector3f v = new Vector3f();
        r.intersectsWherePlane(mMovementPlane, v);
        return v;
    }
    
    /**
     * Checks for mouse hover over object and updates text accordingly.
     * @param object The DrawableObject3d to check against.
     * @param useSphere true if the collision should use the model's attached
     * sphere (used for planets and stars).
     * @return true if mouse is hovering over object, false otherwise.
     */
    private boolean checkTextUpdates(DrawableObject3d object, boolean useSphere)
    {
        boolean somethingSelected;
        
        if (!useSphere)
            somethingSelected = object.getModel().getWorldBound().intersects(getMouseRay());
        else
            somethingSelected = ((BoundingSphere)object.getModel().getUserData("Bounding")).intersects(getMouseRay());
        
        if (somethingSelected)
        {
            mInfoHubText.clearText();
            mInfoHubText.addText((String)object.getModel().getUserData("Display Info"));
        }
        
        return somethingSelected;
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////
// END UPDATE METHODS ///////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static String hashtagFixMyStringGurl(String[] strings)
    {
        String returnString = "";
        for (int i = 0; i < strings.length; i++)
        {
            returnString += strings[i];
            if (i != strings.length - 1)
                returnString += "\n";
        }
        return returnString;
    }
}