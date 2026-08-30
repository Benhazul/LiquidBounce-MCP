package net.minecraftforge.fml.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GuiUtils {
    public static void drawContinuousTexturedBox(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int borderSize, float zLevel) {
        drawContinuousTexturedBox(x, y, u, v, width, height, textureWidth, textureHeight, borderSize, borderSize, borderSize, borderSize, zLevel);
    }

    public static void drawContinuousTexturedBox(ResourceLocation res, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int borderSize, float zLevel) {
        drawContinuousTexturedBox(res, x, y, u, v, width, height, textureWidth, textureHeight, borderSize, borderSize, borderSize, borderSize, zLevel);
    }

    public static void drawContinuousTexturedBox(ResourceLocation res, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
        drawContinuousTexturedBox(x, y, u, v, width, height, textureWidth, textureHeight, topBorder, bottomBorder, leftBorder, rightBorder, zLevel);
    }

    public static void drawContinuousTexturedBox(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int fillerWidth = width - leftBorder - rightBorder;
        int fillerHeight = height - topBorder - bottomBorder;
        int canvasWidth = textureWidth - leftBorder - rightBorder;
        int canvasHeight = textureHeight - topBorder - bottomBorder;
        int xPasses = fillerWidth / canvasWidth;
        int remainderWidth = fillerWidth % canvasWidth;
        int yPasses = fillerHeight / canvasHeight;
        int remainderHeight = fillerHeight % canvasHeight;

        // Top Left
        drawTexturedModalRect(x, y, u, v, leftBorder, topBorder, zLevel);
        // Top Right
        drawTexturedModalRect(x + leftBorder + fillerWidth, y, u + leftBorder + canvasWidth, v, rightBorder, topBorder, zLevel);
        // Bottom Left
        drawTexturedModalRect(x, y + topBorder + fillerHeight, u, v + topBorder + canvasHeight, leftBorder, bottomBorder, zLevel);
        // Bottom Right
        drawTexturedModalRect(x + leftBorder + fillerWidth, y + topBorder + fillerHeight, u + leftBorder + canvasWidth, v + topBorder + canvasHeight, rightBorder, bottomBorder, zLevel);

        for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); ++i) {
            int currentX = x + leftBorder + i * canvasWidth;
            int currentWidth = (i == xPasses) ? remainderWidth : canvasWidth;

            // Top border
            drawTexturedModalRect(currentX, y, u + leftBorder, v, currentWidth, topBorder, zLevel);
            // Bottom border
            drawTexturedModalRect(currentX, y + topBorder + fillerHeight, u + leftBorder, v + topBorder + canvasHeight, currentWidth, bottomBorder, zLevel);

            for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); ++j) {
                int currentY = y + topBorder + j * canvasHeight;
                int currentHeight = (j == yPasses) ? remainderHeight : canvasHeight;

                drawTexturedModalRect(currentX, currentY, u + leftBorder, v + topBorder, currentWidth, currentHeight, zLevel);
            }
        }

        for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); ++j) {
            int currentY = y + topBorder + j * canvasHeight;
            int currentHeight = (j == yPasses) ? remainderHeight : canvasHeight;

            // Left border
            drawTexturedModalRect(x, currentY, u, v + topBorder, leftBorder, currentHeight, zLevel);
            // Right border
            drawTexturedModalRect(x + leftBorder + fillerWidth, currentY, u + leftBorder + canvasWidth, v + topBorder, rightBorder, currentHeight, zLevel);
        }
    }

    public static void drawTexturedModalRect(int x, int y, int u, int v, int width, int height, float zLevel) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double) (x + 0), (double) (y + height), (double) zLevel).tex((double) ((float) (u + 0) * f), (double) ((float) (v + height) * f1)).endVertex();
        worldrenderer.pos((double) (x + width), (double) (y + height), (double) zLevel).tex((double) ((float) (u + width) * f), (double) ((float) (v + height) * f1)).endVertex();
        worldrenderer.pos((double) (x + width), (double) (y + 0), (double) zLevel).tex((double) ((float) (u + width) * f), (double) ((float) (v + 0) * f1)).endVertex();
        worldrenderer.pos((double) (x + 0), (double) (y + 0), (double) zLevel).tex((double) ((float) (u + 0) * f), (double) ((float) (v + 0) * f1)).endVertex();
        tessellator.draw();
    }
}
