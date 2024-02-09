package org.teameugene.prison.enums;

public enum UpgradeType {
    TOOL(null),
    PICKAXE(TOOL),
    SWORD(TOOL),
    BOW(TOOL),
    ARMOR(null),
    CHESTPLATE(ARMOR),
    BOOTS(ARMOR),
    LEGGINGS(ARMOR),
    HELMET(ARMOR);

    private final UpgradeType upgradeGroup;

    UpgradeType(UpgradeType upgradeGroup) {
        this.upgradeGroup = upgradeGroup;
    }

    public static UpgradeType getUpgradeGroup(Upgrade upgrade) {
        switch (upgrade.getUpgradeType()) {
            case LEGGINGS -> {
                return ARMOR;
            }
            case CHESTPLATE -> {
                return ARMOR;
            }
            case HELMET -> {
                return ARMOR;
            }
            case BOOTS -> {
                return ARMOR;
            }
            case PICKAXE -> {
                return TOOL;
            }
            case SWORD -> {
                return TOOL;
            }
            case BOW -> {
                return TOOL;
            }
            case ARMOR -> {
                return ARMOR;
            }
            case TOOL -> {
                return TOOL;
            }
            default -> {
                return null;
            }
        }
    }
}
