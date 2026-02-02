package net.minecraft.util;

import net.minecraft.client.settings.GameSettings;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.MovementInputEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.SuperKnockback;
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold;

public class MovementInputFromOptions extends MovementInput
{
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

        SuperKnockback superKnockback = SuperKnockback.INSTANCE;

        if (superKnockback.shouldBlockInput())
        {
            if (superKnockback.getOnlyMove())
            {
                this.moveForward = 0f;

                if (!superKnockback.getOnlyMoveForward())
                {
                    this.moveStrafe = 0f;
                }
            }
        }

        Scaffold.INSTANCE.handleMovementOptions(this);

        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();

        EventManager.INSTANCE.call(new MovementInputEvent(this));

        if (this.sneak)
        {
            this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
            this.moveForward = (float)((double)this.moveForward * 0.3D);
        }
    }
}