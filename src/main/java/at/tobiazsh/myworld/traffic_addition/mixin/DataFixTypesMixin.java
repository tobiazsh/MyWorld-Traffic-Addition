package at.tobiazsh.myworld.traffic_addition.mixin;

import at.tobiazsh.myworld.traffic_addition.data_fix.AbstractDataFixes;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.DataFixTypes;
import org.spongepowered.asm.mixin.Mixin;

/*
 * This class use code from "Create Steam 'n' Rails", licensed under the GNU Lesser General Public License (LGPL).
 * Source: https://github.com/Layers-of-Railways/Railway/blob/1.20/dev/common/src/main/java/com/railwayteam/railways/mixin/MixinDataFixTypes.java#L34
 */

@Mixin(DataFixTypes.class)
public class DataFixTypesMixin {
    @WrapMethod(method = "update(Lcom/mojang/datafixers/DataFixer;Lcom/mojang/serialization/Dynamic;II)Lcom/mojang/serialization/Dynamic;")
    private <T> Dynamic<T> mwta$updateFixers(DataFixer dataFixer, Dynamic<T> input, int version, int newVersion, Operation<Dynamic<T>> original) {
        Dynamic<T> vanillaFixed = original.call(dataFixer, input, version, newVersion);
        return AbstractDataFixes.getInstance().updateWithAllFixers((DataFixTypes) (Object) this, vanillaFixed);
    }
}
