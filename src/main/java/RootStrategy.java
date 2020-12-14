import hipravin.DebugOut;
import hipravin.model.GameStateParser;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.*;
import hipravin.strategy.command.Command;
import model.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class RootStrategy extends MyStrategy {

    GameHistoryAndSharedState gameHistoryState = new GameHistoryAndSharedState();
    ParsedGameState currentParsedGameState;
    StrategyParams strategyParams;

    List<SubStrategy> subStrategies = new ArrayList<>();

    public RootStrategy() {
        strategyParams = new StrategyParams();
        if(System.getProperty("OPTION2") != null) {
            strategyParams.activateOption2();
        }


        FinalGameStartStrategy buildFirstHouseFinalStrategy = new FinalGameStartStrategy();
        PushMinersStrategy pushMinersStrategy = new PushMinersStrategy();
        SpawnWorkersStrategy spawnWorkersStrategy = new SpawnWorkersStrategy();
        SpawnRangersStrategy spawnRangersStrategy = new SpawnRangersStrategy();
        WorkerDefendingTurretsStrategy workerDefendingTurretsStrategy = new WorkerDefendingTurretsStrategy();
        BuildHousesStrategy buildHousesStrategy = new BuildHousesStrategy();
        MagnetRepairStrategy magnetRepairStrategy = new MagnetRepairStrategy();
        BuildBarrackStrategy buildBarrackStrategy = new BuildBarrackStrategy();

        Round1Strategy round1Strategy = new Round1Strategy();

        TurretSetAutoAttackStrategy turretsAutoAttack = new TurretSetAutoAttackStrategy();
        UnsetBuildCommandsStrategy unsetStrategy = new UnsetBuildCommandsStrategy();
        BuildTurretStrategy buildTurretStrategy = new BuildTurretStrategy();

        WayOutWorkersBlockingDetectingStrategy wayOutWorkersBlockingDetectingStrategy = new WayOutWorkersBlockingDetectingStrategy();
        WorkerScoutStrategy workerScoutStrategy = new WorkerScoutStrategy();
        AutomineFallbackStrategy autoMine = new AutomineFallbackStrategy();
        RangerAutoattackFallbackStrategy rangerAutoAttack = new RangerAutoattackFallbackStrategy();

        subStrategies.add(buildFirstHouseFinalStrategy);
        subStrategies.add(pushMinersStrategy);
        subStrategies.add(spawnWorkersStrategy);
        subStrategies.add(spawnRangersStrategy);
        subStrategies.add(magnetRepairStrategy);
        subStrategies.add(buildHousesStrategy);
        subStrategies.add(workerDefendingTurretsStrategy);

        subStrategies.add(round1Strategy);

        subStrategies.add(buildBarrackStrategy);
        subStrategies.add(buildTurretStrategy);
        subStrategies.add(wayOutWorkersBlockingDetectingStrategy);

        subStrategies.add(workerScoutStrategy);

        subStrategies.add(autoMine);
        subStrategies.add(rangerAutoAttack);
        subStrategies.add(unsetStrategy);
        subStrategies.add(turretsAutoAttack);
    }

    void initStaticParams(PlayerView playerView) {
        if (playerView.getCurrentTick() == 0) {
            Position2dUtil.MAP_SIZE = playerView.getMapSize();
            Optional<Entity> myCC = Arrays.stream(playerView.getEntities())
                    .filter(e -> e.getEntityType() == EntityType.BUILDER_BASE && e.getPlayerId().equals(playerView.getMyId()))
                    .findAny();

            myCC.ifPresent(entity -> {
                Position2dUtil.MY_CC = Position2d.of(entity.getPosition());
            });

            setMyCorner(playerView);
            setBuildingSizes(playerView);

            if(!playerView.isFogOfWar() && playerView.getPlayers().length > 2) {
                strategyParams.activateRound1();
            }
            if(playerView.isFogOfWar() && playerView.getPlayers().length > 2) {
                strategyParams.activateRound2();
            }
        }
    }

    void setBuildingSizes(PlayerView playerView) {
        Map<EntityType, EntityProperties> properties = playerView.getEntityProperties();

        Position2dUtil.CC_SIZE = properties.get(EntityType.BUILDER_BASE).getSize();
        Position2dUtil.WALL_SIZE = properties.get(EntityType.WALL).getSize();
        Position2dUtil.TURRET_SIZE = properties.get(EntityType.TURRET).getSize();
        Position2dUtil.HOUSE_SIZE = properties.get(EntityType.HOUSE).getSize();
        Position2dUtil.RANGED_BASE_SIZE = properties.get(EntityType.RANGED_BASE).getSize();
        Position2dUtil.MELEE_BASE_SIZE = properties.get(EntityType.MELEE_BASE).getSize();
    }

    void setMyCorner(PlayerView playerView) {
        Entity myEntity = Arrays.stream(playerView.getEntities())
                .filter(e -> e.getPlayerId() != null && e.getPlayerId() == playerView.getMyId()).findAny().orElse(null);
        int myCornerX = 0;
        int myCornerY = 0;

        if (myEntity != null) {
            if (myEntity.getPosition().getX() > Position2dUtil.MAP_SIZE / 2) {
                myCornerX = Position2dUtil.MAP_SIZE - 1;
            }
            if (myEntity.getPosition().getY() > Position2dUtil.MAP_SIZE / 2) {
                myCornerY = Position2dUtil.MAP_SIZE - 1;
            }

            Position2dUtil.MY_CORNER = Position2d.of(myCornerX, myCornerY);

        }
    }

    void parseDataForSingleTick(PlayerView playerView) {
        currentParsedGameState = GameStateParser.parse(playerView);
        trackChanges();
        removeCompletedOrStaleCommands();
    }

    void trackChanges() {
        GameStateParser.calculateNewEntityIds(currentParsedGameState, gameHistoryState.getPreviousParsedGameState(), gameHistoryState);
        GameStateParser.calculateNewEntityIds(currentParsedGameState, gameHistoryState.getPreviousParsedGameState(), gameHistoryState);
        GameStateParser.calculateWorkersMovedSinceLastTurn(currentParsedGameState, gameHistoryState.getPreviousParsedGameState());
        GameStateParser.trackRangeBaseBuildTicks(currentParsedGameState, gameHistoryState);
    }

    void removeCompletedOrStaleCommands() {
//        gameHistoryState.getOngoingBuildCommands()
//                .removeIf(this::isBuildCommandCompletedOrStale);

        for (ListIterator<Command> iterator = gameHistoryState.getOngoingCommands().listIterator(); iterator.hasNext(); ) {
            Command command = iterator.next();
            Optional<Command> replacer = command.replacer(gameHistoryState, currentParsedGameState, strategyParams);
            if(replacer.isPresent()) {
                if (DebugOut.enabled) {
                    DebugOut.println("_Replaced:" + command.toString());
                }
                iterator.remove();
                iterator.add(replacer.get());

            } else if (!command.isValid(gameHistoryState, currentParsedGameState, strategyParams)) {
                if (DebugOut.enabled) {
                    DebugOut.println("_Invalid:" + command.toString());
                }
                iterator.remove();
            } else if (command.isCompleted(gameHistoryState, currentParsedGameState, strategyParams)) {
                if (DebugOut.enabled) {
                    DebugOut.println("_Completed:" + command.toString());
                }

                iterator.remove();
                handleNextCommands(command, iterator);
            } else if (command.isStale(gameHistoryState, currentParsedGameState, strategyParams)) {
                iterator.remove();
                if (DebugOut.enabled) {
                    DebugOut.println("_Remove stale command:" + command.toString());
                }
            }
        }
    }

    void handleNextCommands(Command command, ListIterator<Command> iterator) {
        if (DebugOut.enabled) {
            DebugOut.println("Handle next commands for:" + command.toString());
        }
        for (Command nextCommand : command.getNextCommands()) {
            if (nextCommand.isCompleted(gameHistoryState, currentParsedGameState, strategyParams)) {
                if (DebugOut.enabled) {
                    DebugOut.println("Command completed immediately:" + nextCommand.toString());
                }
                handleNextCommands(nextCommand, iterator);
            } else if (nextCommand.isValid(gameHistoryState, currentParsedGameState, strategyParams)
                    && !nextCommand.isStale(gameHistoryState, currentParsedGameState, strategyParams)) {
                if (DebugOut.enabled) {
                    DebugOut.println("Adding command from chain:" + nextCommand.toString());
                }

                iterator.add(nextCommand);
            } else {
                if (DebugOut.enabled) {
                    DebugOut.println("Ignoring command (stale or invalis):" + nextCommand.toString());
                }
            }
        }
    }

    void updateAssignedActions(Map<Integer, ValuedEntityAction> assignedActions) {
        getGameHistoryState().getOngoingCommands()
                .forEach(c -> c.updateAssignedActions(gameHistoryState, currentParsedGameState, strategyParams, assignedActions));
        if(DebugOut.enabled) {
            DebugOut.println("===================");
            DebugOut.println("ALL current Ongiong commands: ");
            for (Command ongoingCommand : getGameHistoryState().getOngoingCommands()) {
                DebugOut.println("Ongonig:" + ongoingCommand);
            }
            DebugOut.println("ALL current Ongiong commands: ");
            DebugOut.println("====================");
        }
    }

    Action combineDecisions() {
        Map<Integer, ValuedEntityAction> assignedActions = new HashMap<>();

        List<SubStrategy> thisTickStrategies = subStrategies.stream()
                .filter(s -> s.isApplicableAtThisTick(gameHistoryState, currentParsedGameState, strategyParams, assignedActions))
                .collect(Collectors.toList());

        thisTickStrategies.forEach(
                ss -> ss.decide(gameHistoryState, currentParsedGameState, strategyParams, assignedActions));

        updateAssignedActions(assignedActions);

        Map<Integer, EntityAction> entityActions = assignedActions.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getEntityAction()));

        return new Action(entityActions);
    }

    void updateGameHistoryState(Action decision) {
        gameHistoryState.setPreviousTickAction(decision);
        gameHistoryState.setPreviousParsedGameState(currentParsedGameState);
        gameHistoryState.getPlayerInfo().put(currentParsedGameState.curTick(), currentParsedGameState.getPlayerView().getPlayers());
    }

    static Duration totalTimeConsumed = Duration.ZERO;

    @Override
    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {

        Instant start = Instant.now();
        Instant afterParse = Instant.now();

        try {

            initStaticParams(playerView);
            parseDataForSingleTick(playerView);
            gameHistoryState.turretRequests = new ArrayList<>();
            gameHistoryState.thisTickUsedTargetPositions = new HashSet<>();

            afterParse = Instant.now();

            Action decision = combineDecisions();
            updateGameHistoryState(decision);

            verifyResponse(decision, playerView);

            return decision;
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            e.printStackTrace();
            DebugOut.println(e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            DebugOut.println(sw.toString());
            return new Action(Collections.emptyMap());
            //Index -1 out of bounds for length 5 ??????????
        } finally {
            if(DebugOut.enabled) {
                Duration tickSpent = Duration.between(start, Instant.now());
                totalTimeConsumed = totalTimeConsumed.plus(tickSpent);

                DebugOut.println("Tick: " + playerView.getCurrentTick() +  " dur:  " + tickSpent + ", total: / " + totalTimeConsumed
                        + " / avg: " + totalTimeConsumed.dividedBy(playerView.getCurrentTick() + 1) + " / afterParse: " + Duration.between(afterParse, Instant.now()));
            }
        }
    }

    public void ensurePosition(Vec2Int position) {
        if(position.getX() < 0) {
            System.err.println("Position x < 0" + position.getX());
            position.setX(0);
        }
        if(position.getX() > 79) {
            System.err.println("Position x > 79" + position.getX());
            position.setX(79);
        }
        if(position.getY() < 0) {
            System.err.println("Position y < 0" + position.getY());
            position.setY(0);
        }
        if(position.getY() > 79) {
            System.err.println("Position x > 79" + position.getY());
            position.setY(79);
        }
    }




    void verifyResponse(Action action, PlayerView pw) {

        Set<Integer> entityIds = Arrays.stream(pw.getEntities()).map(Entity::getId).collect(Collectors.toSet());
        action.getEntityActions().entrySet().removeIf(e-> !entityIds.contains(e.getKey()));

        action.getEntityActions().forEach((id, ea) -> {
            if(ea.getMoveAction() != null) {
                ensurePosition(ea.getMoveAction().getTarget());
            }
            if(ea.getBuildAction()!= null) {
                ensurePosition(ea.getBuildAction().getPosition());
            }
        } );

    }

    @Override
    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }

    public GameHistoryAndSharedState getGameHistoryState() {
        return gameHistoryState;
    }

    public ParsedGameState getCurrentParsedGameState() {
        return currentParsedGameState;
    }

    public StrategyParams getStrategyParams() {
        return strategyParams;
    }

    public List<SubStrategy> getSubStrategies() {
        return subStrategies;
    }
}
