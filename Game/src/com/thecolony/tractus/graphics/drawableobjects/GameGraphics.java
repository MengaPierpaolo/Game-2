package com.thecolony.tractus.graphics.drawableobjects;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A class that holds all the common objects used for graphics.
 * @author Joe Pagliuco
 */
public abstract class GameGraphics
{
    private static Spatial M_SHIP_FIGHTER;
    private static Spatial M_SHIP_FRIGATE;
    private static Spatial M_SHIP_CRUISER;
    private static Spatial M_SHIP_CAPITAL_SHIP;
    
    private static Material M_DEFAULT_WHITE_MATERIAL;
    
    public static void loadGraphics(AssetManager contentMan)
    {
        ClassLoader cl=ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs();
        for(URL url: urls){
            System.out.println(url.getFile());
        }
        Material mat = new Material(contentMan, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.Red);
        mat.setColor("Diffuse", ColorRGBA.Red);
        M_SHIP_FIGHTER = contentMan.loadModel("Models/fighter.obj");
        M_SHIP_FIGHTER.setMaterial(mat.clone());
        M_SHIP_FIGHTER.scale(0.25f);
        
        M_SHIP_CAPITAL_SHIP = contentMan.loadModel("Models/Cube.001.mesh.xml");
        M_SHIP_CAPITAL_SHIP.setMaterial(new Material(contentMan, "Common/MatDefs/Misc/Unshaded.j3md"));
        M_SHIP_CAPITAL_SHIP.scale(0.5f);
        
        M_DEFAULT_WHITE_MATERIAL = new Material(contentMan, "Common/MatDefs/Misc/Unshaded.j3md");
        M_DEFAULT_WHITE_MATERIAL.setColor("Color", ColorRGBA.White);
    }
    
    public static Spatial getShipFighterModel()
    {
        return M_SHIP_FIGHTER.clone();
    }
    public static Spatial getShipFrigateModel()
    {
        return M_SHIP_FRIGATE.clone();
    }
    public static Spatial getShipCruiserModel()
    {
        return M_SHIP_CRUISER.clone();
    }
    public static Spatial getShipCaptialShipModel()
    {
        return M_SHIP_CAPITAL_SHIP.clone();
    }
    
    
    public static Material getDefaultWhiteMaterial()
    {
        return M_DEFAULT_WHITE_MATERIAL.clone();
    }
}