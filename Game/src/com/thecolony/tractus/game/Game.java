package com.thecolony.tractus.game;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.ui.Picture;
import com.thecolony.tractus.audio.AudioManager;
import com.thecolony.tractus.graphics.GUI.ScrollText;
import com.thecolony.tractus.graphics.GraphicsManager;
import com.thecolony.tractus.worldgen.SpatialEntities.*;
import com.thecolony.tractus.networking.ClientMain;
import com.thecolony.tractus.player.Player;
import com.thecolony.tractus.military.battle.BattleObject;
import com.thecolony.tractus.military.battle.FlotillaBattler;
import com.thecolony.tractus.military.ships.Flotilla;
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
    private final float M_ATTACK_DISTANCE = 50.0f;
    private Planet[] mPlanets;
    private Star[] mSuns;
    private ArrayList<Ship> loneShips;
    private ArrayList<Flotilla> flotillas;
    private Node planetsNode;
    private Node starsNode;
    private Node loneShipsNode;
    private Node flotillasNode;
    private Node mSelectedShipsNode;
    private Node mSelectedFlotillasNode;
    private Vector3f mSelectedNodeCenterPos;
    private Plane mMovementPlane;
    private boolean mIsShiftPressed;
    private boolean isMoveToggleOn;
    private boolean isRotateToggleOn;
    private boolean isBoxSelectToggleOn;
    private boolean isAttackToggleOn;
    private JmeCursor mCursorSmiley;
    private Picture mPictureBoxSelect;
    private Picture mOverlay;
    private ScrollText mInfoHubText;
    private boolean isRunning;
    private ClientMain client;
    private ArrayList<FlotillaBattler> flotillaBattles;

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// START INITIALIZATION METHODS /////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Game()
    {
        super(new FlyCamAppState());

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

        GameLoader loader = new GameLoader(assetManager, inputManager, guiNode, rootNode, guiFont, M_WIDTH, M_HEIGHT);
        unpack(loader.loadGame());
        initializeListeners();
    }

    public void unpack(Object[] arr)
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
        mSelectedShipsNode = (arr[index] instanceof Node) ? (Node) arr[index++] : null;
        loneShipsNode = (arr[index] instanceof Node) ? (Node) arr[index++] : null;
        loneShips = (arr[index] instanceof ArrayList) ? (ArrayList<Ship>) arr[index++] : null;
        mSelectedFlotillasNode = (arr[index] instanceof Node) ? (Node) arr[index++] : null;
        flotillasNode = (arr[index] instanceof Node) ? (Node) arr[index++] : null;
        flotillas = (arr[index] instanceof ArrayList) ? (ArrayList<Flotilla>) arr[index++] : null;
        flotillaBattles = (arr[index] instanceof ArrayList) ? (ArrayList<FlotillaBattler>) arr[index++] : null;
        mSelectedNodeCenterPos = (arr[15] instanceof Vector3f) ? (Vector3f) arr[index++] : null;
        mMovementPlane = (arr[index] instanceof Plane) ? (Plane) arr[index++] : null;
        mCursorSmiley = (arr[index] instanceof JmeCursor) ? (JmeCursor) arr[index++] : null;
        mPictureBoxSelect = (arr[index] instanceof Picture) ? (Picture) arr[index++] : null;
        mOverlay = (arr[index] instanceof Picture) ? (Picture) arr[index++] : null;
        mInfoHubText = (arr[index] instanceof ScrollText) ? (ScrollText) arr[index++] : null;
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

        inputManager.addMapping("Right Click", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("Left Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(mMouseActionListener, new String[]
        {
            "Right Click", "Left Click"
        });

        inputManager.addMapping("Shift", new KeyTrigger(KeyInput.KEY_LSHIFT), new KeyTrigger(KeyInput.KEY_RSHIFT));
        inputManager.addMapping("Move", new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("Rotate", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("Box Select", new KeyTrigger(KeyInput.KEY_B));
        inputManager.addMapping("Attack", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Compress", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addMapping("Decompress", new KeyTrigger(KeyInput.KEY_X));
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_BACK));
        inputManager.addMapping("More Ships", new KeyTrigger(KeyInput.KEY_SPACE));

        inputManager.addMapping("Scroll Up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Scroll Down", new KeyTrigger(KeyInput.KEY_DOWN));

        inputManager.addListener(mKeyboardActionListener, new String[]
        {
            "Shift", "Move", "Rotate", "Box Select",
            "Attack", "Pause", "Exit", "Scroll Up",
            "Scroll Down", "More Ships"
        });
        inputManager.addListener(mKeyboardAnalogListener, new String[]
        {
            "Compress", "Decompress"
        });
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
                {
                    clearSelectedObjects();
                }

                Ray r = getMouseRay();

                boolean selectedSomething = false;
                for (int i = 0; i < loneShips.size(); i++)
                {
                    Ship s = loneShips.get(i);
                    selectedSomething = ((BoundingBox) s.getMoveableObject3d().getModel().getWorldBound()).intersects(r);
                    if (selectedSomething)
                    {
                        addSelectedObject(s);
                        break;
                    }
                }
                if (!selectedSomething)
                {
                    clearSelectedObjects();
                }
            }

            if (name.equals("Left Click") && isPressed)
            {
                if (isMoveToggleOn || isRotateToggleOn)
                {
                    Vector3f directionalVector = getMouseRayIntersectionPoint().subtract(mSelectedNodeCenterPos);

                    for (int i = 0; i < loneShips.size(); i++)
                    {
                        Ship s = loneShips.get(i);
                        if (s.isSelected())
                        {
                            s.setTargetPoint(s.getPosition().add(directionalVector), isMoveToggleOn);
                        }
                    }

                    isMoveToggleOn = isRotateToggleOn = false;
                    inputManager.setMouseCursor(null);
                }
                else
                {
                    if (isBoxSelectToggleOn)
                    {
                        Vector2f cursorPos = inputManager.getCursorPosition();
                        mPictureBoxSelect.setUserData("Initial Position", cursorPos.clone());
                        guiNode.attachChild(mPictureBoxSelect);
                    }
                }
            }
            else
            {
                if (name.equals("Left Click") && !isPressed)
                {
                    if (isBoxSelectToggleOn)
                    {
                        clearSelectedObjects();

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
                                addSelectedObject(loneShips.get(i));
                            }
                        }

                        guiNode.detachChild(mPictureBoxSelect);
                        isBoxSelectToggleOn = false;
                        inputManager.setMouseCursor(null);
                        flyCam.setEnabled(true);
                    }
                }
            }
            if (name.equals("Right Click") && isPressed)
            {
                if (!mIsShiftPressed)
                {
                    clearSelectedObjects();
                }

                Ray r = getMouseRay();

                boolean selectedSomething = false;
                for (int i = 0; i < flotillas.size(); i++)
                {
                    Flotilla f = flotillas.get(i);
                    selectedSomething = f.getBoundingBox().intersects(r);
                    if (selectedSomething)
                    {
                        addSelectedObject(f);
                        break;
                    }
                }
                if (!selectedSomething)
                {
                    clearSelectedObjects();
                }
            }

            if (name.equals("Left Click") && isPressed)
            {
                if (isMoveToggleOn || isRotateToggleOn)
                {
                    Vector3f directionalVector = getMouseRayIntersectionPoint().subtract(mSelectedNodeCenterPos);
                    for (int i = 0; i < flotillas.size(); i++)
                    {
                        Flotilla f = flotillas.get(i);
                        if (f.isSelected())
                        {
                            f.setTargetPoint(f.getCenterPosition().add(directionalVector), isMoveToggleOn);
                        }
                    }

                    isMoveToggleOn = isRotateToggleOn = false;
                    inputManager.setMouseCursor(null);
                }
                else
                {
                    if (isAttackToggleOn)
                    {
                        Ray r = getMouseRay();
                        for (int i = 0; i < flotillas.size(); i++)
                        {
                            Flotilla f = flotillas.get(i);
                            if (f.isSelected())
                            {
                                break;
                            }

                            boolean hover = f.getBoundingBox().intersects(r);
                            if (hover)
                            {
                                Vector3f targetDirection = f.getCenterPosition().subtract(mSelectedNodeCenterPos);
                                float change = M_ATTACK_DISTANCE / targetDirection.length();
                                Vector3f targetPoint = f.getCenterPosition().clone().interpolate(mSelectedNodeCenterPos, change);

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
                            if (name.equals("Attack") && mSelectedFlotillasNode.getQuantity() > 0 && isPressed)
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
                if (mSelectedShipsNode.getQuantity() > 1)
                {
                    float speed = M_COMPRESS_SPEED;
                    if (name.equals("Decompress"))
                    {
                        speed *= -1;
                    }

                    for (int i = 0; i < loneShips.size(); i++)
                    {
                        Ship s = loneShips.get(i);
                        if (s.isSelected() && !s.isTransforming())
                        {
                            Vector3f direction = mSelectedNodeCenterPos.subtract(s.getPosition()).normalize();
                            s.move(direction.mult(speed * tpf));
                        }
                    }
                }
            }
            if (name.equals("Compress") || name.equals("Decompress"))
            {
                if (mSelectedFlotillasNode.getQuantity() > 1)
                {
                    float speed = M_COMPRESS_SPEED;
                    if (name.equals("Decompress"))
                    {
                        speed *= -1;
                    }

                    for (int i = 0; i < flotillas.size(); i++)
                    {
                        Flotilla f = flotillas.get(i);
                        if (f.isSelected() && !f.isTransforming())
                        {
                            Vector3f direction = mSelectedNodeCenterPos.subtract(f.getCenterPosition()).normalize();
                            f.move(direction.mult(speed * tpf));
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

            // Update center position of selected ships...
            calculateCenterPoint();


            Ray r = getMouseRay();
            boolean somethingSelected = false;

            // Check if mouse hovering over anything to update info hub...
            for (int i = 0; i < loneShips.size(); i++)
            {
                somethingSelected = loneShips.get(i).getMoveableObject3d().getModel().getWorldBound().intersects(r);
                if (somethingSelected)
                {
                    mInfoHubText.clearText();
                    mInfoHubText.addText(loneShips.get(i).getDisplayInfo());
                    break;
                }
            }
            if (!somethingSelected)
            {
                // Check if mouse hovering over anything to update info hub...
                for (int i = 0; i < flotillas.size(); i++)
                {
                    somethingSelected = flotillas.get(i).getBoundingBox().intersects(r);
                    if (somethingSelected)
                    {
                        mInfoHubText.clearText();
                        mInfoHubText.addText(flotillas.get(i).getDisplayInfo());
                        break;
                    }
                }
            }
            if (!somethingSelected) // Check Planets and Stars...
            {
                for (int i = 0; i < mPlanets.length; i++)
                {
                    Planet p = mPlanets[i];
                    somethingSelected = p.getBoundingSphere().intersects(r);
                    if (somethingSelected)
                    {
                        mInfoHubText.clearText();
                        mInfoHubText.addText(p.getDisplayInfo());
                        break;
                    }
                }

                if (!somethingSelected)
                {
                    for (int i = 0; i < mSuns.length; i++)
                    {
                        Star s = mSuns[i];
                        somethingSelected = s.getBoundingSphere().intersects(r);
                        if (somethingSelected)
                        {
                            mInfoHubText.clearText();
                            mInfoHubText.addText(s.getDisplayInfo());
                            break;
                        }
                    }
                }
            }
            if (!somethingSelected)
            {
                mInfoHubText.clearText();
            }

            // Update battles...
            for (int i = 0; i < flotillaBattles.size(); i++)
            {
                int battle = flotillaBattles.get(i).update(tpf);
                if (battle != 0)
                {
                    flotillaBattles.remove(i--);
                }
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
     * Adds an object to the selected object node.
     *
     * @param ship The reference to the ship that has been selected.
     */
    private void addSelectedObject(Ship ship)
    {
        mSelectedShipsNode.attachChild(ship.getWireBoxGeometry());

        ship.setIsSelected(true);

        calculateCenterPoint();
    }

    public void addSelectedObject(Flotilla flotilla)
    {
        mSelectedFlotillasNode.attachChild(flotilla.getWireBoxGeometry());

        flotilla.setIsSelected(true);

        calculateCenterPoint();
    }

    /**
     * Clears all of the data in the selected objects node.
     */
    private void clearSelectedObjects()
    {
        for (int i = 0; i < loneShips.size(); i++)
        {
            loneShips.get(i).setIsSelected(false);
        }

        mSelectedShipsNode.detachAllChildren();

        for (int i = 0; i < flotillas.size(); i++)
        {
            flotillas.get(i).setIsSelected(false);
        }

        mSelectedFlotillasNode.detachAllChildren();
    }

    private void calculateCenterPoint()
    {
        ArrayList<Vector3f> points = new ArrayList<Vector3f>();
        for (int i = 0; i < loneShips.size(); i++)
        {
            Ship s = loneShips.get(i);
            if (s.isSelected())
            {
                points.add(s.getPosition());
            }
        }
        for (int i = 0; i < flotillas.size(); i++)
        {
            Flotilla f = flotillas.get(i);
            if (f.isSelected())
            {
                points.add(f.getCenterPosition());
            }
        }

        Vector3f[] p = new Vector3f[points.size()];
        for (int i = 0; i < points.size(); i++)
        {
            p[i] = points.get(i);
        }
        mSelectedNodeCenterPos = getCenterPoint(p);
    }

    private Vector3f getCenterPoint(Vector3f[] points)
    {
        float x = 0.0f;
        float z = 0.0f;

        int size = points.length;
        for (int i = 0; i < size; i++)
        {
            x += points[i].x;
            z += points[i].z;
        }

        x /= size;
        z /= size;

        return new Vector3f(x, 0.0f, z);
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////
// END UPDATE METHODS ///////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
}