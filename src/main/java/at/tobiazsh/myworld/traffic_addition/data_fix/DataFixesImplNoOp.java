package at.tobiazsh.myworld.traffic_addition.data_fix;

import at.tobiazsh.myworld.traffic_addition.data_fix.schema.EmptySchema;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/*
 * This class use code from "Create Steam 'n' Rails", licensed under the Apache-2.0 license.
 * Source: https://github.com/Layers-of-Railways/Railway/blob/1.20/dev/common/src/main/java/com/railwayteam/railways/base/datafixerapi/NoOpDataFixesInternals.java#L35
 */

public class DataFixesImplNoOp extends AbstractDataFixes {

    private final Schema schema;

    public DataFixesImplNoOp() {
        schema = new EmptySchema(0);
    }

    @Override
    public void registerFixer(int currentVersion, @NotNull DataFixer dataFixer) {

    }

    @Override
    public @Nullable DataFixerEntry getFix(int version) {
        return null;
    }

    @Override
    public @NotNull Schema createBaseSchema(@NotNull BiFunction<Integer, Schema, Schema> factory) {
        return schema;
    }

    @Override
    public @NotNull <T> Dynamic<T> updateWithAllFixers(@NotNull DataFixTypes dataFixTypes, @NotNull Dynamic<T> dynamic) {
        return dynamic;
    }

    @Override
    public @NotNull <T> Dynamic<T> updateWithAllFixers(DSL.@NotNull TypeReference rootType, @NotNull Dynamic<T> dynamic) {
        return dynamic;
    }

    @Override
    public void addModDataVersion(@NotNull CompoundTag compound) {}
}
