package hipravin.strategy;

import hipravin.model.Position2d;
import org.junit.jupiter.api.Test;

class StrategyParamsTest {

    @Test
    void testAttackPOints() {
        StrategyParams strategyParams = new StrategyParams();
        for (int i = 0; i < 10; i++) {
            Position2d ap = StrategyParams.selectRandomAccordingDistribution(strategyParams.attackPoints, strategyParams.attackPointRates);
            System.out.println(ap);
        }
    }
}