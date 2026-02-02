package net.minecraft.client.entity;

import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.exploit.AntiHunger;
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler;
import net.ccbluex.liquidbounce.features.module.modules.exploit.PortalMenu;
import net.ccbluex.liquidbounce.features.module.modules.fun.Derp;
import net.ccbluex.liquidbounce.features.module.modules.movement.InventoryMove;
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow;
import net.ccbluex.liquidbounce.features.module.modules.movement.Sneak;
import net.ccbluex.liquidbounce.features.module.modules.movement.Sprint;
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam;
import net.ccbluex.liquidbounce.features.module.modules.render.NoSwing;
import net.ccbluex.liquidbounce.utils.attack.CooldownHelper;
import net.ccbluex.liquidbounce.utils.extensions.MathExtensionsKt;
import net.ccbluex.liquidbounce.utils.extensions.PlayerExtensionKt;
import net.ccbluex.liquidbounce.utils.movement.MovementUtils;
import net.ccbluex.liquidbounce.utils.rotation.Rotation;
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings;
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

import java.util.List;

import static net.minecraft.network.play.client.C0BPacketEntityAction.Action.*;

public class EntityPlayerSP extends AbstractClientPlayer
{
    public final NetHandlerPlayClient sendQueue;
    private final StatFileWriter statWriter;
    private double lastReportedPosX;
    private double lastReportedPosY;
    private double lastReportedPosZ;
    private float lastReportedYaw;
    private float lastReportedPitch;
    public boolean serverSneakState;
    public boolean serverSprintState;
    private int positionUpdateTicks;
    private boolean hasValidHealth;
    private String clientBrand;
    public MovementInput movementInput;
    protected Minecraft mc;
    protected int sprintToggleTimer;
    public int sprintingTicksLeft;
    public float renderArmYaw;
    public float renderArmPitch;
    public float prevRenderArmYaw;
    public float prevRenderArmPitch;
    public int horseJumpPowerCounter;
    public float horseJumpPower;
    public float timeInPortal;
    public float prevTimeInPortal;

    public EntityPlayerSP(Minecraft mcIn, World worldIn, NetHandlerPlayClient netHandler, StatFileWriter statFile)
    {
        super(worldIn, netHandler.getGameProfile());
        this.sendQueue = netHandler;
        this.statWriter = statFile;
        this.mc = mcIn;
        this.dimension = 0;
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        return false;
    }

    public void heal(float healAmount)
    {
    }

    public void mountEntity(Entity entityIn)
    {
        super.mountEntity(entityIn);

        if (entityIn instanceof EntityMinecart)
        {
            this.mc.getSoundHandler().playSound(new MovingSoundMinecartRiding(this, (EntityMinecart)entityIn));
        }
    }

    public void onUpdate() {
        PlayerTickEvent tickEvent = new PlayerTickEvent(EventState.PRE);
        EventManager.INSTANCE.call(tickEvent);

        if (tickEvent.isCancelled()) {
            EventManager.INSTANCE.call(RotationUpdateEvent.INSTANCE);
            return;
        }

        if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0D, this.posZ))) {
            super.onUpdate();

            if (this.isRiding()) {
                this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(this.rotationYaw, this.rotationPitch, this.onGround));
                this.sendQueue.addToSendQueue(new C0CPacketInput(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneak));
            } else {
                this.onUpdateWalkingPlayer();
            }
        }

        EventManager.INSTANCE.call(new PlayerTickEvent(EventState.POST));
    }

    private void onUpdateWalkingPlayer() {
        MotionEvent motionEvent = new MotionEvent(
                posX,
                getEntityBoundingBox().minY,
                posZ,
                onGround,
                EventState.PRE
        );

        EventManager.INSTANCE.call(motionEvent);

        final InventoryMove inventoryMove = InventoryMove.INSTANCE;
        final Sneak sneak = Sneak.INSTANCE;
        final Derp derp = Derp.INSTANCE;

        final boolean fakeSprint = inventoryMove.handleEvents() && inventoryMove.getAacAdditionPro()
                || AntiHunger.INSTANCE.handleEvents()
                || sneak.handleEvents() && (!PlayerExtensionKt.isMoving(mc.thePlayer) || !sneak.getStopMove()) && sneak.getMode().equals("MineSecure")
                || Disabler.INSTANCE.handleEvents() && Disabler.INSTANCE.getStartSprint();

        boolean sprinting = isSprinting() && !fakeSprint;

        if (sprinting != serverSprintState) {
            if (sprinting)
                sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, START_SPRINTING));
            else sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, STOP_SPRINTING));

            serverSprintState = sprinting;
        }

        boolean sneaking = isSneaking();

        if (sneaking != serverSneakState && (!sneak.handleEvents() || sneak.getMode().equals("Legit"))) {
            if (sneaking)
                sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, START_SNEAKING));
            else sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, STOP_SNEAKING));

            serverSneakState = sneaking;
        }

        final MovementUtils movementUtils = MovementUtils.INSTANCE;

        if (motionEvent.getOnGround()) {
            movementUtils.setGroundTicks(movementUtils.getGroundTicks() + 1);
            movementUtils.setAirTicks(0);
        } else {
            movementUtils.setGroundTicks(0);
            movementUtils.setAirTicks(movementUtils.getAirTicks() + 1);
        }

        if (isCurrentViewEntity()) {
            float yaw = rotationYaw;
            float pitch = rotationPitch;

            final Rotation currentRotation = RotationUtils.INSTANCE.getCurrentRotation();

            if (derp.handleEvents()) {
                Rotation rot = derp.getRotation();
                yaw = rot.getYaw();
                pitch = rot.getPitch();
            }

            if (currentRotation != null) {
                yaw = currentRotation.getYaw();
                pitch = currentRotation.getPitch();
            }

            double xDiff = motionEvent.getX() - lastReportedPosX;
            double yDiff = motionEvent.getY() - lastReportedPosY;
            double zDiff = motionEvent.getZ() - lastReportedPosZ;
            double yawDiff = yaw - this.lastReportedYaw;
            double pitchDiff = pitch - this.lastReportedPitch;
            boolean moved = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 9.0E-4 || positionUpdateTicks >= 20;
            boolean rotated = !FreeCam.INSTANCE.shouldDisableRotations() && (yawDiff != 0 || pitchDiff != 0);

            if (ridingEntity == null) {
                if (moved && rotated) {
                    sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(motionEvent.getX(), motionEvent.getY(), motionEvent.getZ(), yaw, pitch, motionEvent.getOnGround()));
                } else if (moved) {
                    sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(motionEvent.getX(), motionEvent.getY(), motionEvent.getZ(), motionEvent.getOnGround()));
                } else if (rotated) {
                    sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, motionEvent.getOnGround()));
                } else {
                    sendQueue.addToSendQueue(new C03PacketPlayer(motionEvent.getOnGround()));
                }
            } else {
                sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(motionX, -999, motionZ, yaw, pitch, motionEvent.getOnGround()));
                moved = false;
            }

            ++positionUpdateTicks;

            if (moved) {
                lastReportedPosX = motionEvent.getX();
                lastReportedPosY = motionEvent.getY();
                lastReportedPosZ = motionEvent.getZ();
                positionUpdateTicks = 0;
            }

            if (!FreeCam.INSTANCE.shouldDisableRotations()) {
                RotationUtils.INSTANCE.setServerRotation(new Rotation(yaw, pitch));
            }

            if (rotated) {
                this.lastReportedYaw = yaw;
                this.lastReportedPitch = pitch;
            }
        }

        EventManager.INSTANCE.call(new MotionEvent(posX, getEntityBoundingBox().minY, posZ, onGround, EventState.POST));

        EventManager.INSTANCE.call(RotationUpdateEvent.INSTANCE);
    }

    public EntityItem dropOneItem(boolean dropAll)
    {
        C07PacketPlayerDigging.Action c07packetplayerdigging$action = dropAll ? C07PacketPlayerDigging.Action.DROP_ALL_ITEMS : C07PacketPlayerDigging.Action.DROP_ITEM;
        this.sendQueue.addToSendQueue(new C07PacketPlayerDigging(c07packetplayerdigging$action, BlockPos.ORIGIN, EnumFacing.DOWN));
        return null;
    }

    protected void joinEntityItemWithWorld(EntityItem itemIn)
    {
    }

    public void sendChatMessage(String message) {
        if (Disabler.INSTANCE.handleEvents() && Disabler.INSTANCE.getSpigotSpam()) {
            message = Disabler.INSTANCE.getMessage() + " " + message;
        }
        this.sendQueue.addToSendQueue(new C01PacketChatMessage(message));
    }

    public void swingItem() {
        if (NoSwing.INSTANCE.handleEvents()) {
            if (!NoSwing.INSTANCE.getServerSide()) {
                sendQueue.addToSendQueue(new C0APacketAnimation());
                CooldownHelper.INSTANCE.resetLastAttackedTicks();
            }
        } else {
            super.swingItem();
            this.sendQueue.addToSendQueue(new C0APacketAnimation());
            CooldownHelper.INSTANCE.resetLastAttackedTicks();
        }
    }

    public void respawnPlayer()
    {
        this.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
    }

    protected void damageEntity(DamageSource damageSrc, float damageAmount)
    {
        if (!this.isEntityInvulnerable(damageSrc))
        {
            this.setHealth(this.getHealth() - damageAmount);
        }
    }

    public void closeScreen()
    {
        this.sendQueue.addToSendQueue(new C0DPacketCloseWindow(this.openContainer.windowId));
        this.closeScreenAndDropStack();
    }

    public void closeScreenAndDropStack()
    {
        this.inventory.setItemStack((ItemStack)null);
        super.closeScreen();
        this.mc.displayGuiScreen((GuiScreen)null);
    }

    public void setPlayerSPHealth(float health)
    {
        if (this.hasValidHealth)
        {
            float f = this.getHealth() - health;

            if (f <= 0.0F)
            {
                this.setHealth(health);

                if (f < 0.0F)
                {
                    this.hurtResistantTime = this.maxHurtResistantTime / 2;
                }
            }
            else
            {
                this.lastDamage = f;
                this.setHealth(this.getHealth());
                this.hurtResistantTime = this.maxHurtResistantTime;
                this.damageEntity(DamageSource.generic, f);
                this.hurtTime = this.maxHurtTime = 10;
            }
        }
        else
        {
            this.setHealth(health);
            this.hasValidHealth = true;
        }
    }

    public void addStat(StatBase stat, int amount)
    {
        if (stat != null)
        {
            if (stat.isIndependent)
            {
                super.addStat(stat, amount);
            }
        }
    }

    public void sendPlayerAbilities()
    {
        this.sendQueue.addToSendQueue(new C13PacketPlayerAbilities(this.capabilities));
    }

    public boolean isUser()
    {
        return true;
    }

    protected void sendHorseJump()
    {
        this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.RIDING_JUMP, (int)(this.getHorseJumpPower() * 100.0F)));
    }

    public void sendHorseInventory()
    {
        this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.OPEN_INVENTORY));
    }

    public void setClientBrand(String brand)
    {
        this.clientBrand = brand;
    }

    public String getClientBrand()
    {
        return this.clientBrand;
    }

    public StatFileWriter getStatFileWriter()
    {
        return this.statWriter;
    }

    public void addChatComponentMessage(IChatComponent chatComponent)
    {
        this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
    }

    protected boolean pushOutOfBlocks(double x, double y, double z)
    {
        BlockPushEvent event = new BlockPushEvent();
        if (this.noClip) event.cancelEvent();
        EventManager.INSTANCE.call(event);
        if (event.isCancelled()) return false;

        if (this.noClip)
        {
            return false;
        }
        else
        {
            BlockPos blockpos = new BlockPos(x, y, z);
            double d0 = x - (double)blockpos.getX();
            double d1 = z - (double)blockpos.getZ();

            if (!this.isOpenBlockSpace(blockpos))
            {
                int i = -1;
                double d2 = 9999.0D;

                if (this.isOpenBlockSpace(blockpos.west()) && d0 < d2)
                {
                    d2 = d0;
                    i = 0;
                }

                if (this.isOpenBlockSpace(blockpos.east()) && 1.0D - d0 < d2)
                {
                    d2 = 1.0D - d0;
                    i = 1;
                }

                if (this.isOpenBlockSpace(blockpos.north()) && d1 < d2)
                {
                    d2 = d1;
                    i = 4;
                }

                if (this.isOpenBlockSpace(blockpos.south()) && 1.0D - d1 < d2)
                {
                    d2 = 1.0D - d1;
                    i = 5;
                }

                float f = 0.1F;

                if (i == 0)
                {
                    this.motionX = (double)(-f);
                }

                if (i == 1)
                {
                    this.motionX = (double)f;
                }

                if (i == 4)
                {
                    this.motionZ = (double)(-f);
                }

                if (i == 5)
                {
                    this.motionZ = (double)f;
                }
            }

            return false;
        }
    }

    private boolean isOpenBlockSpace(BlockPos pos)
    {
        return !this.worldObj.getBlockState(pos).getBlock().isNormalCube() && !this.worldObj.getBlockState(pos.up()).getBlock().isNormalCube();
    }

    public void setSprinting(boolean sprinting)
    {
        super.setSprinting(sprinting);
        this.sprintingTicksLeft = sprinting ? 600 : 0;
    }

    public void setXPStats(float currentXP, int maxXP, int level)
    {
        this.experience = currentXP;
        this.experienceTotal = maxXP;
        this.experienceLevel = level;
    }

    public void addChatMessage(IChatComponent component)
    {
        this.mc.ingameGUI.getChatGUI().printChatMessage(component);
    }

    public boolean canCommandSenderUseCommand(int permLevel, String commandName)
    {
        return permLevel <= 0;
    }

    public BlockPos getPosition()
    {
        return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
    }

    public void playSound(String name, float volume, float pitch)
    {
        this.worldObj.playSound(this.posX, this.posY, this.posZ, name, volume, pitch, false);
    }

    public boolean isServerWorld()
    {
        return true;
    }

    public boolean isRidingHorse()
    {
        return this.ridingEntity != null && this.ridingEntity instanceof EntityHorse && ((EntityHorse)this.ridingEntity).isHorseSaddled();
    }

    public float getHorseJumpPower()
    {
        return this.horseJumpPower;
    }

    public void openEditSign(TileEntitySign signTile)
    {
        this.mc.displayGuiScreen(new GuiEditSign(signTile));
    }

    public void openEditCommandBlock(CommandBlockLogic cmdBlockLogic)
    {
        this.mc.displayGuiScreen(new GuiCommandBlock(cmdBlockLogic));
    }

    public void displayGUIBook(ItemStack bookStack)
    {
        Item item = bookStack.getItem();

        if (item == Items.writable_book)
        {
            this.mc.displayGuiScreen(new GuiScreenBook(this, bookStack, true));
        }
    }

    public void displayGUIChest(IInventory chestInventory)
    {
        String s = chestInventory instanceof IInteractionObject ? ((IInteractionObject)chestInventory).getGuiID() : "minecraft:container";

        if ("minecraft:chest".equals(s))
        {
            this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
        }
        else if ("minecraft:hopper".equals(s))
        {
            this.mc.displayGuiScreen(new GuiHopper(this.inventory, chestInventory));
        }
        else if ("minecraft:furnace".equals(s))
        {
            this.mc.displayGuiScreen(new GuiFurnace(this.inventory, chestInventory));
        }
        else if ("minecraft:brewing_stand".equals(s))
        {
            this.mc.displayGuiScreen(new GuiBrewingStand(this.inventory, chestInventory));
        }
        else if ("minecraft:beacon".equals(s))
        {
            this.mc.displayGuiScreen(new GuiBeacon(this.inventory, chestInventory));
        }
        else if (!"minecraft:dispenser".equals(s) && !"minecraft:dropper".equals(s))
        {
            this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
        }
        else
        {
            this.mc.displayGuiScreen(new GuiDispenser(this.inventory, chestInventory));
        }
    }

    public void displayGUIHorse(EntityHorse horse, IInventory horseInventory)
    {
        this.mc.displayGuiScreen(new GuiScreenHorseInventory(this.inventory, horseInventory, horse));
    }

    public void displayGui(IInteractionObject guiOwner)
    {
        String s = guiOwner.getGuiID();

        if ("minecraft:crafting_table".equals(s))
        {
            this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.worldObj));
        }
        else if ("minecraft:enchanting_table".equals(s))
        {
            this.mc.displayGuiScreen(new GuiEnchantment(this.inventory, this.worldObj, guiOwner));
        }
        else if ("minecraft:anvil".equals(s))
        {
            this.mc.displayGuiScreen(new GuiRepair(this.inventory, this.worldObj));
        }
    }

    public void displayVillagerTradeGui(IMerchant villager)
    {
        this.mc.displayGuiScreen(new GuiMerchant(this.inventory, villager, this.worldObj));
    }

    public void onCriticalHit(Entity entityHit)
    {
        this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT);
    }

    public void onEnchantmentCritical(Entity entityHit)
    {
        this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT_MAGIC);
    }

    public boolean isSneaking()
    {
        boolean flag = this.movementInput != null ? this.movementInput.sneak : false;
        return flag && !this.sleeping;
    }

    public void updateEntityActionState()
    {
        super.updateEntityActionState();

        if (this.isCurrentViewEntity())
        {
            this.moveStrafing = this.movementInput.moveStrafe;
            this.moveForward = this.movementInput.moveForward;
            this.isJumping = this.movementInput.jump;
            this.prevRenderArmYaw = this.renderArmYaw;
            this.prevRenderArmPitch = this.renderArmPitch;
            this.renderArmPitch = (float)((double)this.renderArmPitch + (double)(this.rotationPitch - this.renderArmPitch) * 0.5D);
            this.renderArmYaw = (float)((double)this.renderArmYaw + (double)(this.rotationYaw - this.renderArmYaw) * 0.5D);
        }
    }

    protected boolean isCurrentViewEntity()
    {
        return this.mc.getRenderViewEntity() == this;
    }

    public void onLivingUpdate() {
        EventManager.INSTANCE.call(UpdateEvent.INSTANCE);

        if (sprintingTicksLeft > 0) {
            --sprintingTicksLeft;

            if (sprintingTicksLeft == 0) {
                setSprinting(false);
            }
        }

        if (sprintToggleTimer > 0) {
            --sprintToggleTimer;
        }

        prevTimeInPortal = timeInPortal;

        if (inPortal) {
            if (mc.currentScreen != null && !mc.currentScreen.doesGuiPauseGame() && !PortalMenu.INSTANCE.handleEvents()) {
                mc.displayGuiScreen(null);
            }

            if (timeInPortal == 0f) {
                mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), rand.nextFloat() * 0.4F + 0.8F));
            }

            timeInPortal += 0.0125F;

            if (timeInPortal >= 1f) {
                timeInPortal = 1f;
            }

            inPortal = false;
        } else if (isPotionActive(Potion.confusion) && getActivePotionEffect(Potion.confusion).getDuration() > 60) {
            timeInPortal += 0.006666667F;

            if (timeInPortal > 1f) {
                timeInPortal = 1f;
            }
        } else {
            if (timeInPortal > 0f) {
                timeInPortal -= 0.05F;
            }

            if (timeInPortal < 0f) {
                timeInPortal = 0f;
            }
        }

        if (timeUntilPortal > 0) {
            --timeUntilPortal;
        }

        boolean flag = movementInput.jump;
        boolean flag1 = movementInput.sneak;
        float f = 0.8F;
        boolean flag2 = movementInput.moveForward >= f;
        movementInput.updatePlayerMoveState();

        RotationUtils utils = RotationUtils.INSTANCE;

        final Rotation currentRotation = utils.getCurrentRotation();
        MovementInput modifiedInput = new MovementInput();

        modifiedInput.moveForward = movementInput.moveForward;
        modifiedInput.moveStrafe = movementInput.moveStrafe;

        if (movementInput.sneak) {
            modifiedInput.moveStrafe /= 0.3f;
            modifiedInput.moveForward /= 0.3f;
        }

        float moveForward = currentRotation != null ? Math.round(modifiedInput.moveForward * MathHelper.cos(MathExtensionsKt.toRadians(rotationYaw - currentRotation.getYaw())) + modifiedInput.moveStrafe * MathHelper.sin(MathExtensionsKt.toRadians(rotationYaw - currentRotation.getYaw()))) : modifiedInput.moveForward;
        float moveStrafe = currentRotation != null ? Math.round(modifiedInput.moveStrafe * MathHelper.cos(MathExtensionsKt.toRadians(rotationYaw - currentRotation.getYaw())) - modifiedInput.moveForward * MathHelper.sin(MathExtensionsKt.toRadians(rotationYaw - currentRotation.getYaw()))) : modifiedInput.moveStrafe;

        modifiedInput.moveForward = moveForward;
        modifiedInput.moveStrafe = moveStrafe;

        if (movementInput.sneak) {
            final SneakSlowDownEvent sneakSlowDownEvent = new SneakSlowDownEvent(movementInput.moveStrafe, movementInput.moveForward);
            EventManager.INSTANCE.call(sneakSlowDownEvent);
            movementInput.moveStrafe = sneakSlowDownEvent.getStrafe();
            movementInput.moveForward = sneakSlowDownEvent.getForward();
            modifiedInput.moveForward *= 0.3f;
            modifiedInput.moveStrafe *= 0.3f;
            final SneakSlowDownEvent secondSneakSlowDownEvent = new SneakSlowDownEvent(modifiedInput.moveStrafe, modifiedInput.moveForward);
            EventManager.INSTANCE.call(secondSneakSlowDownEvent);
            modifiedInput.moveStrafe = secondSneakSlowDownEvent.getStrafe();
            modifiedInput.moveForward = secondSneakSlowDownEvent.getForward();
        }

        final NoSlow noSlow = NoSlow.INSTANCE;
        final KillAura killAura = KillAura.INSTANCE;

        boolean isUsingItem = getHeldItem() != null && (isUsingItem() || (getHeldItem().getItem() instanceof ItemSword && killAura.getBlockStatus()) || NoSlow.INSTANCE.isUNCPBlocking());

        if (isUsingItem && !isRiding()) {
            final SlowDownEvent slowDownEvent = new SlowDownEvent(0.2F, 0.2F);
            EventManager.INSTANCE.call(slowDownEvent);
            movementInput.moveStrafe *= slowDownEvent.getStrafe();
            movementInput.moveForward *= slowDownEvent.getForward();
            sprintToggleTimer = 0;
            modifiedInput.moveStrafe *= slowDownEvent.getStrafe();
            modifiedInput.moveForward *= slowDownEvent.getForward();
        }

        RotationSettings settings = utils.getActiveSettings();

        utils.setModifiedInput(settings != null && !settings.getStrict() ? modifiedInput : movementInput);

        pushOutOfBlocks(posX - width * 0.35, getEntityBoundingBox().minY + 0.5, posZ + width * 0.35);
        pushOutOfBlocks(posX - width * 0.35, getEntityBoundingBox().minY + 0.5, posZ - width * 0.35);
        pushOutOfBlocks(posX + width * 0.35, getEntityBoundingBox().minY + 0.5, posZ - width * 0.35);
        pushOutOfBlocks(posX + width * 0.35, getEntityBoundingBox().minY + 0.5, posZ + width * 0.35);

        final Sprint sprint = Sprint.INSTANCE;

        boolean flag3 = (float) getFoodStats().getFoodLevel() > 6F || capabilities.allowFlying;
        if (onGround && !flag1 && !flag2 && movementInput.moveForward >= f && !isSprinting() && flag3 && !isUsingItem() && !isPotionActive(Potion.blindness)) {
            if (sprintToggleTimer <= 0 && !mc.gameSettings.keyBindSprint.isKeyDown()) {
                sprintToggleTimer = 7;
            } else {
                setSprinting(true);
            }
        }

        if (!isSprinting() && movementInput.moveForward >= f && flag3 && (noSlow.handleEvents() || !isUsingItem()) && !isPotionActive(Potion.blindness) && mc.gameSettings.keyBindSprint.isKeyDown()) {
            setSprinting(true);
        }

        if (isSprinting() && (movementInput.moveForward < f || isCollidedHorizontally || !flag3)) {
            setSprinting(false);
        }

        EventManager.INSTANCE.call(PostSprintUpdateEvent.INSTANCE);

        sprint.correctSprintState(modifiedInput, isUsingItem);

        if (capabilities.allowFlying) {
            if (mc.playerController.isSpectatorMode()) {
                if (!capabilities.isFlying) {
                    capabilities.isFlying = true;
                    sendPlayerAbilities();
                }
            } else if (!flag && movementInput.jump) {
                if (flyToggleTimer == 0) {
                    flyToggleTimer = 7;
                } else {
                    capabilities.isFlying = !capabilities.isFlying;
                    sendPlayerAbilities();
                    flyToggleTimer = 0;
                }
            }
        }

        if (capabilities.isFlying && isCurrentViewEntity()) {
            if (movementInput.sneak) {
                motionY -= capabilities.getFlySpeed() * 3f;
            }

            if (movementInput.jump) {
                motionY += capabilities.getFlySpeed() * 3f;
            }
        }

        if (isRidingHorse()) {
            if (horseJumpPowerCounter < 0) {
                ++horseJumpPowerCounter;

                if (horseJumpPowerCounter == 0) {
                    horseJumpPower = 0f;
                }
            }

            if (flag && !movementInput.jump) {
                horseJumpPowerCounter = -10;
                sendHorseJump();
            } else if (!flag && movementInput.jump) {
                horseJumpPowerCounter = 0;
                horseJumpPower = 0f;
            } else if (flag) {
                ++horseJumpPowerCounter;

                if (horseJumpPowerCounter < 10) {
                    horseJumpPower = (float) horseJumpPowerCounter * 0.1F;
                } else {
                    horseJumpPower = 0.8F + 2f / (float) (horseJumpPowerCounter - 9) * 0.1F;
                }
            }
        } else {
            horseJumpPower = 0f;
        }

        super.onLivingUpdate();

        if (onGround && capabilities.isFlying && !mc.playerController.isSpectatorMode()) {
            capabilities.isFlying = false;
            sendPlayerAbilities();
        }
    }

    @Override
    public void moveEntity(double x, double y, double z) {
        MoveEvent moveEvent = new MoveEvent(x, y, z);
        EventManager.INSTANCE.call(moveEvent);

        if (moveEvent.isCancelled()) return;

        x = moveEvent.getX();
        y = moveEvent.getY();
        z = moveEvent.getZ();

        if (noClip) {
            setEntityBoundingBox(getEntityBoundingBox().offset(x, y, z));
            posX = (getEntityBoundingBox().minX + getEntityBoundingBox().maxX) / 2;
            posY = getEntityBoundingBox().minY;
            posZ = (getEntityBoundingBox().minZ + getEntityBoundingBox().maxZ) / 2;
        } else {
            worldObj.theProfiler.startSection("move");
            double d0 = posX;
            double d1 = posY;
            double d2 = posZ;

            if (isInWeb) {
                isInWeb = false;
                x *= 0.25;
                y *= 0.05000000074505806;
                z *= 0.25;
                motionX = 0;
                motionY = 0;
                motionZ = 0;
            }

            double d3 = x;
            double d4 = y;
            double d5 = z;
            boolean flag = onGround && isSneaking();

            if (flag || moveEvent.isSafeWalk()) {
                double d6;

                for (d6 = 0.05; x != 0 && worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().offset(x, -1, 0)).isEmpty(); d3 = x) {
                    if (x < d6 && x >= -d6) {
                        x = 0;
                    } else if (x > 0) {
                        x -= d6;
                    } else {
                        x += d6;
                    }
                }

                for (; z != 0 && worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().offset(0, -1, z)).isEmpty(); d5 = z) {
                    if (z < d6 && z >= -d6) {
                        z = 0;
                    } else if (z > 0) {
                        z -= d6;
                    } else {
                        z += d6;
                    }
                }

                for (; x != 0 && z != 0 && worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().offset(x, -1, z)).isEmpty(); d5 = z) {
                    if (x < d6 && x >= -d6) {
                        x = 0;
                    } else if (x > 0) {
                        x -= d6;
                    } else {
                        x += d6;
                    }

                    d3 = x;

                    if (z < d6 && z >= -d6) {
                        z = 0;
                    } else if (z > 0) {
                        z -= d6;
                    } else {
                        z += d6;
                    }
                }
            }

            List<AxisAlignedBB> list1 = worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().addCoord(x, y, z));
            AxisAlignedBB axisalignedbb = getEntityBoundingBox();

            for (AxisAlignedBB axisalignedbb1 : list1) {
                y = axisalignedbb1.calculateYOffset(getEntityBoundingBox(), y);
            }

            setEntityBoundingBox(getEntityBoundingBox().offset(0, y, 0));
            boolean flag1 = onGround || d4 != y && d4 < 0;

            for (AxisAlignedBB axisalignedbb2 : list1) {
                x = axisalignedbb2.calculateXOffset(getEntityBoundingBox(), x);
            }

            setEntityBoundingBox(getEntityBoundingBox().offset(x, 0, 0));

            for (AxisAlignedBB axisalignedbb13 : list1) {
                z = axisalignedbb13.calculateZOffset(getEntityBoundingBox(), z);
            }

            setEntityBoundingBox(getEntityBoundingBox().offset(0, 0, z));

            if (stepHeight > 0f && flag1 && (d3 != x || d5 != z)) {
                StepEvent stepEvent = new StepEvent(stepHeight);
                EventManager.INSTANCE.call(stepEvent);
                double d11 = x;
                double d7 = y;
                double d8 = z;
                AxisAlignedBB axisalignedbb3 = getEntityBoundingBox();
                setEntityBoundingBox(axisalignedbb);
                y = stepEvent.getStepHeight();
                List<AxisAlignedBB> list = worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().addCoord(d3, y, d5));
                AxisAlignedBB axisalignedbb4 = getEntityBoundingBox();
                AxisAlignedBB axisalignedbb5 = axisalignedbb4.addCoord(d3, 0, d5);
                double d9 = y;

                for (AxisAlignedBB axisalignedbb6 : list) {
                    d9 = axisalignedbb6.calculateYOffset(axisalignedbb5, d9);
                }

                axisalignedbb4 = axisalignedbb4.offset(0, d9, 0);
                double d15 = d3;

                for (AxisAlignedBB axisalignedbb7 : list) {
                    d15 = axisalignedbb7.calculateXOffset(axisalignedbb4, d15);
                }

                axisalignedbb4 = axisalignedbb4.offset(d15, 0, 0);
                double d16 = d5;

                for (AxisAlignedBB axisalignedbb8 : list) {
                    d16 = axisalignedbb8.calculateZOffset(axisalignedbb4, d16);
                }

                axisalignedbb4 = axisalignedbb4.offset(0, 0, d16);
                AxisAlignedBB axisalignedbb14 = getEntityBoundingBox();
                double d17 = y;

                for (AxisAlignedBB axisalignedbb9 : list) {
                    d17 = axisalignedbb9.calculateYOffset(axisalignedbb14, d17);
                }

                axisalignedbb14 = axisalignedbb14.offset(0, d17, 0);
                double d18 = d3;

                for (AxisAlignedBB axisalignedbb10 : list) {
                    d18 = axisalignedbb10.calculateXOffset(axisalignedbb14, d18);
                }

                axisalignedbb14 = axisalignedbb14.offset(d18, 0, 0);
                double d19 = d5;

                for (AxisAlignedBB axisalignedbb11 : list) {
                    d19 = axisalignedbb11.calculateZOffset(axisalignedbb14, d19);
                }

                axisalignedbb14 = axisalignedbb14.offset(0, 0, d19);
                double d20 = d15 * d15 + d16 * d16;
                double d10 = d18 * d18 + d19 * d19;

                if (d20 > d10) {
                    x = d15;
                    z = d16;
                    y = -d9;
                    setEntityBoundingBox(axisalignedbb4);
                } else {
                    x = d18;
                    z = d19;
                    y = -d17;
                    setEntityBoundingBox(axisalignedbb14);
                }

                for (AxisAlignedBB axisalignedbb12 : list) {
                    y = axisalignedbb12.calculateYOffset(getEntityBoundingBox(), y);
                }

                setEntityBoundingBox(getEntityBoundingBox().offset(0, y, 0));

                if (d11 * d11 + d8 * d8 >= x * x + z * z) {
                    x = d11;
                    y = d7;
                    z = d8;
                    setEntityBoundingBox(axisalignedbb3);
                } else {
                    EventManager.INSTANCE.call(StepConfirmEvent.INSTANCE);
                }
            }

            worldObj.theProfiler.endSection();
            worldObj.theProfiler.startSection("rest");
            posX = (getEntityBoundingBox().minX + getEntityBoundingBox().maxX) / 2;
            posY = getEntityBoundingBox().minY;
            posZ = (getEntityBoundingBox().minZ + getEntityBoundingBox().maxZ) / 2;
            isCollidedHorizontally = d3 != x || d5 != z;
            isCollidedVertically = d4 != y;
            onGround = isCollidedVertically && d4 < 0;
            isCollided = isCollidedHorizontally || isCollidedVertically;
            int i = MathHelper.floor_double(posX);
            int j = MathHelper.floor_double(posY - 0.20000000298023224);
            int k = MathHelper.floor_double(posZ);
            BlockPos blockpos = new BlockPos(i, j, k);
            Block block1 = worldObj.getBlockState(blockpos).getBlock();

            if (block1.getMaterial() == Material.air) {
                Block block = worldObj.getBlockState(blockpos.down()).getBlock();

                if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate) {
                    block1 = block;
                    blockpos = blockpos.down();
                }
            }

            updateFallState(y, onGround, block1, blockpos);

            if (d3 != x) {
                motionX = 0;
            }

            if (d5 != z) {
                motionZ = 0;
            }

            if (d4 != y) {
                block1.onLanded(worldObj, (Entity) (Object) this);
            }

            if (canTriggerWalking() && !flag && ridingEntity == null) {
                double d12 = posX - d0;
                double d13 = posY - d1;
                double d14 = posZ - d2;

                if (block1 != Blocks.ladder) {
                    d13 = 0;
                }

                if (onGround) {
                    block1.onEntityCollidedWithBlock(worldObj, blockpos, (Entity) (Object) this);
                }

                distanceWalkedModified = (float) (distanceWalkedModified + MathHelper.sqrt_double(d12 * d12 + d14 * d14) * 0.6);
                distanceWalkedOnStepModified = (float) (distanceWalkedOnStepModified + MathHelper.sqrt_double(d12 * d12 + d13 * d13 + d14 * d14) * 0.6);

                if (distanceWalkedOnStepModified > (float) getNextStepDistance() && block1.getMaterial() != Material.air) {
                    setNextStepDistance((int) distanceWalkedOnStepModified + 1);

                    if (isInWater()) {
                        float f = MathHelper.sqrt_double(motionX * motionX * 0.20000000298023224 + motionY * motionY + motionZ * motionZ * 0.20000000298023224) * 0.35F;

                        if (f > 1f) {
                            f = 1f;
                        }

                        playSound(getSwimSound(), f, 1f + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                    }

                    playStepSound(blockpos, block1);
                }
            }

            try {
                doBlockCollisions();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
                addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            boolean flag2 = isWet();

            if (worldObj.isFlammableWithin(getEntityBoundingBox().contract(0.001, 0.001, 0.001))) {
                dealFireDamage(1);

                if (!flag2) {
                    setFire(getFire() + 1);

                    if (getFire() == 0) {
                        setFire(8);
                    }
                }
            } else if (getFire() <= 0) {
                setFire(-fireResistance);
            }

            if (flag2 && getFire() > 0) {
                playSound("random.fizz", 0.7F, 1.6F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                setFire(-fireResistance);
            }

            worldObj.theProfiler.endSection();
            }
        }
}
