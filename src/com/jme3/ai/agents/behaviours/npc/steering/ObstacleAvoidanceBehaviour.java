//Copyright (c) 2014, Jesús Martín Berlanga.
//All rights reserved. Distributed under the BSD licence. Read "com/jme3/ai/license.txt".
package com.jme3.ai.agents.behaviours.npc.steering;

import com.jme3.ai.agents.Agent;
import com.jme3.ai.agents.behaviours.BehaviourExceptions.IllegalBehaviourException;
import com.jme3.ai.agents.behaviours.npc.steering.SteeringExceptions.ObstacleAvoindanceWithNegativeMinDistanceException;
import com.jme3.ai.agents.util.GameEntity;

import com.jme3.math.Vector3f;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;

import java.util.List;
import java.util.Random;

/**
 * Returns a steering force to avoid a given obstacle. The purely lateral
 * steering force will turn our vehicle towards a silhouette edge of the
 * obstacle. Avoidance is required when (1) the obstacle intersects the
 * vehicle's current path, (2) it is in front of the vehicle, and (3) is within
 * minTimeToCollision seconds of travel at the vehicle's current velocity.
 * Returns a zero vector value when no avoidance is required. <br> <br>
 *
 * The implementation of obstacle avoidance behavior here will make a
 * simplifying assumption that both the character and obstacle can be reasonably
 * approximated as spheres. <br> <br>
 *
 * Keep in mind that this relates to obstacle avoidance not necessarily to
 * collision detection. <br> <br>
 *
 * The goal of the behavior is to keep an imaginary cylinder of free space in
 * front of the character. The cylinder lies along the character's forward axis,
 * has a diameter equal to the character's bounding sphere, and extends from the
 * character's center for a distance based on the character's velocity. <br>
 * <br>
 *
 * It is needed that the obstacles (Agents) have the "radius" atribute correctly
 * setted up.
 *
 * @see GameEntity#setRadius(float)
 *
 * @author Jesús Martín Berlanga
 * @version 1.1.1
 */
public class ObstacleAvoidanceBehaviour extends AbstractStrengthSteeringBehaviour {

    private float minDistance;
    private float minTimeToCollision;
    private List<GameEntity> obstacles;

    /**
     * @param obstacles A list with the obstacles (Agents)
     * @param minTimeToCollision When the time to collision is lower than this
     * value the steer force will appear. Time is measured in seconds.
     *
     * @throws ObstacleAvoindanceWithNoMinTimeToCollisionException If
     * minTimeToCollision is lower or equals to 0
     *
     * @see
     * AbstractSteeringBehaviour#AbstractSteeringBehaviour(com.jme3.ai.agents.Agent)
     */
    public ObstacleAvoidanceBehaviour(Agent agent, List<GameEntity> obstacles, float minTimeToCollision) {
        super(agent);
        this.validateMinTimeToCollision(minTimeToCollision);
        this.minTimeToCollision = minTimeToCollision;
        this.obstacles = obstacles;
        this.minDistance = Float.POSITIVE_INFINITY;
    }

    /**
     * @see
     * ObstacleAvoidanceBehaviour#ObstacleAvoidanceBehaviour(com.jme3.ai.agents.Agent,
     * java.util.List, float)
     * @see
     * AbstractSteeringBehaviour#AbstractSteeringBehaviour(com.jme3.ai.agents.Agent,
     * com.jme3.scene.Spatial)
     */
    public ObstacleAvoidanceBehaviour(Agent agent, List<GameEntity> obstacles, float minTimeToCollision, Spatial spatial) {
        super(agent, spatial);
        this.validateMinTimeToCollision(minTimeToCollision);
        this.minTimeToCollision = minTimeToCollision;
        this.obstacles = obstacles;
        this.minDistance = Float.POSITIVE_INFINITY;
    }

    /**
     * @param minDistance Min. distance from center to center to consider an
     * obstacle
     *
     * @throws ObstacleAvoindanceWithNegativeMinDistanceException If
     * minTimeToCollision is lower than 0
     *
     * @see
     * ObstacleAvoidanceBehaviour#ObstacleAvoidanceBehaviour(com.jme3.ai.agents.Agent,
     * java.util.List, float)
     */
    public ObstacleAvoidanceBehaviour(Agent agent, List<GameEntity> obstacles, float minTimeToCollision, float minDistance) {
        super(agent);
        this.validateMinTimeToCollision(minTimeToCollision);
        this.validateMinDistance(minDistance);
        this.minTimeToCollision = minTimeToCollision;
        this.obstacles = obstacles;
        this.minDistance = minDistance;
    }

    /**
     * @see
     * ObstacleAvoidanceBehaviour#ObstacleAvoidanceBehaviour(com.jme3.ai.agents.Agent,
     * com.jme3.scene.Spatial, java.util.List, float)
     * @see
     * AbstractSteeringBehaviour#AbstractSteeringBehaviour(com.jme3.ai.agents.Agent,
     * com.jme3.scene.Spatial)
     */
    public ObstacleAvoidanceBehaviour(Agent agent, List<GameEntity> obstacles, float minTimeToCollision, float minDistance, Spatial spatial) {
        super(agent, spatial);
        this.validateMinTimeToCollision(minTimeToCollision);
        this.validateMinDistance(minDistance);
        this.minTimeToCollision = minTimeToCollision;
        this.obstacles = obstacles;
        this.minDistance = minDistance;
    }

    private void validateMinDistance(float minDistance) {
        if (minDistance < 0) {
            throw new ObstacleAvoindanceWithNegativeMinDistanceException("The min distance from an obstacle can not be negative. Current value is " + minDistance);
        }
    }

    private void validateMinTimeToCollision(float minTimeToCollision) {
        if (minTimeToCollision <= 0) {
            throw new ObstacleAvoindanceWithNegativeMinDistanceException("The min time to collision must be postitive. Current value is " + minTimeToCollision);
        }
    }

    /**
     * @see AbstractSteeringBehaviour#calculateSteering()
     */
    @Override
    protected Vector3f calculateRawSteering() {
        Vector3f nearestObstacleSteerForce = new Vector3f();

        if (this.agent.getVelocity() != null) {
            float agentVel = this.agent.getVelocity().length();
            float minDistanceToCollision = agentVel * timePerFrame * this.minTimeToCollision;

            // test all obstacles for intersection with my forward axis,
            // select the one whose intersection is nearest
            for (GameEntity obstacle : this.obstacles) {
                float distanceFromCenterToCenter = this.agent.distanceRelativeToGameObject(obstacle);
                if (distanceFromCenterToCenter > this.minDistance) {
                    break;
                }

                float distanceFromCenterToObstacleSuperf = distanceFromCenterToCenter - obstacle.getRadius();
                float distance = distanceFromCenterToObstacleSuperf - this.agent.getRadius();

                if (distanceFromCenterToObstacleSuperf < 0) {
                    distanceFromCenterToObstacleSuperf = 0;
                }

                if (distance < 0) {
                    distance = 0;
                }

                // if it is at least in the radius of the collision cylinder and we are facing the obstacle
                if (this.agent.forwardness(obstacle) > 0
                        && //Are we facing the obstacle ?
                        distance * distance
                        < ((minDistanceToCollision * minDistanceToCollision)
                        + (this.agent.getRadius() * this.agent.getRadius())) //Pythagoras Theorem
                        ) {
                    Vector3f velocityNormaliced = this.agent.getVelocity().normalize();
                    Vector3f distanceVec = this.agent.offset(obstacle).normalize().mult(distanceFromCenterToObstacleSuperf);
                    Vector3f projectedVector = velocityNormaliced.mult(velocityNormaliced.dot(distanceVec));

                    Vector3f collisionDistanceOffset = projectedVector.subtract(distanceVec);

                    if (collisionDistanceOffset.length() < this.agent.getRadius()) {
                        Vector3f collisionDistanceDirection;

                        if (!collisionDistanceOffset.equals(Vector3f.ZERO)) {
                            collisionDistanceDirection = collisionDistanceOffset.normalize();
                        } else {
                            collisionDistanceDirection = randomVectInPlane(this.agent.getVelocity(), this.agent.getLocalTranslation()).normalize();
                        }

                        Vector3f steerForce = collisionDistanceDirection.mult((this.agent.getRadius() - collisionDistanceOffset.length())
                                / this.agent.getRadius());

                        if (steerForce.length() > nearestObstacleSteerForce.length()) {
                            nearestObstacleSteerForce = steerForce;
                        }
                    }
                }
            }
        }

        return nearestObstacleSteerForce;
    }
    
    /**
     * Generates a random vector inside a plane defined by a normal vector and a point
     */
    protected Vector3f randomVectInPlane(Vector3f planeNormalV, Vector3f planePoint) {
        Random rand = FastMath.rand;

        /* Plane ecuation: Ax + By + Cz + D = 0 
         *  => z = -(Ax + By + D) / C
         *  => x = -(By + Cz + D) / A
         *  => y = -(Ax + Cz + D) / B
         */
        float a = planeNormalV.x;
        float b = planeNormalV.y;
        float c = planeNormalV.z;
        float d = -((a * planePoint.x)
                + (b * planePoint.y)
                + (c * planePoint.z));

        float x, y, z;

        if (c != 0) {
            x = rand.nextFloat();
            y = rand.nextFloat();
            z = -((a * x) + (b * y) + d) / c;
        } else if (a != 0) {
            y = rand.nextFloat();
            z = rand.nextFloat();
            x = -((b * y) + (c * z) + d) / a;
        } else if (b != 0) {
            x = rand.nextFloat();
            z = rand.nextFloat();
            y = -((a * x) + (c * z) + d) / b;
        } else {
            x = rand.nextFloat();
            y = rand.nextFloat();
            z = rand.nextFloat();
        }

        Vector3f randPoint = new Vector3f(x, y, z);

        return randPoint.subtract(planePoint);
    }

    protected List<GameEntity> getObstacles() {
        return this.obstacles;
    }

    public void setObstacles(List<GameEntity> obstacles) {
        this.obstacles = obstacles;
    }

    protected float getMinTimeToCollision() {
        return this.minTimeToCollision;
    }

    protected float getMinDistance() {
        return this.minDistance;
    }
}