package at.tobiazsh.myworld.traffic_addition;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ModItems {
    public static Item registerItem(Item.Properties itemSettings, String id) {
        Identifier itemId = Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, id);
        ResourceKey<@NotNull Item> itemKey = ResourceKey.create(Registries.ITEM, itemId);

        itemSettings.setId(itemKey);
        return Registry.register(BuiltInRegistries.ITEM, itemKey, new Item(itemSettings));
    }

    /*public static final Item AUSTRIA_ITEM = registerItem(new Item.Properties().stacksTo(128), "austria_item");*/ // kept for reference

    public static void initialize(){
        /*ItemGroupEvents.modifyEntriesEvent(ModGroups.TRAFFIC_ADDITION_AUSTRIA_GROUP_KEY).register((itemGroup) -> itemGroup.accept(ModItems.AUSTRIA_ITEM.asItem()));*/ // kept for reference
    }
}
