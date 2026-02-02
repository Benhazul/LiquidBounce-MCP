package net.minecraftforge.client.model;

import javax.vecmath.Matrix4fLOL;
import net.minecraft.util.EnumFacing;

public interface ITransformation
{
    Matrix4fLOL getMatrix();

    EnumFacing rotate(EnumFacing var1);

    int rotate(EnumFacing var1, int var2);
}
