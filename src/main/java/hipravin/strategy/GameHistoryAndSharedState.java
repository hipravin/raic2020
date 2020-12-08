package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.ParsedGameState;
import hipravin.strategy.command.BuildThenRepairCommand;
import hipravin.strategy.command.Command;
import model.Action;
import model.EntityType;
import model.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameHistoryAndSharedState {
    public static Random random = new Random(0);

    //    List<BuildingBuildCommand> ongoingBuildCommands = new ArrayList<>();
    List<Command> ongoingCommands = new ArrayList<>();

    ParsedGameState previousParsedGameState = null;
    Map<Integer, Player[]> playerInfo = new HashMap<>();


    Action previousTickAction;

    public Action getPreviousTickAction() {
        return previousTickAction;
    }

    public void setPreviousTickAction(Action previousTickAction) {
        this.previousTickAction = previousTickAction;
    }

//    public void addBuildCommand(BuildingBuildCommand bc) {
//        ongoingBuildCommands.add(bc);
//    }

//    public List<BuildingBuildCommand> getOngoingBuildCommands() {
//        return ongoingBuildCommands;
//    }
//
//    public Set<Integer> buildOrRepairAssignedWorkersAtCurrentTick() {
//        return ongoingBuildCommands.stream()
//                .flatMap(BuildingBuildCommand::buidersAndRepairers)
//                .collect(Collectors.toSet());
//    }

    public ParsedGameState getPreviousParsedGameState() {
        return previousParsedGameState;
    }

    public void setPreviousParsedGameState(ParsedGameState previousParsedGameState) {
        this.previousParsedGameState = previousParsedGameState;
    }

    public Map<Integer, Player[]> getPlayerInfo() {
        return playerInfo;
    }

    public void setPlayerInfo(Map<Integer, Player[]> playerInfo) {
        this.playerInfo = playerInfo;
    }

    public boolean addOngoingCommand(Command command, boolean force) {
        DebugOut.println("Adding command:" + command.toString());

        if (force) {
            ongoingCommands.removeIf(oc -> twoSetFirstIntersection(oc.getRelatedEntityIds(), command.getRelatedEntityIds()).isPresent());
            ongoingCommands.add(command);

            return true;
        } else {
            boolean conflict = ongoingCommands.stream().anyMatch(c -> twoSetFirstIntersection(c.getRelatedEntityIds(), command.getRelatedEntityIds()).isPresent());
            if (conflict) {
                return false;
            } else {
                ongoingCommands.add(command);
                return true;
            }
        }
    }

    static Optional<Integer> twoSetFirstIntersection(Set<Integer> set1, Set<Integer> set2) {
        if (set1.size() < set2.size()) {
            return twoSetFirstIntersection(set2, set1);
        } else {
            for (Integer p : set2) {
                if (set1.contains(p)) {
                    return Optional.of(p);
                }
            }
            return Optional.empty();
        }
    }

    public int ongoingHouseBuildCommandCount() {
        return (int)ongoingCommands.stream()
                .filter(c -> {
                    if(c instanceof BuildThenRepairCommand) {
                        return ((BuildThenRepairCommand) c).getBuildingType() == EntityType.HOUSE;
                    }

                    return false;
                }).count();
    }

    public int ongoingBarrackBuildCommandCount() {
        return (int)ongoingCommands.stream()
                .filter(c -> {
                    if(c instanceof BuildThenRepairCommand) {
                        return ((BuildThenRepairCommand) c).getBuildingType() == EntityType.RANGED_BASE
                                || ((BuildThenRepairCommand) c).getBuildingType() == EntityType.MELEE_BASE ;
                    }

                    return false;
                }).count();
    }

    public Stream<Integer> allOngoingCommandRelatedEntitiIds() {
        return ongoingCommands.stream().flatMap(c -> c.getRelatedEntityIds().stream());
    }

    public Set<Integer> allOngoingCommandRelatedEntitiIdsSet() {
        return allOngoingCommandRelatedEntitiIds().collect(Collectors.toSet());
    }

    public List<Command> getOngoingCommands() {
        return ongoingCommands;
    }
}
