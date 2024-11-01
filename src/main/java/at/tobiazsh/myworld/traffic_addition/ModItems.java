package at.tobiazsh.myworld.traffic_addition;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {
    public static Item registerItem(Item.Settings itemSettings, String id) {
        Identifier itemId = Identifier.of(MyWorldTrafficAddition.MOD_ID, id);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, itemId);

        itemSettings.registryKey(itemKey);
        return Registry.register(Registries.ITEM, itemKey, new Item(itemSettings));
    }

    public static final Item AUSTRIA_ITEM = registerItem(new Item.Settings().maxCount(128), "austria_item");

    public static void initialize(){
        ItemGroupEvents.modifyEntriesEvent(ModGroups.TRAFFIC_ADDITION_AUSTRIA_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(ModItems.AUSTRIA_ITEM.asItem());
        });
    }
}
