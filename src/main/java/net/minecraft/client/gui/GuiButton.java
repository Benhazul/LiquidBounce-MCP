package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import java.awt.Color;

import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;

public class GuiButton extends Gui
{
    protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
    protected int width;
    protected int height;
    public int xPosition;
    public int yPosition;
    public String displayString;
    public int id;
    public boolean enabled;
    public boolean visible;
    protected boolean hovered;

    private long startTime = -1L;
    private boolean lastHover = false;
    private float progress = 0;

    public GuiButton(int buttonId, int x, int y, String buttonText)
    {
        this(buttonId, x, y, 200, 20, buttonText);
    }

    public GuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        this.width = 200;
        this.height = 20;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = widthIn;
        this.height = heightIn;
        this.displayString = buttonText;
        this.progress = (float)x;
    }

    protected int getHoverState(boolean mouseOver)
    {
        int i = 1;
        if (!this.enabled) i = 0;
        else if (mouseOver) i = 2;
        return i;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            float f = (float)this.width;

            if (this instanceof GuiOptionSlider)
            {
                f *= ((GuiOptionSlider)this).sliderValue;
                this.hovered = true;
            }

            if (this instanceof GuiScreenOptionsSounds.Button)
            {
                f *= ((GuiScreenOptionsSounds.Button)this).field_146156_o;
                this.hovered = true;
            }

            if (this.hovered != this.lastHover)
            {
                if (System.currentTimeMillis() - this.startTime > 200L)
                {
                    this.startTime = System.currentTimeMillis();
                }
                this.lastHover = this.hovered;
            }

            long i = System.currentTimeMillis() - this.startTime;
            float f1 = (this.enabled && this.hovered) ? (float)this.xPosition : this.progress;
            float f2 = (this.enabled && this.hovered) ? (float)this.xPosition + f : (float)this.xPosition;

            this.progress = (f1 + (f2 - f1) * MathHelper.clamp_float((float)i / 200.0F, 0.0F, 1.0F));

            float f3 = 2.5F;

            RenderUtils.INSTANCE.withClipping(() -> {
                RenderUtils.INSTANCE.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, this.enabled ? new Color(0.0F, 0.0F, 0.0F, 0.47F).getRGB() : new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB(), f3, RenderUtils.RoundedCorners.ALL);
                return null;
            }, () -> {
                if (this.enabled && this.progress != (float)this.xPosition)
                {
                    RenderUtils.INSTANCE.drawGradientRect(this.xPosition, this.yPosition, (int)this.progress, this.yPosition + this.height, new Color(0, 139, 139).getRGB(), new Color(0, 0, 139).getRGB(), 0.0F);
                }
                return null;
            });

            mc.getTextureManager().bindTexture(buttonTextures);
            this.mouseDragged(mc, mouseX, mouseY);

            AWTFontRenderer.Companion.setAssumeNonVolatile(true);
            final FontRenderer fontrenderer = Fonts.fontSemibold35;
            fontrenderer.drawStringWithShadow(this.displayString, (float)(this.xPosition + this.width / 2 - fontrenderer.getStringWidth(this.displayString) / 2), (float)this.yPosition + (float)(this.height - 5) / 2.0F, 14737632);
            AWTFontRenderer.Companion.setAssumeNonVolatile(false);

            GlStateManager.resetColor();
        }
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {}

    public void mouseReleased(int mouseX, int mouseY) {}

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    public boolean isMouseOver()
    {
        return this.hovered;
    }

    public void drawButtonForegroundLayer(int mouseX, int mouseY) {}

    public void playPressSound(SoundHandler soundHandlerIn)
    {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public int getButtonWidth()
    {
        return this.width;
    }

    public void setWidth(int widthIn)
    {
        this.width = widthIn;
    }
}