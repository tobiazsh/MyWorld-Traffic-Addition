package at.tobiazsh.myworld.traffic_addition.Components.Blocks;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Components.BlockEntities.SpecialBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SpecialBlock extends Block implements BlockEntityProvider {
    public static final Identifier Id = Identifier.of(MyWorldTrafficAddition.MOD_ID, "special_block");

    public SpecialBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SpecialBlockEntity(pos, state);
    }
}
