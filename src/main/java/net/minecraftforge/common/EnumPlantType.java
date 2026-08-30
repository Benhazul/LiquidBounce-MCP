package net.minecraftforge.common;

public enum EnumPlantType {
    Plains,
    Desert,
    Beach,
    Cave,
    Water,
    Nether,
    Crop;

    public static EnumPlantType getPlantType(String name) {
        for (EnumPlantType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return Plains;
    }
}
