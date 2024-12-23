package at.tobiazsh.myworld.traffic_addition.CustomizableSign;

import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.ErrorPopup;
import at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignStyle;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.SetCustomizableSignTexture;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.UpdateTextureVarsCustomizableSignBlockPayload;
import io.netty.util.internal.StringUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SignOperation {
    public static class Json {

        public static void write(BlockPos pos, CustomizableSignStyle signJson, List<BaseElement> drawables) {
            signJson = signJson.setElements(drawables);

            if (StringUtil.isNullOrEmpty(signJson.jsonString)) {
                ErrorPopup.open("Error", "Couldn't write to Sign: Current JSON is Empty! It seems like nothing has been edited!", ()->{});
                return;
            }

            ClientPlayNetworking.send(new SetCustomizableSignTexture(pos, signJson.jsonString));
            ClientPlayNetworking.send(new UpdateTextureVarsCustomizableSignBlockPayload(pos));
        }

        public static class Reader {

            private List<BaseElement> drawables = new ArrayList<>();
            private List<String> backgroundTextures = new ArrayList<>();
            CustomizableSignStyle json = new CustomizableSignStyle();

            public void readFromBlock(BlockPos pos, World world) {
                BlockEntity blockEntity = world.getBlockEntity(pos);

                if (!(blockEntity instanceof CustomizableSignBlockEntity)) return; // Return nothing; No BlockEntity found

                String jsonString = ((CustomizableSignBlockEntity) blockEntity).getSignTextureJson();
                if (StringUtil.isNullOrEmpty(jsonString)) return;

                CustomizableSignStyle json = new CustomizableSignStyle();
                json.setJsonString(jsonString);

                readFromJson(json);
                this.json = json;
            }

            public void readFromJson(CustomizableSignStyle json) {
                if (json.json.has("Style")) this.backgroundTextures = CustomizableSignStyle.deconstructStyleToArray(json);
                if (json.json.has("Elements")) this.drawables = CustomizableSignStyle.deconstructElementsToArray(json);
            }

            public List<BaseElement> getDrawables() {
                return this.drawables;
            }

            public List<String> getBackgroundTextures() {
                return this.backgroundTextures;
            }

            public CustomizableSignStyle getJson() {
                return this.json;
            }
        }
    }
}
