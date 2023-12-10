package com.mystchonky.arsoscura.common.init;

import com.mystchonky.arsoscura.ArsOscura;
import com.mystchonky.arsoscura.common.enchantments.FealtyEnchantment;
import com.mystchonky.arsoscura.common.enchantments.TorrentEnchantment;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class EnchantmentRegistry {
    private static final Registrate REGISTRATE = ArsOscura.registrate();

    public static final RegistryEntry<TorrentEnchantment> MANA_RIPTIDE_ENCHANTMENT = REGISTRATE.object("torrent")
            .enchantment(EnchantmentCategory.TRIDENT, (rarity, type, slots) -> new TorrentEnchantment(rarity, slots))
            .rarity(Enchantment.Rarity.VERY_RARE)
            .addSlots(EquipmentSlot.MAINHAND)
            .register();

    public static final RegistryEntry<FealtyEnchantment> MANA_LOYALTY_ENCHANTMENT = REGISTRATE.object("fealty")
            .enchantment(EnchantmentCategory.TRIDENT, (rarity, type, slots) -> new FealtyEnchantment(rarity, slots))
            .rarity(Enchantment.Rarity.VERY_RARE)
            .addSlots(EquipmentSlot.MAINHAND)
            .register();

    public static void register() {
    }
}
