package at.tobiazsh.myworld.traffic_addition;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModGroups {

    public static final RegistryKey<ItemGroup> TRAFFIC_ADDITION_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(MyWorldTrafficAddition.MOD_ID, "traffic_addition"));
    public static final ItemGroup TRAFFIC_ADDITION_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModBlocks.BORDER_BLOCK))
            .displayName(Text.translatable("itemGroup.myworld_traffic_addition"))
            .build();

    public static final RegistryKey<ItemGroup> TRAFFIC_ADDITION_AUSTRIA_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(MyWorldTrafficAddition.MOD_ID, "traffic_addition_austria"));
    public static final ItemGroup TRAFFIC_ADDITION_AUSTRIA_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.AUSTRIA_ITEM))
            .displayName(Text.translatable("itemGroup.myworld_traffic_addition_austria"))
            .build();

    static final ItemGroup traffic_addition_group = Registry.register(Registries.ITEM_GROUP, TRAFFIC_ADDITION_ITEM_GROUP_KEY, TRAFFIC_ADDITION_ITEM_GROUP);
    static final ItemGroup traffic_addition_austria_group = Registry.register(Registries.ITEM_GROUP, TRAFFIC_ADDITION_AUSTRIA_GROUP_KEY, TRAFFIC_ADDITION_AUSTRIA_GROUP);

    public static void initialize() {

    }
}
