package dev.elpu7.easyFreecam.client;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.util.Locale;

public final class EasyFreecamConfigScreen extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("screen.easy-freecam.config");
    private static final double MIN_SPEED = 4.0D;
    private static final double MAX_SPEED = 40.0D;
    private static final double MIN_SPRINT_MULTIPLIER = 1.0D;
    private static final double MAX_SPRINT_MULTIPLIER = 8.0D;

    private final EasyFreecamConfig config;

    public EasyFreecamConfigScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
        this.config = EasyFreecamConfigManager.getConfig();
    }

    @Override
    protected void addOptions() {
        list.addHeader(Component.translatable("option.easy-freecam.header.movement"));
        list.addSmall(
            new DoubleSlider(
                "option.easy-freecam.horizontal_speed",
                "tooltip.easy-freecam.horizontal_speed",
                MIN_SPEED,
                MAX_SPEED,
                config.horizontalSpeed,
                value -> config.horizontalSpeed = value
            ),
            new DoubleSlider(
                "option.easy-freecam.vertical_speed",
                "tooltip.easy-freecam.vertical_speed",
                MIN_SPEED,
                MAX_SPEED,
                config.verticalSpeed,
                value -> config.verticalSpeed = value
            )
        );
        list.addSmall(
            new DoubleSlider(
                "option.easy-freecam.sprint_multiplier",
                "tooltip.easy-freecam.sprint_multiplier",
                MIN_SPRINT_MULTIPLIER,
                MAX_SPRINT_MULTIPLIER,
                config.sprintMultiplier,
                value -> config.sprintMultiplier = value
            ),
            null
        );

        list.addHeader(Component.translatable("option.easy-freecam.header.visual"));
        list.addSmall(
            createBooleanOption(
                "option.easy-freecam.show_hand",
                "tooltip.easy-freecam.show_hand",
                config.showHand,
                value -> config.showHand = value
            ),
            createBooleanOption(
                "option.easy-freecam.show_player",
                "tooltip.easy-freecam.show_player",
                config.showPlayer,
                value -> config.showPlayer = value
            )
        );

        list.addHeader(Component.translatable("option.easy-freecam.header.safety"));
        list.addSmall(
            createBooleanOption(
                "option.easy-freecam.disable_on_damage",
                "tooltip.easy-freecam.disable_on_damage",
                config.disableOnDamage,
                value -> config.disableOnDamage = value
            ),
            null
        );
    }

    @Override
    public void removed() {
        EasyFreecamConfigManager.save();
        super.removed();
    }

    private OptionInstance<Boolean> createBooleanOption(String key, String tooltipKey, boolean initialValue, java.util.function.Consumer<Boolean> consumer) {
        return OptionInstance.createBoolean(
            key,
            OptionInstance.cachedConstantTooltip(Component.translatable(tooltipKey)),
            initialValue,
            consumer
        );
    }

    private static final class DoubleSlider extends AbstractSliderButton {
        private static final int WIDTH = 150;
        private static final int HEIGHT = 20;

        private final Component caption;
        private final OptionInstance.TooltipSupplier<Double> tooltip;
        private final double minValue;
        private final double maxValue;
        private final java.util.function.DoubleConsumer onValueChanged;

        private DoubleSlider(
            String key,
            String tooltipKey,
            double minValue,
            double maxValue,
            double initialValue,
            java.util.function.DoubleConsumer onValueChanged
        ) {
            super(0, 0, WIDTH, HEIGHT, Component.empty(), toSliderValue(initialValue, minValue, maxValue));
            this.caption = Component.translatable(key);
            this.tooltip = OptionInstance.cachedConstantTooltip(Component.translatable(tooltipKey));
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.onValueChanged = onValueChanged;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(buildMessage());
            setTooltip(tooltip.apply(getActualValue()));
        }

        @Override
        protected void applyValue() {
            onValueChanged.accept(getActualValue());
        }

        private double getActualValue() {
            double actualValue = minValue + value * (maxValue - minValue);
            return Math.round(actualValue * 10.0D) / 10.0D;
        }

        private MutableComponent buildMessage() {
            return Component.empty()
                .append(caption)
                .append(": ")
                .append(Component.literal(formatValue(getActualValue())));
        }

        private static String formatValue(double value) {
            return String.format(Locale.ROOT, "%.1f", value);
        }

        private static double toSliderValue(double value, double minValue, double maxValue) {
            return Mth.inverseLerp(value, minValue, maxValue);
        }
    }
}
