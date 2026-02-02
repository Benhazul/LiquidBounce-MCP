package net.minecraft.client.renderer.tileentity;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;

import static net.minecraft.client.renderer.GlStateManager.*;
import static net.minecraft.client.renderer.GlStateManager.disableCull;
import static net.minecraft.client.renderer.GlStateManager.enableCull;
import static net.minecraft.client.renderer.GlStateManager.popMatrix;

public class TileEntityItemStackRenderer
{
    public static TileEntityItemStackRenderer instance = new TileEntityItemStackRenderer();
    private TileEntityChest field_147717_b = new TileEntityChest(0);
    private TileEntityChest field_147718_c = new TileEntityChest(1);
    private TileEntityEnderChest enderChest = new TileEntityEnderChest();
    private TileEntityBanner banner = new TileEntityBanner();
    private TileEntitySkull skull = new TileEntitySkull();

    public void renderByItem(ItemStack itemStackIn) {
        if (itemStackIn.getItem() == Items.banner) {
            banner.setItemValues(itemStackIn);
            TileEntityRendererDispatcher.instance.renderTileEntityAt(banner, 0, 0, 0, 0f);
        } else if (itemStackIn.getItem() == Items.skull) {
            GameProfile gameprofile = null;

            if (itemStackIn.hasTagCompound()) {
                NBTTagCompound nbttagcompound = itemStackIn.getTagCompound();

                try {
                    if (nbttagcompound.hasKey("SkullOwner", 10)) {
                        gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
                    } else if (nbttagcompound.hasKey("SkullOwner", 8) && nbttagcompound.getString("SkullOwner").length() > 0) {
                        GameProfile lvt_2_2_ = new GameProfile(null, nbttagcompound.getString("SkullOwner"));
                        gameprofile = TileEntitySkull.updateGameprofile(lvt_2_2_);
                        nbttagcompound.removeTag("SkullOwner");
                        nbttagcompound.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameprofile));
                    }
                } catch(Exception ignored) {
                }
            }

            if (TileEntitySkullRenderer.instance != null) {
                pushMatrix();
                translate(-0.5F, 0f, -0.5F);
                scale(2f, 2f, 2f);
                disableCull();
                TileEntitySkullRenderer.instance.renderSkull(0f, 0f, 0f, EnumFacing.UP, 0f, itemStackIn.getMetadata(), gameprofile, -1);
                enableCull();
                popMatrix();
            }
        } else {
            Block block = Block.getBlockFromItem(itemStackIn.getItem());

            if (block == Blocks.ender_chest) {
                TileEntityRendererDispatcher.instance.renderTileEntityAt(enderChest, 0, 0, 0, 0f);
            } else if (block == Blocks.trapped_chest) {
                TileEntityRendererDispatcher.instance.renderTileEntityAt(field_147718_c, 0, 0, 0, 0f);
//            } else if (block != Blocks.chest)
//                net.minecraftforge.client.ForgeHooksClient.renderTileItem(itemStackIn.getItem(), itemStackIn.getMetadata());
            } else {
                TileEntityRendererDispatcher.instance.renderTileEntityAt(field_147717_b, 0, 0, 0, 0f);
            }
        }
    }
}
