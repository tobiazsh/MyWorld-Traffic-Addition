package at.tobiazsh.myworld.traffic_addition.data_fix;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.DataFixTypes;
import org.jetbrains.annotations.NotNull;

public interface DataFixInterface {
    int getVersion();
    Dynamic<?> update(@NotNull DataFixTypes dataFixTypes, Dynamic<?> dynamic);
}
