package net.minecraftforge.client.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RenderBlockOverlayEvent extends Event {
    public static final boolean LIQUIDBOUNCE_MCP_STUB = true;

    public static enum OverlayType {
        FIRE,
        WATER,
        BLOCK;

        public static final boolean LIQUIDBOUNCE_MCP_STUB = true;
    }

    public final EntityPlayer player;
    public final float renderPartialTicks;
    public final OverlayType overlayType;
    public final IBlockState blockForOverlay;
    public final BlockPos blockPos;

    public RenderBlockOverlayEvent(EntityPlayer player, float renderPartialTicks, OverlayType overlayType, IBlockState blockForOverlay, BlockPos blockPos) {
        this.player = player;
        this.renderPartialTicks = renderPartialTicks;
        this.overlayType = overlayType;
        this.blockForOverlay = blockForOverlay;
        this.blockPos = blockPos;
    }
}
