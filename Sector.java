/**
 * Created by chthonic7 on 10/8/14.
 */
public class Sector extends Territory{
    //To be honest, wasn't sure if this was necessary, but maybe this is akin to galaxy, so whatevs
    public Sector(double locationX, double locationY, Cluster[] clusters, String name, String owner){
        super(locationX,locationY,null,clusters,name,owner);
    }
}