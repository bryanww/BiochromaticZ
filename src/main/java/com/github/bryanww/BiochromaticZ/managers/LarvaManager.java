package com.github.bryanww.BiochromaticZ.managers;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.protocol.action.raw.ActionRawUnitCommand;
import com.github.ocraft.s2client.protocol.response.ResponseObservation;
import com.github.ocraft.s2client.protocol.unit.Unit;

import java.util.HashSet;
import java.util.Set;

import static com.github.ocraft.s2client.protocol.action.Action.action;
import static com.github.ocraft.s2client.protocol.data.Abilities.*;
import static com.github.ocraft.s2client.protocol.request.Requests.actions;

public class LarvaManager {

    private Set<Unit> larvae;
    private ResponseObservation observation;
    private S2Client client;
    private int mineralsSpentThisStep;

    public LarvaManager() {
        larvae = new HashSet<>();
        mineralsSpentThisStep = 0;
    }

    public void addLarva(Unit larva) {
        System.out.println("[BOT] added larva: " + larva);
        larvae.add(larva);
    }

    public void trainNextUnit(ResponseObservation observation, S2Client client) {
        mineralsSpentThisStep = 0;
        this.observation = observation;
        this.client = client;
        trainWorker();
        trainOverlord();
        trainArmy();
    }

    private void trainWorker() {
        if (shouldTrainWorker()) {
            // TODO - choose larva at the correct base
            Unit larva = larvae.iterator().next();
            System.out.println("[BOT] training drone");
            client.request(actions().of(
                    action().raw(ActionRawUnitCommand.unitCommand().forUnits(larva.getTag()).useAbility(TRAIN_DRONE))
            ));
            // TODO - get this magic number from api
            mineralsSpentThisStep += 50;
        }
    }

    private boolean shouldTrainWorker() {
        return observation.getObservation().getPlayerCommon().getMinerals() - mineralsSpentThisStep >= 50
                && observation.getObservation().getPlayerCommon().getFoodUsed() < (observation.getObservation().getPlayerCommon().getFoodCap() - 1)
                && !larvae.isEmpty();
    }

    private void trainOverlord() {
        if (shouldTrainOverlord()) {
            // TODO - choose larva at the correct base
            Unit larva = larvae.iterator().next();
            System.out.println("[BOT] training overlord");
            client.request(actions().of(
                    action().raw(ActionRawUnitCommand.unitCommand().forUnits(larva.getTag()).useAbility(TRAIN_OVERLORD))
            ));
            // TODO - get this magic number from api
            mineralsSpentThisStep += 100;
        }
    }

    private boolean shouldTrainOverlord() {
        return observation.getObservation().getPlayerCommon().getMinerals() - mineralsSpentThisStep >= 100
                && observation.getObservation().getPlayerCommon().getFoodUsed() >= (observation.getObservation().getPlayerCommon().getFoodCap() * 0.8)
                && !larvae.isEmpty();
    }

    private void trainArmy() {
        if (shouldTrainArmy()) {
            // TODO - choose larva at the correct base
            Unit larva = larvae.iterator().next();
            System.out.println("[BOT] training army");
            client.request(actions().of(
                    action().raw(ActionRawUnitCommand.unitCommand().forUnits(larva.getTag()).useAbility(TRAIN_ZERGLING))
            ));
            // TODO - get this magic number from api
            mineralsSpentThisStep += 100;
        }
    }

    private boolean shouldTrainArmy() {
        return false;
    }
}
