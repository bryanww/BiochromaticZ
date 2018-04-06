package com.github.bryanww.BiochromaticZ;

import com.github.bryanww.BiochromaticZ.managers.DroneManager;
import com.github.bryanww.BiochromaticZ.managers.LarvaManager;
import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.api.controller.S2Controller;
import com.github.ocraft.s2client.protocol.action.raw.ActionRawUnitCommand;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.BattlenetMap;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.GameStatus;
import com.github.ocraft.s2client.protocol.game.InterfaceOptions;
import com.github.ocraft.s2client.protocol.response.ResponseCreateGame;
import com.github.ocraft.s2client.protocol.response.ResponseJoinGame;
import com.github.ocraft.s2client.protocol.response.ResponseObservation;
import com.github.ocraft.s2client.protocol.response.ResponseStep;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.github.ocraft.s2client.api.S2Client.starcraft2Client;
import static com.github.ocraft.s2client.api.controller.S2Controller.starcraft2Game;
import static com.github.ocraft.s2client.protocol.action.Action.action;
import static com.github.ocraft.s2client.protocol.data.Abilities.TRAIN_DRONE;
import static com.github.ocraft.s2client.protocol.data.Abilities.TRAIN_OVERLORD;
import static com.github.ocraft.s2client.protocol.game.ComputerPlayerSetup.computer;
import static com.github.ocraft.s2client.protocol.game.PlayerSetup.participant;
import static com.github.ocraft.s2client.protocol.game.Race.PROTOSS;
import static com.github.ocraft.s2client.protocol.game.Race.ZERG;
import static com.github.ocraft.s2client.protocol.request.Requests.*;

public class Main {

    public static void main(String[] args) {
        S2Controller game = starcraft2Game().launch();
        S2Client client = starcraft2Client().connectTo(game).traced(true).start();

        LarvaManager larvaManager = new LarvaManager();
        DroneManager droneManager = new DroneManager();
        Map<Tag, Unit> knownUnits = new HashMap<>();

        client.request(createGame()
                .onBattlenetMap(BattlenetMap.of("Lava Flow"))
                .withPlayerSetup(participant(), computer(PROTOSS, Difficulty.MEDIUM)));

        client.responseStream()
                .takeWhile(response -> !game.inState(GameStatus.ENDED))
                .subscribe(response -> {

                    response.as(ResponseCreateGame.class).ifPresent(r -> client.request(joinGame().as(ZERG).use(InterfaceOptions.interfaces().raw().build())));
                    response.as(ResponseJoinGame.class).ifPresent(r  -> client.request(observation()));
                    response.as(ResponseStep.class).ifPresent(r -> client.request(observation()));

                    response.as(ResponseObservation.class).ifPresent(r  -> {
                        // HERE GOES BOT LOGIC
                        r.getObservation().getRaw().ifPresent(w -> {
                            for (Unit u :w.getUnits()) {
                                if (!knownUnits.containsKey(u.getTag())) {
                                    if (u.getType() == Units.ZERG_LARVA) {
                                        larvaManager.addLarva(u);
                                    } else if (u.getType() == Units.ZERG_DRONE) {
                                        droneManager.addDrone(u);
                                    }
                                    // enemies
                                    // minerals
                                    // army units
                                    knownUnits.put(u.getTag(), u);
                                }
                            }
                        });

                        larvaManager.trainNextUnit(r, client);

                        client.request(nextStep());
                    });

                });

        client.await();
    }
}