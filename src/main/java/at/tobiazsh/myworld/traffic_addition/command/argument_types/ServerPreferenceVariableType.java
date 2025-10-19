package at.tobiazsh.myworld.traffic_addition.command.argument_types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

enum ServerPreferenceVariable {
    MAXIMUM_IMAGE_UPLOAD_SIZE,
    WHITELIST
}
public class ServerPreferenceVariableType implements ArgumentType<String> {

    @Override
    public String parse(StringReader stringReader) throws CommandSyntaxException {
        return "";
    }

}
