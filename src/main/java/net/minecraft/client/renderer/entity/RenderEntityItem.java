package net.minecraft.client.renderer.entity;

import java.util.Random;

import net.ccbluex.liquidbounce.features.module.modules.render.Chams;
import net.ccbluex.liquidbounce.features.module.modules.render.ItemPhysics;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static net.minecraft.client.renderer.GlStateManager.*;
import static net.minecraft.util.MathHelper.sin;
import static org.lwjgl.opengl.GL11.*;

public class RenderEntityItem extends Render<EntityItem>
{
    private final RenderItem itemRenderer;
    private Random field_177079_e = new Random();

    public RenderEntityItem(RenderManager renderManagerIn, RenderItem p_i46167_2_)
    {
        super(renderManagerIn);
        this.itemRenderer = p_i46167_2_;
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    private int func_177077_a(EntityItem itemIn, double x, double y, double z, float p_177077_8_, IBakedModel ibakedmodel) {
        final ItemPhysics itemPhysics = ItemPhysics.INSTANCE;

        ItemStack itemStack = itemIn.getEntityItem();
        Item item = itemStack.getItem();

        if (item == null || itemStack == null) {
            return 0;
        }

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        boolean isGui3d = ibakedmodel.isGui3d();
        int count = getItemCount(itemStack);
        float yOffset = 0.25F;

        float age = (float) itemIn.getAge() + p_177077_8_;
        float hoverStart = itemIn.hoverStart;
        boolean isPhysicsState = itemPhysics.handleEvents();
        boolean isRealistic = itemPhysics.getRealistic();
        float weight = isPhysicsState ? itemPhysics.getWeight() : 0.0f;

        float sinValue = sin((age / 10.0F + hoverStart)) * 0.1F + 0.1F;
        if (isPhysicsState) {
            sinValue = 0.0f;
        }
        float scaleY = ibakedmodel.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;

        if (isPhysicsState) {
            translate((float)x, (float)y, (float)z);
        } else {
            translate((float) x, (float) y + sinValue + yOffset * scaleY, (float) z);
        }

        if (isGui3d) {
            translate(0, 0, -0.08);
        } else {
            translate(0, 0, -0.04);
        }

        if (isGui3d || this.renderManager.options != null) {
            float rotationYaw = (age / 20.0F + hoverStart) * (180F / (float) Math.PI);

            rotationYaw *= itemPhysics.getRotationSpeed() * (1.0F + Math.min(age / 360.0F, 1.0F));

            if (isPhysicsState) {
                if (itemIn.onGround) {
                    GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
                    if (!isRealistic) {
                        GL11.glRotatef(itemIn.rotationYaw, 0.0f, 0.0f, 1.0f);
                    } else {
                        GL11.glRotatef(itemIn.rotationYaw, 0.0f, 1.0f, 0.6f);
                    }
                } else {
                    for (int a = 0; a < 7; ++a) {
                        GL11.glRotatef(rotationYaw, weight, weight, 1.35f);
                    }
                }
            } else {
                rotate(rotationYaw, 0.0F, 1.0F, 0.0F);
            }
        }

        if (!isGui3d) {
            float offsetX = -0.0F * (float) (count - 1) * 0.5F;
            float offsetY = -0.0F * (float) (count - 1) * 0.5F;
            float offsetZ = -0.09375F * (float) (count - 1) * 0.5F;
            translate(offsetX, offsetY, offsetZ);
        }

        glDisable(GL_CULL_FACE);

        color(1.0F, 1.0F, 1.0F, 1.0F);
        return count;
    }

    private int getItemCount(ItemStack stack) {
        int size = stack.stackSize;

        if (size > 48) {
            return 5;
        } else if (size > 32) {
            return 4;
        } else if (size > 16) {
            return 3;
        } else if (size > 1) {
            return 2;
        }

        return 1;
    }

    private int func_177078_a(ItemStack stack)
    {
        int i = 1;

        if (stack.stackSize > 48)
        {
            i = 5;
        }
        else if (stack.stackSize > 32)
        {
            i = 4;
        }
        else if (stack.stackSize > 16)
        {
            i = 3;
        }
        else if (stack.stackSize > 1)
        {
            i = 2;
        }

        return i;
    }

    public void doRender(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        final Chams chams = Chams.INSTANCE;

        if (chams.handleEvents() && chams.getItems()) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(1f, -1000000F);
        }

        ItemStack itemstack = entity.getEntityItem();
        this.field_177079_e.setSeed(187L);
        boolean flag = false;

        if (this.bindEntityTexture(entity))
        {
            this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
            flag = true;
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();
        IBakedModel ibakedmodel = this.itemRenderer.getItemModelMesher().getItemModel(itemstack);
        int i = this.func_177077_a(entity, x, y, z, partialTicks, ibakedmodel);

        for (int j = 0; j < i; ++j)
        {
            if (ibakedmodel.isGui3d())
            {
                GlStateManager.pushMatrix();

                if (j > 0)
                {
                    float f = (this.field_177079_e.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f1 = (this.field_177079_e.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f2 = (this.field_177079_e.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    GlStateManager.translate(f, f1, f2);
                }

                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                ibakedmodel.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GROUND);
                this.itemRenderer.renderItem(itemstack, ibakedmodel);
                GlStateManager.popMatrix();
            }
            else
            {
                GlStateManager.pushMatrix();
                ibakedmodel.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GROUND);
                this.itemRenderer.renderItem(itemstack, ibakedmodel);
                GlStateManager.popMatrix();
                float f3 = ibakedmodel.getItemCameraTransforms().ground.scale.x;
                float f4 = ibakedmodel.getItemCameraTransforms().ground.scale.y;
                float f5 = ibakedmodel.getItemCameraTransforms().ground.scale.z;
                GlStateManager.translate(0.0F * f3, 0.0F * f4, 0.046875F * f5);
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        this.bindEntityTexture(entity);

        if (flag)
        {
            this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
        }

        if (chams.handleEvents() && chams.getItems()) {
            glPolygonOffset(1f, 1000000F);
            glDisable(GL_POLYGON_OFFSET_FILL);
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    public ResourceLocation getEntityTexture(EntityItem entity)
    {
        return TextureMap.locationBlocksTexture;
    }
}
