package net.ccbluex.liquidbounce.ui.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton

class GuiSliderMCP(
    id: Int,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    var dispString: String,
    var suffix: String,
    val min: Double,
    val max: Double,
    value: Double,
    val onChange: (GuiSliderMCP) -> Unit
) : GuiButton(id, x, y, width, height, "") {

    var value = value
        set(v) {
            field = v.coerceIn(min, max)
            updateSlider()
            onChange(this)
        }

    val valueInt: Int
        get() = value.toInt()

    private var dragging = false

    init {
        updateSlider()
    }

    fun updateSlider() {
        displayString = "$dispString$valueInt$suffix"
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            updateValue(mouseX)
            dragging = true
            return true
        }
        return false
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int) {
        dragging = false
    }

    override fun mouseDragged(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (dragging) {
            updateValue(mouseX)
        }
    }

    private fun updateValue(mouseX: Int) {
        val pct = ((mouseX - xPosition).toDouble() / width).coerceIn(0.0, 1.0)
        value = min + (max - min) * pct
    }
}
