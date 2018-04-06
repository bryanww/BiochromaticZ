package com.github.bryanww.BiochromaticZ.managers;

import com.github.ocraft.s2client.protocol.unit.Unit;

import java.util.HashSet;
import java.util.Set;

public class DroneManager {

    private Set<Unit> drones;

    public DroneManager() {
        drones = new HashSet<>();
    }

    public void addDrone(Unit drone) {
        System.out.println("[BOT] added drone: " + drone.toString());
        drones.add(drone);
    }
}
