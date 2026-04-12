package at.tobiazsh.myworld.traffic_addition;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ModGroups {

    public static final ResourceKey<@NotNull CreativeModeTab> TRAFFIC_ADDITION_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "traffic_addition"));
    public static final CreativeModeTab TRAFFIC_ADDITION_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModBlocks.BORDER_BLOCK.getBlock()))
            .title(Component.translatable("itemGroup.myworld_traffic_addition"))
            .build();

    static final CreativeModeTab traffic_addition_group = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TRAFFIC_ADDITION_ITEM_GROUP_KEY, TRAFFIC_ADDITION_ITEM_GROUP);

    public static void initialize() {

    }
}
