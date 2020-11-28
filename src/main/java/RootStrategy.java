import hipravin.DebugOut;
import model.Action;
import model.DebugCommand;
import model.PlayerView;

public class RootStrategy extends MyStrategy {

    @Override
    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {
        DebugOut.println(playerView.toString());
        return new Action(new java.util.HashMap<>());
    }

    @Override
    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}
