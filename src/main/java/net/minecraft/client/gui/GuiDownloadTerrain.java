package net.minecraft.client.gui;

import java.io.IOException;

import net.ccbluex.liquidbounce.ui.client.GuiMainMenu;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.realms.RealmsBridge;
import net.optifine.CustomLoadingScreen;
import net.optifine.CustomLoadingScreens;

public class GuiDownloadTerrain extends GuiScreen {
    private NetHandlerPlayClient netHandlerPlayClient;
    private int progress;
    private CustomLoadingScreen customLoadingScreen = CustomLoadingScreens.getCustomLoadingScreen();

    public GuiDownloadTerrain(NetHandlerPlayClient netHandler) {
        this.netHandlerPlayClient = netHandler;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel", new Object[0])));
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            boolean flag = this.mc.isIntegratedServerRunning();
            boolean flag1 = this.mc.isConnectedToRealms();
            button.enabled = false;
            this.mc.theWorld.sendQuittingDisconnectingPacket();
            this.mc.loadWorld(null);

            if (flag) {
                this.mc.displayGuiScreen(new GuiMainMenu());
            } else if (flag1) {
                RealmsBridge realmsbridge = new RealmsBridge();
                realmsbridge.switchToRealms(new net.ccbluex.liquidbounce.ui.client.GuiMainMenu());
            } else {
                this.mc.displayGuiScreen(new GuiMultiplayer(new net.ccbluex.liquidbounce.ui.client.GuiMainMenu()));
            }
        }
    }

    public void updateScreen() {
        ++this.progress;

        if (this.progress % 20 == 0) {
            this.netHandlerPlayClient.addToSendQueue(new C00PacketKeepAlive());
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.customLoadingScreen != null) {
            this.customLoadingScreen.drawBackground(this.width, this.height);
        } else {
            this.drawBackground(0);
        }

        this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.downloadingTerrain", new Object[0]), this.width / 2, this.height / 2 - 50, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean doesGuiPauseGame() {
        return false;
    }
}