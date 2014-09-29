//Copyright (c) 2014, Jesús Martín Berlanga. All rights reserved.
//Distributed under the BSD licence. Read "com/jme3/ai/license.txt".
package com.jme3.ai.agents.behaviours.npc.steering;

import com.jme3.ai.agents.Agent;
import com.jme3.ai.agents.AgentExceptions;
import com.jme3.ai.agents.Team;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import java.util.List;

/**
 * Craig W. Reynolds: "Alignment steering behavior gives an character the
 * ability to align itself with (that is, head in the same direction as) other
 * nearby characters. Steering for alignment can be computed by finding all
 * characters in the local neighborhood, averaging the unit forward vector of
 * the nearby characters. This average is the “desired velocity,” and so the
 * steering vector is the difference between the average and our character’s
 * forward vector. This steering will tend to turn our character so it is
 * aligned with its neighbors."
 *
 * @author Jesús Martín Berlanga
 * @version 1.2.1
 */
public class AlignmentBehaviour extends AbstractStrengthSteeringBehaviour {

    /**
     * List of agents that should align.
     */
    private List<Agent> neighbours;
    /**
     * Maximum distance between tow agents.
     */
    private float maxDistance = Float.POSITIVE_INFINITY;
    private float maxAngle = FastMath.PI / 2;

    /**
     * maxAngle is setted to PI / 2 by default and maxDistance to infinite.
     * Neighbours of agent will be his team members.
     *
     * @param agent
     */
    public AlignmentBehaviour(Agent agent) {
        super(agent);
        try {
            neighbours = agent.getTeam().getMembers();
        } catch (NullPointerException npe) {
            throw new AgentExceptions.TeamNotFoundException(agent);
        }
    }

    /**
     * Neighbours of agent will be his team members.
     *
     * @param maxDistance In order to consider a neihbour inside the
     * neighbourhood
     * @param maxAngle In order to consider a neihbour inside the neighbourhood
     * @param agent
     */
    public AlignmentBehaviour(Agent agent, float maxDistance, float maxAngle) {
        super(agent);
        try {
            this.validateMaxDistance(maxDistance);
            this.maxDistance = maxDistance;
            this.maxAngle = maxAngle;
            neighbours = agent.getTeam().getMembers();
        } catch (NullPointerException npe) {
            throw new AgentExceptions.TeamNotFoundException(agent);
        }
    }

    /**
     * Neighbours of agent will be his team members.
     *
     * @param maxDistance In order to consider a neihbour inside the
     * neighbourhood
     * @param maxAngle In order to consider a neihbour inside the neighbourhood
     * Neighbours of agent will be his team members.
     * @param spatial active spatial during excecution of behaviour
     * @param agent
     */
    public AlignmentBehaviour(Agent agent, float maxDistance, float maxAngle, Spatial spatial) {
        super(agent, spatial);
        try {
            this.validateMaxDistance(maxDistance);
            this.maxDistance = maxDistance;
            this.maxAngle = maxAngle;
            neighbours = agent.getTeam().getMembers();
        } catch (NullPointerException npe) {
            throw new AgentExceptions.TeamNotFoundException(agent);
        }
    }

    /**
     * maxAngle is setted to PI / 2 by default and maxDistance to infinite.
     *
     * @param agent To whom behaviour belongs.
     * @param neighbours Neighbours, this agent is moving toward the center of
     * this neighbours.
     */
    public AlignmentBehaviour(Agent agent, List<Agent> neighbours) {
        super(agent);
        this.neighbours = neighbours;
    }

    /**
     * @param maxDistance In order to consider a neihbour inside the
     * neighbourhood
     * @param maxAngle In order to consider a neihbour inside the neighbourhood
     *
     * @throws NegativeMaxDistanceException If maxDistance is lower than 0
     *
     * @see Agent#inBoidNeighborhoodMaxAngle(com.jme3.ai.agents.Agent, float,
     * float, float)
     * @see AlignmentBehaviour#AlignmentBehaviour(com.jme3.ai.agents.Agent,
     * java.util.List)
     */
    public AlignmentBehaviour(Agent agent, List<Agent> neighbours, float maxDistance, float maxAngle) {
        super(agent);
        this.validateMaxDistance(maxDistance);
        this.neighbours = neighbours;
        this.maxDistance = maxDistance;
        this.maxAngle = maxAngle;
    }

    /**
     * @param spatial active spatial during excecution of behaviour
     * @see AlignmentBehaviour#AlignmentBehaviour(com.jme3.ai.agents.Agent,
     * java.util.List)
     */
    public AlignmentBehaviour(Agent agent, List<Agent> neighbours, Spatial spatial) {
        super(agent, spatial);
        this.neighbours = neighbours;
    }

    /**
     * @see AlignmentBehaviour#AlignmentBehaviour(com.jme3.ai.agents.Agent,
     * java.util.List)
     * @see AlignmentBehaviour#AlignmentBehaviour(com.jme3.ai.agents.Agent,
     * java.util.List, float, float)
     */
    public AlignmentBehaviour(Agent agent, List<Agent> neighbours, float maxDistance, float maxAngle, Spatial spatial) {
        super(agent, spatial);
        this.validateMaxDistance(maxDistance);
        this.neighbours = neighbours;
        this.maxDistance = maxDistance;
        this.maxAngle = maxAngle;
    }

    private void validateMaxDistance(float maxDistance) {
        if (maxDistance < 0) {
            throw new SteeringExceptions.NegativeValueException("The max distance value can not be negative.", maxDistance);
        }
    }

    /**
     * @see AbstractSteeringBehaviour#calculateSteering()
     */
    @Override
    protected Vector3f calculateRawSteering() {
        // steering accumulator and count of neighbors, both initially zero
        Vector3f steering = new Vector3f();
        int realNeighbors = 0;
        // for each of the other vehicles...
        for (Agent agentO : neighbours) {
            if (this.agent.inBoidNeighborhood(agentO, this.agent.getRadius() * 3, this.maxDistance, this.maxAngle)) {
                // accumulate sum of neighbor's positions
                steering = steering.add(agentO.fordwardVector());
                realNeighbors++;
            }
        }
        // divide by neighbors, subtract off current position to get error-correcting direction
        if (realNeighbors > 0) {
            steering = steering.divide(realNeighbors);
            steering = this.agent.offset(steering);
        }
        return steering;
    }

    public void setNeighbours(List<Agent> neighbours) {
        this.neighbours = neighbours;
    }

    public void setNeighboursFromTeam(Team team) {
        this.neighbours = team.getMembers();
    }
}