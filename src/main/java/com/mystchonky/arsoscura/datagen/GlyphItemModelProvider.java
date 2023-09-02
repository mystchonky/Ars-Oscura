package com.mystchonky.arsoscura.datagen;

import com.mystchonky.arsoscura.ArsOscura;
import com.mystchonky.arsoscura.common.init.Integrations;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GlyphItemModelProvider extends ItemModelProvider {
    public GlyphItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, ArsOscura.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        Integrations.registeredSpells.forEach(spell -> basicItem(spell.getRegistryName()));
    }
}
