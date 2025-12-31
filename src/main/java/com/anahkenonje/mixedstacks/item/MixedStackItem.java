package com.anahkenonje.mixedstacks.item;

import com.anahkenonje.mixedstacks.config.MixedStacksConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class MixedStackItem extends BundleItem {

    public MixedStackItem(Properties properties) {
        super(properties);
    }

    // --- TICK LOGIC (Auto-unwrap) ---

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player player)) return;

        ItemStack simplified = simplifyStack(stack);
        if (simplified != stack) {
            player.getInventory().setItem(slotId, simplified);
        }
    }

    // --- EATING LOGIC (Fixed for Java 25 & Mojang Mappings) ---

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bundle = player.getItemInHand(hand);
        ItemStack first = getFirstItem(bundle);

        if (player.isShiftKeyDown() || first == null || !first.has(DataComponents.FOOD)) {
            return super.use(level, player, hand);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(bundle);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null || contents.isEmpty()) return stack;

        // 1. Convert Iterable to List so we can access/modify indices
        List<ItemStack> items = new ArrayList<>();
        contents.items().forEach(s -> items.add(s.copy()));

        // 2. Get the first item (the one being eaten)
        ItemStack first = items.get(0);

        if (first.has(DataComponents.FOOD)) {
            // 3. Eat it (returns remainder, e.g. empty stack or bowl)
            ItemStack result = first.finishUsingItem(level, entity);

            // 4. Update the list: Replace or Remove
            if (result.isEmpty()) {
                items.remove(0);
            } else {
                items.set(0, result);
            }

            // 5. Save changes back to Bundle
            stack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(items));

            // 6. Check if bundle should unwrap (e.g. if empty or only 1 stack left)
            return simplifyStack(stack);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        ItemStack first = getFirstItem(stack);
        return first != null ? first.getUseDuration(entity) : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        ItemStack first = getFirstItem(stack);
        return first != null ? first.getUseAnimation() : UseAnim.NONE;
    }

    // --- INVENTORY INTERACTION LOGIC (Fixed Syncing) ---

    private void playSound(Entity entity, net.minecraft.sounds.SoundEvent sound) {
        entity.playSound(sound, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    // Case 1: Holding Mixed Stack, Right-Click on Slot Item -> Add Slot Item to Bundle
    @Override
    public boolean overrideStackedOnOther(ItemStack bundle, Slot slot, ClickAction action, Player player) {
        if (bundle.getCount() != 1 || action != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack itemStack = slot.getItem();
            if (itemStack.isEmpty()) {
                this.playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);
                return false;
            } else if (itemStack.getItem().canFitInsideContainerItems()) {
                if (canEnterMixedStack(bundle, itemStack)) {
                    BundleContents bundleContents = bundle.get(DataComponents.BUNDLE_CONTENTS);
                    if (bundleContents == null) bundleContents = BundleContents.EMPTY;

                    BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);

                    // tryInsert modifies itemStack directly!
                    if (mutable.tryInsert(itemStack) > 0) {
                        this.playSound(player, SoundEvents.BUNDLE_INSERT);
                        bundle.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());

                        // Force slot update since itemStack changed
                        slot.setChanged();

                        ItemStack simplified = simplifyStack(bundle);
                        if (simplified != bundle) {
                            player.containerMenu.setCarried(simplified);
                        }

                        return true;
                    }
                }
            }
            return false;
        }
    }

    // Case 2: Holding Item, Right-Click on Mixed Stack in Slot -> Add Held Item to Bundle
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack bundle, ItemStack itemStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (bundle.getCount() != 1 || action != ClickAction.SECONDARY) {
            return false;
        } else {
            if (itemStack.isEmpty()) {
                // Pull item out
                BundleContents bundleContents = bundle.get(DataComponents.BUNDLE_CONTENTS);
                if (bundleContents != null && !bundleContents.isEmpty()) {
                    this.playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);

                    BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
                    ItemStack pulled = mutable.removeOne();

                    bundle.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                    access.set(pulled);

                    ItemStack simplified = simplifyStack(bundle);
                    if (simplified != bundle) {
                        slot.set(simplified);
                    }

                    return true;
                }
            } else if (itemStack.getItem().canFitInsideContainerItems()) {
                // Put item in
                if (canEnterMixedStack(bundle, itemStack)) {
                    BundleContents bundleContents = bundle.get(DataComponents.BUNDLE_CONTENTS);
                    if (bundleContents == null) bundleContents = BundleContents.EMPTY;

                    BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
                    if (mutable.tryInsert(itemStack) > 0) {
                        this.playSound(player, SoundEvents.BUNDLE_INSERT);
                        bundle.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());

                        ItemStack simplified = simplifyStack(bundle);
                        if (simplified != bundle) {
                            slot.set(simplified);
                        }

                        return true;
                    }
                }
            }
            return false;
        }
    }

    // --- HELPERS ---

    private ItemStack getFirstItem(ItemStack bundle) {
        BundleContents contents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null || contents.isEmpty()) return null;
        return contents.items().iterator().next();
    }

    private ItemStack simplifyStack(ItemStack bundle) {
        BundleContents contents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null || contents.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // If only 1 stack type inside, unwrap
        if (contents.size() == 1) {
            return contents.items().iterator().next().copy();
        }

        // Logic: If multiple stacks but ALL are same type (e.g. Beef + Beef), combine and unwrap
        ItemStack firstStack = null;
        int totalCount = 0;

        for (ItemStack stack : contents.items()) {
            if (firstStack == null) {
                firstStack = stack;
            } else {
                if (!ItemStack.isSameItemSameComponents(firstStack, stack)) {
                    return bundle; // Different items -> Keep as Mixed Stack
                }
            }
            totalCount += stack.getCount();
        }

        if (firstStack != null && totalCount <= firstStack.getMaxStackSize()) {
            ItemStack combined = firstStack.copy();
            combined.setCount(totalCount);
            return combined;
        }

        return bundle;
    }

    private boolean canEnterMixedStack(ItemStack bundle, ItemStack itemToAdd) {
        BundleContents contents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null || contents.isEmpty()) return true;

        for (ItemStack inside : contents.items()) {
            if (!MixedStacksConfig.INSTANCE.canMix(inside, itemToAdd)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}