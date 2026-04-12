package at.tobiazsh.myworld.traffic_addition.data_fix.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

import java.util.Map;
import java.util.function.Supplier;

public class EmptySchema extends Schema {

    public EmptySchema(int versionKey) {
        super(versionKey, null);
    }

    @Override
    public void registerType(boolean recursive, DSL.TypeReference type, Supplier<TypeTemplate> template) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Map<String, Type<?>> buildTypes() {
        return Object2ObjectMaps.emptyMap();
    }

}