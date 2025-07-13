package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;

import imgui.ImGui;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.function.Consumer;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class UserInputPopup {

    public enum InputType {
        STRING, INTEGER, FLOAT
    }

    private static boolean shouldOpen = false;
    private static boolean waitingOnInput = true;
    private static String title;
    private static String input;
    private static InputType type;

    private static ImString inputString = new ImString(512);
    private static ImInt inputInt;
    private static ImFloat inputFloat;

    public static void render() {
        if (ImGui.beginPopupModal(title + "##UserInputPopup")) {
            switch(type) {
                case STRING -> ImGui.inputText("##StringInputUserInputPopup", inputString);

                case INTEGER -> ImGui.inputInt("##IntInputUserInputPopup", inputInt, 1, 5);

                case FLOAT -> ImGui.inputFloat("##FloatInputUserInputPopup", inputFloat, 0.25f, 1, "%.2f");
            }

            if (ImGui.button(tr("Global", "Confirm") + "##UserInputPopup")) confirm();
            ImGui.sameLine();
            if (ImGui.button(tr("Global", "Cancel") + "##UserInputPopup")) cancel();

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup(title + "##UserInputPopup");
            shouldOpen = false;
        }
    }

    public static void open(String title, InputType type, Consumer<String> onInputCompleted) {
        shouldOpen = true;
        waitingOnInput = true;

        resetValues();
        UserInputPopup.title = title;
        UserInputPopup.type = type;

        new Thread(() -> {
            while (UserInputPopup.waitingOnInput) {
                try { Thread.sleep(500); } catch (InterruptedException ignore) {}
            }

            onInputCompleted.accept(input);
        }).start();
    }

    public static void close() {
        waitingOnInput = false;
        ImGui.closeCurrentPopup();
    }

    public static void confirm() {
        setInput();
        close();
    }

    public static void cancel() {
        input = "";
        close();
    }

    public static void setInput() {
        switch(type) {
            case STRING -> input = inputString.get();
            case INTEGER -> input = String.valueOf(inputInt.get());
            case FLOAT -> input = String.valueOf(inputFloat.get());
        }
    }

    private static void resetValues() {
        input = "";
        title = "";
        type = null;
        inputString = new ImString(512);
        inputInt = new ImInt();
        inputFloat = new ImFloat();
    }
}
