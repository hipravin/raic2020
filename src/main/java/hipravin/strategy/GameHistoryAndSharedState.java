package hipravin.strategy;

import hipravin.model.ParsedGameState;
import hipravin.strategy.command.BuildingBuildCommand;
import model.Action;
import model.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GameHistoryAndSharedState {
    List<BuildingBuildCommand> ongoingBuildCommands = new ArrayList<>();
    ParsedGameState previousParsedGameState = null;
    Map<Integer, Player[]> playerInfo = new HashMap<>();

    Action previousTickAction;

    public Action getPreviousTickAction() {
        return previousTickAction;
    }

    public void setPreviousTickAction(Action previousTickAction) {
        this.previousTickAction = previousTickAction;
    }

    public void addBuildCommand(BuildingBuildCommand bc) {
        ongoingBuildCommands.add(bc);
    }

    public List<BuildingBuildCommand> getOngoingBuildCommands() {
        return ongoingBuildCommands;
    }

    public Set<Integer> buildOrRepairAssignedWorkersAtCurrentTick() {
        return ongoingBuildCommands.stream()
                .flatMap(BuildingBuildCommand::buidersAndRepairers)
                .collect(Collectors.toSet());
    }

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
}
