package dev.elpu7.easyFreecam.client;

public final class EasyFreecamConfig {
    public static final double DEFAULT_HORIZONTAL_SPEED = 18.0D;
    public static final double DEFAULT_VERTICAL_SPEED = 18.0D;
    public static final double DEFAULT_SPRINT_MULTIPLIER = 4.0D;

    public boolean showHand = false;
    public boolean showPlayer = true;
    public boolean disableOnDamage = true;
    public double horizontalSpeed = DEFAULT_HORIZONTAL_SPEED;
    public double verticalSpeed = DEFAULT_VERTICAL_SPEED;
    public double sprintMultiplier = DEFAULT_SPRINT_MULTIPLIER;

    public EasyFreecamConfig copy() {
        EasyFreecamConfig copy = new EasyFreecamConfig();
        copy.showHand = showHand;
        copy.showPlayer = showPlayer;
        copy.disableOnDamage = disableOnDamage;
        copy.horizontalSpeed = horizontalSpeed;
        copy.verticalSpeed = verticalSpeed;
        copy.sprintMultiplier = sprintMultiplier;
        return copy;
    }
}
