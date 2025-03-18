package at.tobiazsh.myworld.traffic_addition.CustomizableSign;

import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.ErrorPopup;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Networking.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignData;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Components.BlockEntities.CustomizableSignBlockEntity;
import com.google.gson.JsonObject;
import io.netty.util.internal.StringUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SignOperation {
    public static class Json {

        public static void write(BlockPos pos, CustomizableSignData signJson, List<BaseElement> drawables) {
            signJson = signJson.setElements(drawables);

            if (StringUtil.isNullOrEmpty(signJson.jsonString)) {
                ErrorPopup.open("Error", "Couldn't write to Sign: Current JSON is Empty! It seems like nothing has been edited!", ()->{});
                return;
            }

            JsonObject blockEntityPosition = new JsonObject();
            blockEntityPosition.addProperty("x", pos.getX());
            blockEntityPosition.addProperty("y", pos.getY());
            blockEntityPosition.addProperty("z", pos.getZ());

            JsonObject constructedJson = new JsonObject();
            constructedJson.add("blockEntityPosition", blockEntityPosition);
            constructedJson.add("texture", signJson.json);

            String jsonString = constructedJson.toString();

            CustomClientNetworking.getInstance().sendStringToServer(Identifier.of(MyWorldTrafficAddition.MOD_ID, "set_customizable_sign_texture"), jsonString);
            //ClientPlayNetworking.send(new UpdateTextureVarsCustomizableSignBlockPayload(pos));
        }

        public static class Reader {

            private List<BaseElement> drawables = new ArrayList<>();
            private List<String> backgroundTextures = new ArrayList<>();
            CustomizableSignData json = new CustomizableSignData();

            public void readFromBlock(BlockPos pos, World world) {
                BlockEntity blockEntity = world.getBlockEntity(pos);

                if (!(blockEntity instanceof CustomizableSignBlockEntity)) return; // Return nothing; No BlockEntity found

                String jsonString = ((CustomizableSignBlockEntity) blockEntity).getSignTextureJson();
                if (StringUtil.isNullOrEmpty(jsonString)) return;

                CustomizableSignData json = new CustomizableSignData();
                json.setJsonString(jsonString);

                readFromJson(json, (CustomizableSignBlockEntity) blockEntity);
                this.json = json;
            }

            public void readFromJson(CustomizableSignData json, CustomizableSignBlockEntity blockEntity) {
                if (json.json.has("Style")) this.backgroundTextures = CustomizableSignData.getBackgroundTexturePathList(json, blockEntity);
                if (json.json.has("Elements")) this.drawables = CustomizableSignData.deconstructElementsToArray(json);
            }

            public List<BaseElement> getDrawables() {
                return this.drawables;
            }

            public List<String> getBackgroundTextures() {
                return this.backgroundTextures;
            }

            public CustomizableSignData getJson() {
                return this.json;
            }
        }
    }
}
