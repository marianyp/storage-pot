package dev.mariany.storagepot.datagen;

import dev.mariany.storagepot.block.SPBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class SPRecipeProvider extends FabricRecipeProvider {
    public SPRecipeProvider(FabricDataOutput output,
                            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup,
                                                 RecipeExporter exporter) {
        return new RecipeGenerator(registryLookup, exporter) {
            @Override
            public void generate() {
                this.createShaped(RecipeCategory.DECORATIONS, SPBlocks.STORAGE_POT).input('#', Items.BRICK)
                    .input('C', ConventionalItemTags.WOODEN_CHESTS).pattern(" # ").pattern("#C#").pattern(" # ")
                    .criterion("has_brick", this.conditionsFromItem(Items.BRICK)).offerTo(this.exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "Storage Pots Recipes";
    }
}
