package net.minecraftforge.client;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

public class ForgeHooksClient {
    public static final boolean LIQUIDBOUNCE_MCP_STUB = true;

    public static void renderTileItem(Item item, int metadata) {
    }

    public static void registerTESRItemStack(Item item, int metadata, Class<? extends TileEntity> clazz) {
    }
}
