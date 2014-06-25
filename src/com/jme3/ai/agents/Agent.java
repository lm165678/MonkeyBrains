//Copyright (c) 2014, Jesús Martín Berlanga. All rights reserved. Distributed under the BSD licence. Read "com/jme3/ai/license.txt".

package com.jme3.ai.agents;

import com.jme3.ai.agents.behaviours.Behaviour;
import com.jme3.ai.agents.behaviours.npc.SimpleMainBehaviour;
import com.jme3.scene.Spatial;
import com.jme3.ai.agents.util.AbstractWeapon;
import com.jme3.ai.agents.util.GameObject;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;

/**
 * Class that represents Agent. Note: Not recommended for extending. Use
 * generics.
 *
 * @author Jesús Martín Berlanga
 * @author Tihomir Radosavljević
 * @version 1.3
 */
public class Agent<T> extends GameObject {
    
    /**
     * Class that enables you to add all variable you need for your agent.
     */
    private T model;
    /**
     * Unique name of Agent.
     */
    private String name;
    /**
     * Name of team. Primarily used for enabling friendly fire.
     */
    private Team team;
    /**
     * AbstractWeapon used by agent.
     */
    private AbstractWeapon weapon;
    /**
     * Main behaviour of Agent. Behaviour that will be active while his alive.
     */
    private Behaviour mainBehaviour;
    /**
     * Visibility range. How far agent can see.
     */
    private float visibilityRange;
    /**
     * Camera that is attached to agent.
     */
    private Camera camera;
    /**
     * Size of bounding sphere, for steer behaviours
     *
     * @author Jesús Martín Berlanga
     */
    float radius = 0;

    /**
     * @author Jesús Martín Berlanga
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * @author Jesús Martín Berlanga
     */
    public float getRadius() {
        return this.radius;
    }

    /**
     * @param name unique name/id of agent
     */
    public Agent(String name) {
        this.name = name;
    }

    /**
     * @param name unique name/id of agent
     * @param spatial spatial that will agent have durring game
     */
    public Agent(String name, Spatial spatial) {
        this.name = name;
        this.spatial = spatial;
    }

    /**
     * @return weapon that agent is currently using
     */
    public AbstractWeapon getWeapon() {
        return weapon;
    }

    /**
     * It will add weapon to agent and add its spatial to agent, if there
     * already was weapon before with its own spatial, it will remove it before
     * adding new weapon spatial.
     *
     * @param weapon that agent will use
     */
    public void setWeapon(AbstractWeapon weapon) {
        //remove previous weapon spatial
        if (this.weapon != null && this.weapon.getSpatial() != null) {
            this.weapon.getSpatial().removeFromParent();
        }
        //add new weapon spatial if there is any
        if (weapon.getSpatial() != null) {
            ((Node) spatial).attachChild(weapon.getSpatial());
        }
        this.weapon = weapon;
    }

    /**
     * @return main behaviour of agent
     */
    public Behaviour getMainBehaviour() {
        return mainBehaviour;
    }

    /**
     * Setting main behaviour to agent. For more how should main behaviour look
     * like:
     *
     * @see SimpleMainBehaviour
     * @param mainBehaviour
     */
    public void setMainBehaviour(Behaviour mainBehaviour) {
        this.mainBehaviour = mainBehaviour;
        this.mainBehaviour.setEnabled(false);
    }

    /**
     * @return unique name/id of agent
     */
    public String getName() {
        return name;
    }

    /**
     * Method for starting agent. Note: Agent must be alive to be started.
     *
     * @see Agent#enabled
     */
    public void start() {
        enabled = true;
        mainBehaviour.setEnabled(true);
    }

    /**
     * @return visibility range of agent
     */
    public float getVisibilityRange() {
        return visibilityRange;
    }

    /**
     * @param visibilityRange how far agent can see
     */
    public void setVisibilityRange(float visibilityRange) {
        this.visibilityRange = visibilityRange;
    }

    /**
     * @return model of agent
     */
    public T getModel() {
        return model;
    }

    /**
     * @param model of agent
     */
    public void setModel(T model) {
        this.model = model;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.model != null ? this.model.hashCode() : 0);
        hash = 47 * hash + (this.team != null ? this.team.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Agent<T> other = (Agent<T>) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.team != other.team && (this.team == null || !this.team.equals(other.team))) {
            return false;
        }
        return true;
    }

    /**
     * @author Tihomir Radosavljević
     * @author Jesús Martín Berlanga
     */
    @Override
    protected void controlUpdate(float tpf) {
               
        if (mainBehaviour != null) {
            mainBehaviour.update(tpf);
        }
        //for updating cooldown on weapon
        if (weapon != null) {
            weapon.update(tpf);
        }
    }

    /**
     * Gets the predicted position for this 'frame', taking into account current
     * position and velocity.
     *
     * @author Jesús Martín Berlanga
     */
    public Vector3f getPredictedPosition()
    {
        Vector3f acc = this.getAcceleration();
        
        if (acc == null) 
            acc = new Vector3f();
        
        return this.getLocalTranslation().add(acc);
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        throw new UnsupportedOperationException("You should override it youself");
    }

    /**
     * @return team in which agent belongs
     */
    public Team getTeam() {
        return team;
    }

    /**
     * @param team in which agent belongs
     */
    public void setTeam(Team team) {
        this.team = team;
    }

    /**
     * Check if this agent is in same team as another agent.
     *
     * @param agent
     * @return true if they are in same team, false otherwise
     */
    public boolean isSameTeam(Agent agent) {
        if (team == null || agent.getTeam() == null) {
            return false;
        }
        return team.equals(agent.getTeam());
    }

    /**
     * @return camera that is attached to agent
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * Setting camera for agent. It is recommended for use mouse input.
     *
     * @param camera
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * Check if this agent is considered in the same "neighborhood" in relation
     * with another agent. <br> <br>
     *
     * If the distance is lower than minDistance It is definitely considered in
     * the same neighborhood. <br> <br>
     *
     * If the distance is higher than maxDistance It is defenitely not
     * considered in the same neighborhood. <br> <br>
     *
     * If the distance is inside [minDistance. maxDistance] It is considered in
     * teh same neighborhood if the forwardness is higher than the 1 -
     * sinMaxAngle.
     *
     * @param Agent The other agent
     * @param minDistance Min. distance to be in the same "neighborhood"
     * @param maxDistance Max. distance to be in the same "neighborhood"
     * @param MaxAngle Max angle in radians
     *
     * @author Jesús Martín Berlanga
     */
    public boolean inBoidNeighborhood(
            Agent neighbour,
            float minDistance,
            float maxDistance,
            float MaxAngle) {
        boolean isInBoidNeighborhood;
        
        if (this == neighbour) {
            isInBoidNeighborhood = false;
        } else {
            float distanceSquared = this.distanceSquaredRelativeToAgent(neighbour);

            // definitely in neighborhood if inside minDistance sphere
            if (distanceSquared < (minDistance * minDistance)) {
                isInBoidNeighborhood = true;
            } // definitely not in neighborhood if outside maxDistance sphere
            else if (distanceSquared > maxDistance * maxDistance) {
                isInBoidNeighborhood = false;
            } // otherwise, test angular offset from forward axis.
            else {
                
                if(this.getAcceleration() != null)
                {
                    Vector3f unitOffset = this.offset(neighbour).divide(distanceSquared);
                    float forwardness = this.forwardness(unitOffset);
                    isInBoidNeighborhood = forwardness > FastMath.cos(MaxAngle);
                //    System.out.println("Offset:" + unitOffset + ";Fordwardness = " + forwardness + "; cosMaxAngle =" + FastMath.cos(MaxAngle) + "; " + isInBoidNeighborhood);//debug
                }
                else
                {
                    isInBoidNeighborhood = false;
                }
            }
        }
        
        return isInBoidNeighborhood;
    }

    /**
     * Calculates the forwardness in relation with another agent. That is how
     * "forward" is the direction to the quarry (1 means dead ahead, 0 is
     * directly to the side, -1 is straight back)
     *
     * @param agent Other agent
     * @return The forwardness in relation with another agent
     *
     * @author Jesús Martín Berlanga
     */
    public float forwardness(Agent agent) 
    {
        Vector3f agentLooks = this.fordwardVector();
        float radiansAngleBetwen = agentLooks.angleBetween(this.offset(agent).normalize()); 
        return (float) FastMath.cos(radiansAngleBetwen); 
    }
    
    public Vector3f fordwardVector() {
        return this.getLocalRotation().mult(new Vector3f(0,0,1)).normalize();
    }

    /**
     * Calculates the forwardness in relation with a position vector
     *
     * @param positionVector Offset vector.
     * @see Agent#forwardness(com.jme3.ai.agents.Agent)
     *
     * @author Jesús Martín Berlanga
     */
    public float forwardness(Vector3f offsetVector) {
        Vector3f agentLooks = this.getLocalRotation().mult(new Vector3f(0,0,1)).normalize();
        float radiansAngleBetwen = agentLooks.angleBetween(offsetVector.normalize());
        return FastMath.cos(radiansAngleBetwen); 
    }

    /**
     * @param agent Other agent
     * @return Distance relative to another Agent
     *
     * @author Jesús Martín Berlanga
     */
    public float distanceRelativeToAgent(Agent agent) {
        return this.offset(agent).length();
    }

    /**
     * @param agent Other agent
     * @return Distance squared relative to another Agent
     *
     * @author Jesús Martín Berlanga
     */
    public float distanceSquaredRelativeToAgent(Agent agent) {
        return this.offset(agent).lengthSquared();
    }

    /**
     * @param agent Other agent
     * @return The offset relative to another Agent
     *
     * @author Jesús Martín Berlanga
     */
    public Vector3f offset(Agent agent) {
        return agent.getLocalTranslation().subtract(this.getLocalTranslation());
    }
    
    /**
     * @param agent Other agent
     * @return The offset relative to an position vector
     *
     * @author Jesús Martín Berlanga
     */
    public Vector3f offset(Vector3f positionVector) {
        return positionVector.subtract(this.getLocalTranslation());
    }
}