/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.script.api

import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.ScriptUtils
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack

class ScriptTab(private val tabObject: JSObject)
    : CreativeTabs(getNextTabId(), tabObject.getMember("name") as String) {

    companion object {
        private fun getNextTabId(): Int {
            for (i in CreativeTabs.creativeTabArray.indices) {
                if (CreativeTabs.creativeTabArray[i] == null)
                    return i
            }
            throw IllegalStateException("No free CreativeTabs slots")
        }
    }

    val items = ScriptUtils.convert(tabObject.getMember("items"), Array<ItemStack>::class.java) as Array<ItemStack>

    override fun getTabIconItem() = ItemUtils.createItem(tabObject.getMember("icon") as String)?.item

    override fun getTranslatedTabLabel() = tabObject.getMember("name") as String

    override fun displayAllReleventItems(items: MutableList<ItemStack>) = items.forEach { items += it }
}