package at.tobiazsh.myworld.traffic_addition.datagen.translation;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class MyWorldTrafficAdditionDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(MyWorldTrafficAdditionEnglishLangProvider::new); // English
        pack.addProvider(MyWorldTrafficAdditionGermanLangProvider::new); // German
    }
}
