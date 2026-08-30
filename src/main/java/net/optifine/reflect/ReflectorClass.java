package net.optifine.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.optifine.Log;

public class ReflectorClass implements IResolvable
{
    private String targetClassName = null;
    private boolean checked = false;
    private Class targetClass = null;

    public ReflectorClass(String targetClassName)
    {
        this.targetClassName = targetClassName;
        ReflectorResolver.register(this);
    }

    public ReflectorClass(Class targetClass)
    {
        this.targetClass = targetClass;
        this.targetClassName = targetClass.getName();
        this.checked = true;
    }

    public Class getTargetClass()
    {
        if (this.checked)
        {
            return this.targetClass;
        }
        else
        {
            this.checked = true;

            try
            {
                this.targetClass = Class.forName(this.targetClassName);

                // MCP compatibility shims provide a handful of Forge API classes so the
                // ported client can compile, but they are not a real Forge runtime. Letting
                // OptiFine detect those shims enables Forge-only model/render paths (notably
                // ModelBakery's TRSR path) and disables vanilla face culling.
                try
                {
                    Field field = this.targetClass.getDeclaredField("LIQUIDBOUNCE_MCP_STUB");

                    if (Modifier.isStatic(field.getModifiers()) && field.getType() == Boolean.TYPE)
                    {
                        field.setAccessible(true);

                        if (field.getBoolean((Object)null))
                        {
                            this.targetClass = null;
                        }
                    }
                }
                catch (NoSuchFieldException ignored)
                {
                }
            }
            catch (ClassNotFoundException var2)
            {
                Log.log("(Reflector) Class not present: " + this.targetClassName);
            }
            catch (Throwable throwable)
            {
                throwable.printStackTrace();
            }

            return this.targetClass;
        }
    }

    public boolean exists()
    {
        return this.getTargetClass() != null;
    }

    public String getTargetClassName()
    {
        return this.targetClassName;
    }

    public boolean isInstance(Object obj)
    {
        return this.getTargetClass() == null ? false : this.getTargetClass().isInstance(obj);
    }

    public ReflectorField makeField(String name)
    {
        return new ReflectorField(this, name);
    }

    public ReflectorMethod makeMethod(String name)
    {
        return new ReflectorMethod(this, name);
    }

    public ReflectorMethod makeMethod(String name, Class[] paramTypes)
    {
        return new ReflectorMethod(this, name, paramTypes);
    }

    public void resolve()
    {
        Class oclass = this.getTargetClass();
    }
}
