package com.mystchonky.arsoscura.common.enchantment;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.TridentRiptideEnchantment;
import org.jetbrains.annotations.NotNull;

public class TorrentEnchantment extends TridentRiptideEnchantment implements IArcaneEnchantment {
    public TorrentEnchantment(Rarity rarity, EquipmentSlot... equipmentSlots) {
        super(rarity, equipmentSlots);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    public int defaultCastingCost() {
        return 50;
    }

    @Override
    public int getCastingCost(Player player, ItemStack stack) {
        return player.isInWaterOrRain() ? 0 : defaultCastingCost();
    }

    public boolean checkCompatibility(@NotNull Enchantment enchant) {
        return super.checkCompatibility(enchant) && enchant != Enchantments.RIPTIDE && !(enchant instanceof IArcaneEnchantment);
    }

    @Override
    public @NotNull Component getFullname(int level) {
        return getNameWithStyle(this, level);
    }
}
