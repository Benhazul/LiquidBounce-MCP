package net.minecraftforge.fml.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiSlider extends GuiButtonExt {
    public double sliderValue = 1.0D;
    public String dispString = "";
    public boolean dragging = false;
    public boolean showDecimal = true;
    public double minValue = 0.0D;
    public double maxValue = 5.0D;
    public int precision = 1;
    public ISlider parent = null;
    public String suffix = "";
    public boolean drawString = true;

    public interface ISlider {
        void onChangeSliderValue(GuiSlider slider);
    }

    public GuiSlider(int id, int xPos, int yPos, int width, int height, String prefix, String suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr) {
        this(id, xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, null);
    }

    public GuiSlider(int id, int xPos, int yPos, int width, int height, String prefix, String suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, ISlider par) {
        super(id, xPos, yPos, width, height, prefix);
        this.minValue = minVal;
        this.maxValue = maxVal;
        this.sliderValue = (currentVal - minValue) / (maxValue - minValue);
        this.dispString = prefix;
        this.suffix = suf;
        this.showDecimal = showDec;
        this.drawString = drawStr;
        this.parent = par;
        this.updateSlider();
    }

    public GuiSlider(int id, int xPos, int yPos, String displayStr, double minVal, double maxVal, double currentVal, ISlider par) {
        this(id, xPos, yPos, 150, 20, displayStr, "", minVal, maxVal, currentVal, true, true, par);
    }

    @Override
    public int getHoverState(boolean mouseOver) {
        return 0;
    }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (double) (mouseX - (this.xPosition + 4)) / (double) (this.width - 8);
                this.updateSlider();
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GuiUtils.drawContinuousTexturedBox(buttonTextures, this.xPosition + (int) (this.sliderValue * (double) (this.width - 8)), this.yPosition, 0, 66, 8, this.height, 200, 20, 2, 3, 2, 2, this.zLevel);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (double) (mouseX - (this.xPosition + 4)) / (double) (this.width - 8);
            this.updateSlider();
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    public void updateSlider() {
        if (this.sliderValue < 0.0D) {
            this.sliderValue = 0.0D;
        }

        if (this.sliderValue > 1.0D) {
            this.sliderValue = 1.0D;
        }

        String val;

        if (this.showDecimal) {
            val = Double.toString(this.sliderValue * (this.maxValue - this.minValue) + this.minValue);

            if (val.substring(val.indexOf(".") + 1).length() > this.precision) {
                val = val.substring(0, val.indexOf(".") + this.precision + 1);

                if (val.endsWith(".")) {
                    val = val.substring(0, val.indexOf(".") + this.precision);
                }
            } else {
                while (val.substring(val.indexOf(".") + 1).length() < this.precision) {
                    val = val + "0";
                }
            }
        } else {
            val = Integer.toString((int) Math.round(this.sliderValue * (this.maxValue - this.minValue) + this.minValue));
        }

        if (this.drawString) {
            this.displayString = this.dispString + val + this.suffix;
        }

        if (this.parent != null) {
            this.parent.onChangeSliderValue(this);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }

    public int getValueInt() {
        return (int) Math.round(this.sliderValue * (this.maxValue - this.minValue) + this.minValue);
    }

    public double getValue() {
        return this.sliderValue * (this.maxValue - this.minValue) + this.minValue;
    }

    public void setValue(double d) {
        this.sliderValue = (d - this.minValue) / (this.maxValue - this.minValue);
    }
}
