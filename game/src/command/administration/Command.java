package command.administration;

import database.Database;

import java.util.ArrayList;
import java.util.List;

public class Command {

    public static List<Command> commands = new ArrayList<>();

    private final int id;
    private ArrayList<String> namesString = new ArrayList<>();
    private final String[] arguments = new String[3];

    public Command(int id, String command, String args, String description) {
        this.id = id;

        if(command.contains("|")){
            for(String name : command.split("\\|")){
                namesString.add(name);

            }
            this.arguments[0] = command.split("\\|")[0];
        }
        else{
            namesString.add(command);
            this.arguments[0] = command;
        }

        this.arguments[1] = args == null ? "" : args;
        this.arguments[2] = description == null ? "" : description;

        Command.commands.add(this);
    }

    public ArrayList<String> getnamesString() {
        return namesString;
    }

    public int getId() {
        return id;
    }

    public String[] getArguments() {
        return arguments;
    }

    public static Command getCommandById(int id) {
        for(Command command : Command.commands)
            if(command.id == id)
                return command;
        return null;
    }

    public static void reload() {
        Command.commands.clear();
        Database.getStatics().getCommandData().load(null);
    }
}
