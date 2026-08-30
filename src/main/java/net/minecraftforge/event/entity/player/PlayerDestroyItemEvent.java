package net.minecraftforge.event.entity.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PlayerDestroyItemEvent extends Event {
    public final EntityPlayer entityPlayer;
    public final ItemStack original;

    public PlayerDestroyItemEvent(EntityPlayer player, ItemStack original) {
        this.entityPlayer = player;
        this.original = original;
    }
}
