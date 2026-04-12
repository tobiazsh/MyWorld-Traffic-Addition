package at.tobiazsh.myworld.traffic_addition.data_fix;

import at.tobiazsh.myworld.traffic_addition.mixin.DataFixTypesAccessor;
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
 * Source: https://github.com/Layers-of-Railways/Railway/blob/1.20/dev/common/src/main/java/com/railwayteam/railways/base/datafixerapi/DataFixesInternalsImpl.java
 */

public class DataFixesImpl extends AbstractDataFixes {
    private final @NotNull Schema latestVanillaSchema;

    private DataFixerEntry dataFixer;

    public DataFixesImpl(@NotNull Schema schema) {
        this.latestVanillaSchema = schema;
    }

    @Override
    public void registerFixer(int currentVersion, @NotNull DataFixer dataFixer) {
        if (this.dataFixer != null)
            throw new IllegalStateException("Cannot register data fixer twice");

        this.dataFixer = new DataFixerEntry(dataFixer, currentVersion);
    }

    @Override
    public @Nullable DataFixerEntry getFix(int version) {
        return dataFixer;
    }

    @Override
    public @NotNull Schema createBaseSchema(@NotNull BiFunction<Integer, Schema, Schema> factory) {
        return factory.apply(0, this.latestVanillaSchema);
    }

    @Override
    public @NotNull <T> Dynamic<T> updateWithAllFixers(@NotNull DataFixTypes dataFixTypes, @NotNull Dynamic<T> dynamic) {
        return updateWithAllFixers(((DataFixTypesAccessor) (Object) dataFixTypes).mwta$getType(), dynamic);
    }

    @Override
    public @NotNull <T> Dynamic<T> updateWithAllFixers(DSL.@NotNull TypeReference rootType, @NotNull Dynamic<T> dynamic) {
        if (dataFixer == null)
            return dynamic;

        int modDataVersion = AbstractDataFixes.getModDataVersion(dynamic);
        return dataFixer.data().update(rootType, dynamic, modDataVersion, dataFixer.version());
    }

    @Override
    public void addModDataVersion(@NotNull CompoundTag compound) {
        if (dataFixer != null)
            compound.putInt("MyWorld-Traffic-Addition_DataVersion", dataFixer.version());
    }
}
