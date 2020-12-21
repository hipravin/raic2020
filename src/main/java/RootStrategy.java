import hipravin.DebugOut;
import hipravin.model.*;
import hipravin.strategy.*;
import hipravin.strategy.command.Command;
import hipravin.strategy.command.RangerAttackHoldRetreatMicroCommand;
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
        PullToCenterStrategy pullToCenterStrategy = new PullToCenterStrategy();


        subStrategies.add(buildFirstHouseFinalStrategy);
        subStrategies.add(pushMinersStrategy);
        subStrategies.add(spawnWorkersStrategy);
        subStrategies.add(spawnRangersStrategy);
        subStrategies.add(pullToCenterStrategy);
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

            if (!playerView.isFogOfWar() && playerView.getPlayers().length > 2) {
                System.out.println("round1");
                strategyParams.activateRound1();
            }
            if (playerView.isFogOfWar() && playerView.getPlayers().length > 2) {
                strategyParams.activateRound2();
                System.out.println("round2");
            }

            if (System.getProperty("OPTION2") != null) {
                if (playerView.getPlayers().length == 2) {
                    strategyParams.activateOption2();
                }

                if (playerView.getMyId() == 1) {
                    strategyParams.activateOptionId1();
                }
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
        GameStateParser.trackEnemyBarracks(currentParsedGameState, gameHistoryState);
        GameStateParser.trackCollectedMinerals(currentParsedGameState, gameHistoryState);
    }


    void removeCompletedOrStaleCommands() {
//        gameHistoryState.getOngoingBuildCommands()
//                .removeIf(this::isBuildCommandCompletedOrStale);

        for (ListIterator<Command> iterator = gameHistoryState.getOngoingCommands().listIterator(); iterator.hasNext(); ) {
            Command command = iterator.next();
            Optional<Command> replacer = command.replacer(gameHistoryState, currentParsedGameState, strategyParams);
            if (replacer.isPresent()) {
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
        if (DebugOut.enabled) {
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
        if (playerView.getCurrentTick() % 100 == 0) {
            System.out.println("Tick #" + playerView.getCurrentTick());
        }

        Instant start = Instant.now();
        Instant afterParse = Instant.now();

        try {

            initStaticParams(playerView);
            parseDataForSingleTick(playerView);
            gameHistoryState.turretRequests = new ArrayList<>();
            gameHistoryState.thisTickUsedTargetPositions = new HashSet<>();

            sortAndProcessCommands(currentParsedGameState, gameHistoryState);

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
            if (DebugOut.enabled) {
                Duration tickSpent = Duration.between(start, Instant.now());
                totalTimeConsumed = totalTimeConsumed.plus(tickSpent);

                DebugOut.println("Tick: " + playerView.getCurrentTick() + " dur:  " + tickSpent + ", total: / " + totalTimeConsumed
                        + " / avg: " + totalTimeConsumed.dividedBy(playerView.getCurrentTick() + 1) + " / afterParse: " + Duration.between(afterParse, Instant.now()));
            }
        }
    }

    private void sortAndProcessCommands(ParsedGameState pgs, GameHistoryAndSharedState gameHistoryState) {
        gameHistoryState.getOngoingCommands().forEach(c -> c.computeLenToTarget(pgs, gameHistoryState));

        gameHistoryState.getOngoingCommands().sort(Comparator.comparingInt(Command::getLenToTarget));


        gameHistoryState.getRangerCommands().clear();
        gameHistoryState.getOngoingCommands().forEach(c -> {
            if (c instanceof RangerAttackHoldRetreatMicroCommand) {
                RangerAttackHoldRetreatMicroCommand comm = (RangerAttackHoldRetreatMicroCommand) c;
                gameHistoryState.getRangerCommands().put(comm.currentPosition(pgs), comm);
            }
        });

        if(strategyParams.useRangerManualTargeting) {
            computeAssignedAttackTargets();
        }
    }

    public void computeAssignedAttackTargets() {
        gameHistoryState.getAssignedAttackTargets().clear();
        Set<Position2d> myRangersWhoCanAttack = currentParsedGameState.getMyRangers().values().stream()
                .filter(c -> c.getAttackerCount(Position2dUtil.RANGER_RANGE) > 0)
                .map(Cell::getPosition)
                .collect(Collectors.toSet());

        int damage = 5;

        ParsedGameState pgs = currentParsedGameState;

        //ranger -> entityId
        Map<Position2d, List<Position2d>> enemiesUnderAttack = new HashMap<>();
        Map<Position2d, Integer> enemyCurrentHealth = new HashMap<>();

        Comparator<Position2d> byHealth = Comparator.comparingInt(p -> enemyCurrentHealth.get(p));

        for (Position2d rp : myRangersWhoCanAttack) {
            Position2dUtil.iterAllPositionsInRangeInclusive(rp, Position2dUtil.RANGER_RANGE, p -> {
                if (pgs.at(p).test(c -> c.isEnemySwordman() || c.isEnemyRanger())) {
                    Position2d opp = pgs.at(p).getPosition();
                    enemiesUnderAttack.merge(rp, new ArrayList<>(List.of(opp)), (l1, l2) -> {
                        l1.addAll(l2);
                        return l1;
                    });
                    enemyCurrentHealth.put(opp, pgs.at(p).getHealthLeft());
                }
            });
            Comparator<Position2d> byHealthThenByLen = byHealth.thenComparingInt(p -> rp.lenShiftSum(p));

            enemiesUnderAttack.get(rp).sort(byHealthThenByLen);
        }

        List<Position2d> myRangersOrdered = new ArrayList<>(myRangersWhoCanAttack);
        Comparator<Position2d> byxy = Comparator.comparingInt(p -> p.x + p.y);
        Comparator<Position2d> byxyThenXThenY = byxy
                .thenComparingInt(p -> p.x)
                .thenComparingInt(p -> p.y);

        myRangersOrdered.sort(byxyThenXThenY);

        for (Position2d rang : myRangersOrdered) {
            List<Position2d> targets = enemiesUnderAttack.get(rang);

            if (targets != null) {
                for (Position2d target : targets) {
                    Integer curHealth = enemyCurrentHealth.get(target);
                    if (curHealth == null || curHealth <= 0) {
                        continue;
                    }

                    enemyCurrentHealth.compute(target, (p, h) -> (h == null) ? 0 : h - damage);
                    gameHistoryState.getAssignedAttackTargets().put(pgs.at(rang).getEntityId(), pgs.at(target).getEntityId());
                    break;
                }
            }
        }
    }

    public void ensurePosition(Vec2Int position) {
        if (position.getX() < 0) {
            System.err.println("Position x < 0" + position.getX());
            position.setX(0);
        }
        if (position.getX() > 79) {
            System.err.println("Position x > 79" + position.getX());
            position.setX(79);
        }
        if (position.getY() < 0) {
            System.err.println("Position y < 0" + position.getY());
            position.setY(0);
        }
        if (position.getY() > 79) {
            System.err.println("Position x > 79" + position.getY());
            position.setY(79);
        }
    }


    void verifyResponse(Action action, PlayerView pw) {

        Set<Integer> entityIds = Arrays.stream(pw.getEntities()).map(Entity::getId).collect(Collectors.toSet());
        action.getEntityActions().entrySet().removeIf(e -> !entityIds.contains(e.getKey()));

        action.getEntityActions().forEach((id, ea) -> {
//            DebugOut.println(id + " " + ea.getMoveAction());

            if (ea.getMoveAction() != null) {
                ensurePosition(ea.getMoveAction().getTarget());
            }
            if (ea.getBuildAction() != null) {
                ensurePosition(ea.getBuildAction().getPosition());
            }
        });

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
