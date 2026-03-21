package at.tobiazsh.myworld.traffic_addition.data_fix;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.data_fix.schema.V0;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.fixes.*;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.function.BiFunction;

public class GlobalDataFixer {
    private static final BiFunction<Integer, Schema, Schema> NAMESPACED = NamespacedSchema::new;

    public static void register() {
        MyWorldTrafficAddition.LOGGER.info("Registering UpsideDownTriangularSignDataFixer Data Fixer!");

        AbstractDataFixes dataFixApi = AbstractDataFixes.getInstance();

        DataFixerBuilder builder = new DataFixerBuilder(MyWorldTrafficAddition.DATA_FIXER_VERSION);
        addFixers(builder);

        dataFixApi.registerFixer(MyWorldTrafficAddition.DATA_FIXER_VERSION, builder.build().fixer());
    }

    private static void addFixers(DataFixerBuilder builder) {
        // Version 0: Broken Upside Down Triangular Signs
        Schema schemaV0 = builder.addSchema(0, AbstractDataFixes.baseSchema(V0::new));

        // For v1, need to fix upside_down_triangular_sign_block id (see method for more info)
        Schema schemaV1 = builder.addSchema(1, NAMESPACED);
        final String OLD_ID = "myworld_traffic_addition:upsidedown_triangular_sign_block";
        final String NEW_ID = "myworld_traffic_addition:upside_down_triangular_sign_block";
        builder.addFixer(BlockRenameFix.create(schemaV1, "Fix Sign Block ID", (name) -> name.equals(OLD_ID) ? NEW_ID : name));
        builder.addFixer(ItemRenameFix.create(schemaV1, "Fix Sign Item ID", (name) -> name.equals(OLD_ID) ? NEW_ID : name));
        builder.addFixer(BlockEntityRenameFix.create(schemaV1, "Fix Sign BlockEntity ID", (name) -> name.equals(OLD_ID) ? NEW_ID : name));
    }
}
