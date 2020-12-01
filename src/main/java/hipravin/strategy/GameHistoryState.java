package hipravin.strategy;

import model.Action;

public class GameHistoryState {
    Action previousTickAction;

    public Action getPreviousTickAction() {
        return previousTickAction;
    }

    public void setPreviousTickAction(Action previousTickAction) {
        this.previousTickAction = previousTickAction;
    }
}
