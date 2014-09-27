package com.jme3.ai.agents.behaviours.npc;

import com.jme3.ai.agents.Agent;
import com.jme3.ai.agents.behaviours.Behaviour;
import com.jme3.ai.agents.events.GameObjectSeenEvent;
import com.jme3.ai.agents.events.GameObjectSeenListener;
import com.jme3.ai.agents.util.GameObject;
import com.jme3.ai.agents.util.weapons.AbstractFirearmWeapon;
import com.jme3.ai.agents.util.weapons.WeaponExceptions;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * Simple attack behaviour for NPC. This behaviour is for to agent to attack
 * targetedObject or fixed point in space. It will attack with weapon, and it
 * doesn't check if it has ammo.
 *
 * @see AbstractFirearmWeapon#hasBullets()
 * @see AbstractFirearmWeapon#isInRange(com.jme3.math.Vector3f)
 * @see AbstractFirearmWeapon#isInRange(com.jme3.ai.agents.util.GameObject)
 *
 * @author Tihomir Radosavljević
 * @version 1.0.2
 */
public class SimpleAttackBehaviour extends Behaviour implements GameObjectSeenListener {

    /**
     * Target for attack behaviour.
     */
    protected GameObject targetedObject;
    /**
     * Target for attack behaviour.
     */
    protected Vector3f targetPosition;

    /**
     * @param agent to whom behaviour belongs
     */
    public SimpleAttackBehaviour(Agent agent) {
        super(agent);
    }

    /**
     * @param agent to whom behaviour belongs
     * @param spatial active spatial durring this behaviour
     */
    public SimpleAttackBehaviour(Agent agent, Spatial spatial) {
        super(agent, spatial);
    }

    /**
     * @param target at which agent will attack
     */
    public void setTarget(GameObject target) {
        this.targetedObject = target;
    }

    /**
     *
     * @param target at what point will agent attack
     */
    public void setTarget(Vector3f target) {
        this.targetPosition = target;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (agent.getInventory() != null) {
            try {
                if (targetPosition != null) {
                    agent.getInventory().getActiveWeapon().attack(targetPosition, tpf);
                    targetPosition = null;
                } else if (targetedObject != null && targetedObject.isEnabled()) {
                    agent.getInventory().getActiveWeapon().attack(targetedObject, tpf);
                }
            } catch (NullPointerException npe) {
                throw new WeaponExceptions.WeaponNotFound("Agent "+ agent.getName() + " doesn't have active weapon");
            }
        }
    }

    /**
     * Behaviour can automaticaly update its targetedObject with
     * GameObjectSeenEvent, if it is agent, then it check if it is in range and
     * check if they are in same team.
     *
     * @param event
     */
    public void handleGameObjectSeenEvent(GameObjectSeenEvent event) {
        if (event.getGameObjectSeen() instanceof Agent) {
            Agent targetAgent = (Agent) event.getGameObjectSeen();
            if (agent.isSameTeam(targetAgent)) {
                return;
            }
            if (!agent.getInventory().getActiveWeapon().isInRange(targetAgent)) {
                return;
            }
        }
        targetedObject = event.getGameObjectSeen();
        enabled = true;
    }

    /**
     * Method for checking is there any target that agent should attack.
     *
     * @return true if target is set
     */
    public boolean isTargetSet() {
        return targetPosition != null && targetedObject != null;
    }
}
