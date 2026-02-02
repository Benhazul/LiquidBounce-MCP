package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridge;
import net.ccbluex.liquidbounce.utils.client.ServerUtils;

public class GuiIngameMenu extends GuiScreen {
    private int field_146445_a;
    private int field_146444_f;

    public void initGui() {
        this.field_146445_a = 0;
        this.buttonList.clear();
        int i = -16;
        int j = 98;

        GuiButton disconnectButton = new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + i, I18n.format("menu.returnToMenu", new Object[0]));
        this.buttonList.add(disconnectButton);

        if (!this.mc.isIntegratedServerRunning()) {
            disconnectButton.displayString = I18n.format("menu.disconnect", new Object[0]);
            disconnectButton.xPosition = this.width / 2 + 2;
            disconnectButton.width = 98;
            disconnectButton.height = 20;
            this.buttonList.add(new GuiButton(1337, this.width / 2 - 100, this.height / 4 + 120 + i, 98, 20, "Reconnect"));
        }

        this.buttonList.add(new GuiButton(4, this.width / 2 - 100, this.height / 4 + 24 + i, I18n.format("menu.returnToGame", new Object[0])));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + i, 98, 20, I18n.format("menu.options", new Object[0])));

        GuiButton shareToLanBtn;
        this.buttonList.add(shareToLanBtn = new GuiButton(7, this.width / 2 + 2, this.height / 4 + 96 + i, 98, 20, I18n.format("menu.shareToLan", new Object[0])));
        this.buttonList.add(new GuiButton(5, this.width / 2 - 100, this.height / 4 + 48 + i, 98, 20, I18n.format("gui.achievements", new Object[0])));
        this.buttonList.add(new GuiButton(6, this.width / 2 + 2, this.height / 4 + 48 + i, 98, 20, I18n.format("gui.stats", new Object[0])));

        shareToLanBtn.enabled = this.mc.isSingleplayer() && !this.mc.getIntegratedServer().getPublic();
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;

            case 1:
                boolean flag = this.mc.isIntegratedServerRunning();
                boolean flag1 = this.mc.isConnectedToRealms();
                button.enabled = false;
                this.mc.theWorld.sendQuittingDisconnectingPacket();
                this.mc.loadWorld((WorldClient)null);

                if (flag) {
                    this.mc.displayGuiScreen(new net.ccbluex.liquidbounce.ui.client.GuiMainMenu());
                } else if (flag1) {
                    RealmsBridge realmsbridge = new RealmsBridge();
                    realmsbridge.switchToRealms(new net.ccbluex.liquidbounce.ui.client.GuiMainMenu());
                } else {
                    this.mc.displayGuiScreen(new GuiMultiplayer(new net.ccbluex.liquidbounce.ui.client.GuiMainMenu()));
                }
                break;

            case 4:
                this.mc.displayGuiScreen((GuiScreen)null);
                this.mc.setIngameFocus();
                break;

            case 5:
                this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
                break;

            case 6:
                this.mc.displayGuiScreen(new GuiStats(this, this.mc.thePlayer.getStatFileWriter()));
                break;

            case 7:
                this.mc.displayGuiScreen(new GuiShareToLan(this));
                break;

            case 1337:
                this.mc.theWorld.sendQuittingDisconnectingPacket();
                ServerUtils.INSTANCE.connectToLastServer();
                break;
        }
    }

    public void updateScreen() {
        super.updateScreen();
        ++this.field_146444_f;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, I18n.format("menu.game", new Object[0]), this.width / 2, 40, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}