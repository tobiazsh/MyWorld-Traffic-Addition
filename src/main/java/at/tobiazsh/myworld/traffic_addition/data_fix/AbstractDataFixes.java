package at.tobiazsh.myworld.traffic_addition.data_fix;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/*
 * This class use code from "Create Steam 'n' Rails", licensed under the Apache-2.0 license.
 * Source: https://github.com/Layers-of-Railways/Railway/blob/1.20/dev/common/src/main/java/com/railwayteam/railways/base/datafixerapi/DataFixesInternals.java#L40
 */

public abstract class AbstractDataFixes {

    public static BiFunction<Integer, Schema, Schema> baseSchema(BiFunction<Integer, Schema, Schema> factory) {
        return (version, parent) -> {
            Preconditions.checkArgument(version == 0, "Version has to be 0!");
            Preconditions.checkArgument(parent == null, "Parent has to be null!");
            return getInstance().createBaseSchema(factory);
        };
    }

    @Contract(pure = true)
    public static int getModDataVersion(@NotNull CompoundTag compound) {
        return compound.getInt("MyWorld-Traffic-Addition_DataVersion").orElse(0);
    }

    @Contract(pure = true)
    public static <T> int getModDataVersion(@NotNull Dynamic<T> dynamic) {
        return dynamic.get("MyWorld-Traffic-Addition_DataVersion").asInt(0);
    }

    private static AbstractDataFixes instance;

    public static @NotNull AbstractDataFixes getInstance() {
        if (instance == null) {
            try {
                Schema latestVanillaSchema = DataFixers.getDataFixer()
                        .getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().dataVersion().version()));

                instance = new DataFixesImpl(latestVanillaSchema);
            } catch (Exception e) {
                MyWorldTrafficAddition.LOGGER.error(
                        "Failed to get latest vanilla schema!\n" +
                        "This will cause data fixer registration to fail!\n" +
                        "Will continue with no-op implementation!", e
                );

                instance = new DataFixesImplNoOp();
            }
        }

        return instance;
    }

    public abstract void registerFixer(int currentVersion, @NotNull DataFixer dataFixer);
    public abstract @Nullable DataFixerEntry getFix(int version);

    public abstract @NotNull Schema createBaseSchema(@NotNull BiFunction<Integer, Schema, Schema> factory);

    public abstract <T> @NotNull Dynamic<T> updateWithAllFixers(@NotNull DataFixTypes dataFixTypes, @NotNull Dynamic<T> dynamic);
    public abstract <T> @NotNull Dynamic<T> updateWithAllFixers(@NotNull DSL.TypeReference rootType, @NotNull Dynamic<T> dynamic);

    public abstract void addModDataVersion(@NotNull CompoundTag compound);

    public record DataFixerEntry(DataFixer data, int version) {}
}
