package net.minecraft.client.gui;

import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

import static net.minecraft.client.renderer.GlStateManager.resetColor;

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
    private float progress = xPosition;
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
    }

    protected int getHoverState(boolean mouseOver)
    {
        int i = 1;

        if (!this.enabled)
        {
            i = 0;
        }
        else if (mouseOver)
        {
            i = 2;
        }

        return i;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;

            float supposedWidth = width;

            if (this instanceof GuiOptionSlider) {
                supposedWidth *= ((GuiOptionSlider) this).sliderValue;
                hovered = true;
            }

            if (this instanceof GuiScreenOptionsSounds.Button) {
                supposedWidth *= ((GuiScreenOptionsSounds.Button) this).field_146156_o;
                hovered = true;
            }

            if (hovered != lastHover) {
                if (System.currentTimeMillis() - startTime > 200L) {
                    startTime = System.currentTimeMillis();
                }
                lastHover = hovered;
            }

            long elapsed = System.currentTimeMillis() - startTime;

            float startingPos = enabled && hovered ? xPosition : progress;
            float endingPos = enabled && hovered ? xPosition + supposedWidth : xPosition;

            progress = (int) (startingPos + (endingPos - startingPos) * MathHelper.clamp_float(elapsed / 200f, 0f, 1f));

            float radius = 2.5F;

            RenderUtils.INSTANCE.withClipping(() -> {
                RenderUtils.INSTANCE.drawRoundedRect(xPosition, yPosition, xPosition + width, yPosition + height, enabled ? new Color(0F, 0F, 0F, 120 / 255f).getRGB() : new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB(), radius, RenderUtils.RoundedCorners.ALL);
                return null;
            }, () -> {
                if (enabled && progress != xPosition) {
                    // Draw blue overlay
                    RenderUtils.INSTANCE.drawGradientRect(xPosition, yPosition, progress, yPosition + height, Color.CYAN.darker().getRGB(), Color.BLUE.darker().getRGB(), 0F);
                }
                return null;
            });

            mc.getTextureManager().bindTexture(buttonTextures);
            mouseDragged(mc, mouseX, mouseY);

            AWTFontRenderer.Companion.setAssumeNonVolatile(true);

            final FontRenderer fontRenderer = Fonts.fontSemibold35;
            fontRenderer.drawStringWithShadow(displayString, (float) (xPosition + width / 2 - fontRenderer.getStringWidth(displayString) / 2), yPosition + (height - 5) / 2F, 14737632);

            AWTFontRenderer.Companion.setAssumeNonVolatile(false);

            resetColor();
        }
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
    }

    public void mouseReleased(int mouseX, int mouseY)
    {
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    public boolean isMouseOver()
    {
        return this.hovered;
    }

    public void drawButtonForegroundLayer(int mouseX, int mouseY)
    {
    }

    public void playPressSound(SoundHandler soundHandlerIn)
    {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public int getButtonWidth()
    {
        return this.width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }
}
