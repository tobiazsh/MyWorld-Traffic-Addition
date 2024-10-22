package at.tobiazsh.myworld.traffic_addition;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static Item registerItem(Item item, String id) {
        Identifier itemId = Identifier.of(MyWorldTrafficAddition.MOD_ID, id);
        return Registry.register(Registries.ITEM, itemId, item);
    }

    public static final Item AUSTRIA_ITEM = registerItem(new Item(new Item.Settings().maxCount(128)), "austria_item");

    public static void initialize(){
        ItemGroupEvents.modifyEntriesEvent(ModGroups.TRAFFIC_ADDITION_AUSTRIA_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(ModItems.AUSTRIA_ITEM.asItem());
        });
    }
}
