package net.minecraft.client.gui;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.net.Proxy;

import com.thealtening.AltService;
import com.thealtening.api.TheAltening;
import com.thealtening.api.data.AccountData;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.SessionUpdateEvent;
import net.ccbluex.liquidbounce.features.special.AutoReconnect;
import net.ccbluex.liquidbounce.features.special.ClientFixes;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.ui.client.GuiMainMenu;
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager;
import net.ccbluex.liquidbounce.ui.client.altmanager.menus.GuiLoginProgress;
import net.ccbluex.liquidbounce.ui.client.altmanager.menus.altgenerator.GuiTheAltening;
import net.ccbluex.liquidbounce.utils.client.ClientUtils;
import net.ccbluex.liquidbounce.utils.client.ServerUtils;
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Session;
import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class GuiDisconnected extends GuiScreen {
    private String reason;
    private IChatComponent message;
    private List<String> multilineMessage;
    private final GuiScreen parentScreen;
    private int field_175353_i;

    private GuiButton reconnectButton;
    private net.minecraft.client.gui.GuiSlider autoReconnectDelaySlider;
    private GuiButton forgeBypassButton;
    private int reconnectTimer;

    public GuiDisconnected(GuiScreen screen, String reasonLocalizationKey, IChatComponent chatComp) {
        this.parentScreen = screen;
        this.reason = I18n.format(reasonLocalizationKey, new Object[0]);
        this.message = chatComp;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    public void initGui() {
        this.buttonList.clear();
        this.multilineMessage = this.fontRendererObj.listFormattedStringToWidth(this.message.getFormattedText(), this.width - 50);
        this.field_175353_i = this.multilineMessage.size() * this.fontRendererObj.FONT_HEIGHT;

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT, I18n.format("gui.toMenu", new Object[0])));

        this.reconnectTimer = 0;
        this.buttonList.add(reconnectButton = new GuiButton(1, width / 2 - 100, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 22, 98, 20, "Reconnect"));

        drawReconnectDelaySlider();

        this.buttonList.add(new GuiButton(3, width / 2 - 100, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 44, 98, 20, GuiTheAltening.Companion.getApiKey().isEmpty() ? "Random alt" : "New TheAltening alt"));
        this.buttonList.add(new GuiButton(4, width / 2 + 2, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 44, 98, 20, "Random username"));
        this.buttonList.add(forgeBypassButton = new GuiButton(5, width / 2 - 100, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 66, "Bypass AntiForge: " + (ClientFixes.INSTANCE.getFmlFixesEnabled() ? "On" : "Off")));

        updateSliderText();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(this.parentScreen);
        }

        switch (button.id) {
            case 1:
                ServerUtils.INSTANCE.connectToLastServer();
                break;
            case 3:
                if (!GuiTheAltening.Companion.getApiKey().isEmpty()) {
                    final String apiKey = GuiTheAltening.Companion.getApiKey();
                    final TheAltening theAltening = new TheAltening(apiKey);

                    try {
                        final AccountData account = theAltening.getAccountData();
                        GuiAltManager.Companion.getAltService().switchService(AltService.EnumAltService.THEALTENING);

                        final YggdrasilUserAuthentication yggdrasilUserAuthentication = new YggdrasilUserAuthentication(new YggdrasilAuthenticationService(Proxy.NO_PROXY, ""), Agent.MINECRAFT);
                        yggdrasilUserAuthentication.setUsername(account.getToken());
                        yggdrasilUserAuthentication.setPassword("LiquidBounce");
                        yggdrasilUserAuthentication.logIn();

                        mc.session = new Session(yggdrasilUserAuthentication.getSelectedProfile().getName(), yggdrasilUserAuthentication.getSelectedProfile().getId().toString(), yggdrasilUserAuthentication.getAuthenticatedToken(), "microsoft");
                        EventManager.INSTANCE.call(SessionUpdateEvent.INSTANCE);
                        ServerUtils.INSTANCE.connectToLastServer();
                        break;
                    } catch (final Throwable throwable) {
                        ClientUtils.INSTANCE.getLOGGER().error("Failed to login into random account from TheAltening.", throwable);
                    }
                }

                final List<me.liuli.elixir.account.MinecraftAccount> accounts = FileManager.INSTANCE.getAccountsConfig().getAccounts();
                if (accounts.isEmpty()) break;
                final me.liuli.elixir.account.MinecraftAccount minecraftAccount = accounts.get(new Random().nextInt(accounts.size()));

                mc.displayGuiScreen(new GuiLoginProgress(minecraftAccount, () -> {
                    mc.addScheduledTask(() -> {
                        EventManager.INSTANCE.call(SessionUpdateEvent.INSTANCE);
                        ServerUtils.INSTANCE.connectToLastServer();
                    });
                    return null;
                }, e -> {
                    mc.addScheduledTask(() -> {
                        mc.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new GuiMainMenu()), e.getMessage(), new ChatComponentText(e.getMessage())));
                    });
                    return null;
                }, () -> null));
                break;
            case 4:
                RandomUtils.INSTANCE.randomAccount();
                ServerUtils.INSTANCE.connectToLastServer();
                break;
            case 5:
                ClientFixes.INSTANCE.setFmlFixesEnabled(!ClientFixes.INSTANCE.getFmlFixesEnabled());
                forgeBypassButton.displayString = "Bypass AntiForge: " + (ClientFixes.INSTANCE.getFmlFixesEnabled() ? "On" : "Off");
                try {
                    FileManager.INSTANCE.getValuesConfig().saveConfig();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    public void updateScreen() {
        if (AutoReconnect.INSTANCE.isEnabled()) {
            reconnectTimer++;
            if (reconnectTimer > AutoReconnect.INSTANCE.getDelay() / 50)
                ServerUtils.INSTANCE.connectToLastServer();
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.reason, this.width / 2, this.height / 2 - this.field_175353_i / 2 - this.fontRendererObj.FONT_HEIGHT * 2, 11184810);
        int i = this.height / 2 - this.field_175353_i / 2;

        if (this.multilineMessage != null) {
            for (String s : this.multilineMessage) {
                this.drawCenteredString(this.fontRendererObj, s, this.width / 2, i, 16777215);
                i += this.fontRendererObj.FONT_HEIGHT;
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (AutoReconnect.INSTANCE.isEnabled()) {
            updateReconnectButton();
        }
    }

    private void drawReconnectDelaySlider() {
        autoReconnectDelaySlider = new GuiSlider(
                new GuiPageButtonList.GuiResponder() {
                    @Override
                    public void onTick(int id, float value) {
                        AutoReconnect.INSTANCE.setDelay((int) value);
                        reconnectTimer = 0;
                        updateReconnectButton();
                        updateSliderText();
                    }
                    @Override public void func_175319_a(int p_175319_1_, String p_175319_2_) {}
                    @Override public void func_175321_a(int p_175321_1_, boolean p_175321_2_) {}
                },
                2,
                width / 2 + 2,
                height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 22,
                "AutoReconnect",
                (float) AutoReconnect.MIN,
                (float) AutoReconnect.MAX,
                (float) AutoReconnect.INSTANCE.getDelay(),
                new GuiSlider.FormatHelper() {
                    @Override
                    public String getText(int id, String name, float value) {
                        return name + ": " + (int) value + " ms";
                    }
                }
        );
        buttonList.add(autoReconnectDelaySlider);
    }

    private void updateSliderText() {
        if (autoReconnectDelaySlider == null) return;
        if (!AutoReconnect.INSTANCE.isEnabled()) {
            autoReconnectDelaySlider.displayString = "AutoReconnect: Off";
        } else {
            autoReconnectDelaySlider.displayString = "AutoReconnect: " + Math.floor(AutoReconnect.INSTANCE.getDelay() / 1000.0) + "s";
        }
    }

    private void updateReconnectButton() {
        if (reconnectButton != null)
            reconnectButton.displayString = "Reconnect" + (AutoReconnect.INSTANCE.isEnabled() ? " (" + (AutoReconnect.INSTANCE.getDelay() / 1000 - reconnectTimer / 20) + ")" : "");
    }
}