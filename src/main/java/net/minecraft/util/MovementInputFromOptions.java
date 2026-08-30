package net.minecraft.util;

import net.minecraft.client.settings.GameSettings;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.MovementInputEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.SuperKnockback;
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold;

public class MovementInputFromOptions extends MovementInput
{
    // Mixin Porter applied: net/ccbluex/liquidbounce/injection/forge/mixins/client/MixinMovementInputFromOptions.java
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState()
    {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.gameSettings.keyBindForward.isKeyDown())
        {
            ++this.moveForward;
        }

        if (this.gameSettings.keyBindBack.isKeyDown())
        {
            --this.moveForward;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown())
        {
            ++this.moveStrafe;
        }

        if (this.gameSettings.keyBindRight.isKeyDown())
        {
            --this.moveStrafe;
        }

        SuperKnockback module = SuperKnockback.INSTANCE;
        
                if (module.shouldBlockInput()) {
                    if (module.getOnlyMove()) {
                        this.moveForward = 0f;
        
                        if (!module.getOnlyMoveForward()) {
                            this.moveStrafe = 0f;
                        }
                    }
                }
        
                Scaffold.INSTANCE.handleMovementOptions(((MovementInput) (Object) this));
        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();

        EventManager.INSTANCE.call(new MovementInputEvent((MovementInput) (Object) this));
        if (this.sneak)
        {
            this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
            this.moveForward = (float)((double)this.moveForward * 0.3D);
        }
    }
}
