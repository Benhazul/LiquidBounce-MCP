package net.minecraftforge.event;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

public class ForgeEventFactory {
    public static void onPlayerDestroyItem(EntityPlayer player, ItemStack stack) {
        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, stack));
    }

    public static Event.Result canEntityDespawn(EntityLiving entity) {
        return Event.Result.DEFAULT;
    }

    public static Event.Result canEntitySpawn(EntityLiving entity, World world, float x, float y, float z) {
        return Event.Result.DEFAULT;
    }

    public static boolean doSpecialSpawn(EntityLiving entity, World world, float x, float y, float z) {
        return false;
    }

    public static int getMaxSpawnPackSize(EntityLiving entity) {
        return entity.getMaxSpawnedInChunk();
    }

    public static boolean renderBlockOverlay(EntityPlayer player, float partialTicks, RenderBlockOverlayEvent.OverlayType type, IBlockState state, BlockPos pos) {
        return MinecraftForge.EVENT_BUS.post(new RenderBlockOverlayEvent(player, partialTicks, type, state, pos));
    }

    public static boolean renderFireOverlay(EntityPlayer player, float partialTicks) {
        return renderBlockOverlay(player, partialTicks, RenderBlockOverlayEvent.OverlayType.FIRE, net.minecraft.init.Blocks.fire.getDefaultState(), new BlockPos(player));
    }

    public static boolean renderWaterOverlay(EntityPlayer player, float partialTicks) {
        return renderBlockOverlay(player, partialTicks, RenderBlockOverlayEvent.OverlayType.WATER, net.minecraft.init.Blocks.water.getDefaultState(), new BlockPos(player));
    }

    public static boolean doPlayerHarvestCheck(EntityPlayer player, Block block, boolean success) {
        return success;
    }

    public static float getBreakSpeed(EntityPlayer player, IBlockState state, float original, BlockPos pos) {
        return original;
    }

    public static boolean onEntityStruckByLightning(Entity entity, net.minecraft.entity.effect.EntityLightningBolt bolt) {
        return false;
    }

    public static int onItemUseStart(EntityPlayer player, ItemStack item, int duration) {
        return duration;
    }

    public static int onItemUseTick(EntityPlayer player, ItemStack item, int duration) {
        return duration;
    }

    public static boolean onUseItemStop(EntityPlayer player, ItemStack item, int duration) {
        return false;
    }

    public static ItemStack onItemUseFinish(EntityPlayer player, ItemStack item, int duration, ItemStack result) {
        return result;
    }

    public static IChatComponent onClientChat(byte type, IChatComponent message) {
        return message;
    }

    public static boolean canEntityUpdate(Entity entity) {
        return true;
    }

    public static boolean canInteractWith(EntityPlayer player, Entity entity) {
        return true;
    }

    public static boolean canMountEntity(Entity entity, Entity rider, boolean isMounting) {
        return true;
    }

    public static float onLivingHeal(EntityLivingBase entity, float amount) {
        return amount;
    }
}
