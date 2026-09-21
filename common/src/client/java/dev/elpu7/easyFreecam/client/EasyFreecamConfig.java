package dev.elpu7.easyFreecam.client;

public final class EasyFreecamConfig {
    public static final double DEFAULT_HORIZONTAL_SPEED = 18.0D;
    public static final double DEFAULT_VERTICAL_SPEED = 18.0D;
    public static final double DEFAULT_SPRINT_MULTIPLIER = 4.0D;

    public boolean showHand = false;
    public boolean showPlayer = true;
    public boolean disableOnDamage = true;
    public boolean smoothCameraMovement = true;
    public boolean adjustSpeedWithMouseWheel = true;
    public boolean allowFood = true;
    public boolean allowDrinks = true;
    public boolean allowElytraRockets = true;
    public boolean allowInventoryActions = false;
    public double horizontalSpeed = DEFAULT_HORIZONTAL_SPEED;
    public double verticalSpeed = DEFAULT_VERTICAL_SPEED;
    public double sprintMultiplier = DEFAULT_SPRINT_MULTIPLIER;

    public EasyFreecamConfig copy() {
        EasyFreecamConfig copy = new EasyFreecamConfig();
        copy.copyFrom(this);
        return copy;
    }

    public void resetToDefaults() {
        copyFrom(new EasyFreecamConfig());
    }

    private void copyFrom(EasyFreecamConfig source) {
        showHand = source.showHand;
        showPlayer = source.showPlayer;
        disableOnDamage = source.disableOnDamage;
        smoothCameraMovement = source.smoothCameraMovement;
        adjustSpeedWithMouseWheel = source.adjustSpeedWithMouseWheel;
        allowFood = source.allowFood;
        allowDrinks = source.allowDrinks;
        allowElytraRockets = source.allowElytraRockets;
        allowInventoryActions = source.allowInventoryActions;
        horizontalSpeed = source.horizontalSpeed;
        verticalSpeed = source.verticalSpeed;
        sprintMultiplier = source.sprintMultiplier;
    }
}
