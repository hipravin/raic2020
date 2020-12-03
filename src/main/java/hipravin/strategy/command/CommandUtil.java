package hipravin.strategy.command;

public abstract class CommandUtil {

    public static Command chainCommands(Command... commands) {
        Command prevCommand = null;
        for (Command command : commands) {
            if(prevCommand != null) {
                prevCommand.setNextCommand(command);
            }

            prevCommand = command;
        }

        return commands[0];
    }


    private CommandUtil() {
    }
}
