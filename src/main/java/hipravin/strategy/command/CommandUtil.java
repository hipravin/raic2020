package hipravin.strategy.command;

import java.util.List;

public abstract class CommandUtil {

    public static void addNextCommands(Command currentCommand, List<Command> nextCommands) {
        currentCommand.getNextCommands().addAll(nextCommands);
    }

    public static void chainCommands(Command... commands) {
        Command prevCommand = null;
        for (Command command : commands) {
            if(prevCommand != null) {
                prevCommand.getNextCommands().add(command);
            }

            prevCommand = command;
        }
    }

    private CommandUtil() {
    }
}
