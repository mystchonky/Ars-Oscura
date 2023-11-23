package com.mystchonky.arsoscura.common.mixin;

import com.hollingsworth.arsnouveau.api.client.IDisplayMana;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mystchonky.arsoscura.common.init.EnchantmentRegistry;
import com.mystchonky.arsoscura.common.util.EnchantmentUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TridentItem.class)
public abstract class TridentMixin implements IDisplayMana {

    @Override
    public boolean shouldDisplay(ItemStack stack) {
        return EnchantmentUtil.isManaTool(stack);
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;startUsingItem(Lnet/minecraft/world/InteractionHand;)V"), cancellable = true)
    public void checkEnchantAndMana(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getEnchantmentLevel(EnchantmentRegistry.MANA_RIPTIDE_ENCHANTMENT.get()) > 0) {
            if (!player.isInWaterOrRain()) {
                CapabilityRegistry.getMana(player).ifPresent(manaCap -> {
                    //TODO: Use configs
                    if (manaCap.getCurrentMana() < 25) {
                        cir.setReturnValue(InteractionResultHolder.fail(stack));
                        cir.cancel();
                    }
                });

            }
        }
    }

    @WrapOperation(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getRiptide(Lnet/minecraft/world/item/ItemStack;)I"))
    public int getRiptideLevel(ItemStack stack, Operation<Integer> original, @Share("riptide") LocalBooleanRef usedManaRiptide) {
        if (EnchantmentHelper.getRiptide(stack) <= 0) {
            int level = stack.getEnchantmentLevel(EnchantmentRegistry.MANA_RIPTIDE_ENCHANTMENT.get());
            if (level > 0) {
                usedManaRiptide.set(true);
            }
            return level;
        }
        return original.call(stack);

    }

    @WrapOperation(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    public boolean rainCheck(Player player, Operation<Boolean> original, @Share("riptide") LocalBooleanRef usedManaRiptide, @Share("useMana") LocalBooleanRef useMana) {
        boolean isRaining = original.call(player);
        if (!isRaining && usedManaRiptide.get()) {
            isRaining = true;
            useMana.set(true);
        }
        return isRaining;
    }

    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/player/Player;startAutoSpinAttack(I)V"))
    public void manaCost(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft, CallbackInfo ci, @Share("useMana") LocalBooleanRef usedMana) {
        if (usedMana.get()) {
            Player player = (Player) pEntityLiving;
            //TODO: Use configs
            CapabilityRegistry.getMana(player).ifPresent(manaCap -> manaCap.removeMana(25));
        }
    }
}