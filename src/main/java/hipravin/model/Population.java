package hipravin.model;

import model.Entity;
import model.EntityProperties;
import model.EntityType;
import model.PlayerView;

import java.util.Arrays;
import java.util.Map;

public class Population {
    int populationUse = -1;
    int activeLimit = -1;
    int potentialLimit = -1;//includes inactive buildings
    int myWorkerCount = -1;
    int myRangerCount = -1;


    public static Population of(PlayerView playerView) {
        Map<EntityType, EntityProperties> properties = playerView.getEntityProperties();

        Population population = new Population();
        population.populationUse = Arrays.stream(playerView.getEntities())
                .filter(e -> e.getPlayerId() != null)
                .filter(e -> e.getPlayerId() == playerView.getMyId())
                .map(e -> properties.get(e.getEntityType()).getPopulationUse()).reduce(Integer::sum).orElse(0);

        population.activeLimit = Arrays.stream(playerView.getEntities())
                .filter(e -> e.getPlayerId() != null)
                .filter(e -> e.getPlayerId() == playerView.getMyId())
                .filter(Entity::isActive)
                .map(e -> properties.get(e.getEntityType()).getPopulationProvide()).reduce(Integer::sum).orElse(0);

        population.potentialLimit = Arrays.stream(playerView.getEntities())
                .filter(e -> e.getPlayerId() != null)
                .filter(e -> e.getPlayerId() == playerView.getMyId())
                .map(e -> properties.get(e.getEntityType()).getPopulationProvide()).reduce(Integer::sum).orElse(0);

        population.myWorkerCount = (int) Arrays.stream(playerView.getEntities())
                .filter(e -> e.getPlayerId() != null)
                .filter(e -> e.getPlayerId() == playerView.getMyId())
                .filter(e -> e.getEntityType() == EntityType.BUILDER_UNIT)
                .count();

        population.myRangerCount = (int) Arrays.stream(playerView.getEntities())
                .filter(e -> e.getPlayerId() != null)
                .filter(e -> e.getPlayerId() == playerView.getMyId())
                .filter(e -> e.getEntityType() == EntityType.RANGED_UNIT)
                .count();

        return population;
    }

    public int getPopulationUse() {
        return populationUse;
    }

    public int getActiveLimit() {
        return activeLimit;
    }

    public int getPotentialLimit() {
        return potentialLimit;
    }

    public int getMyWorkerCount() {
        return myWorkerCount;
    }

    public int getMyRangerCount() {
        return myRangerCount;
    }
}
