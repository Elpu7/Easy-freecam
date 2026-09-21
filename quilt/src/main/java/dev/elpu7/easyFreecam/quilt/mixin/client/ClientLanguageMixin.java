package dev.elpu7.easyFreecam.quilt.mixin.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin {
    private static final String EASY_FREECAM_LANGUAGE = "/assets/easy-freecam/lang/en_us.json";

    @ModifyArg(
        method = "loadFrom",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;copyOf(Ljava/util/Map;)Ljava/util/Map;"
        ),
        index = 0
    )
    private static Map<String, String> easyFreecam$addTranslations(Map<String, String> translations) {
        try (InputStream input = ClientLanguageMixin.class.getResourceAsStream(EASY_FREECAM_LANGUAGE)) {
            if (input == null) {
                throw new IllegalStateException("Missing Easy Freecam language resource");
            }

            Language.loadFromJson(input, translations::putIfAbsent);
            return translations;
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not load Easy Freecam translations", exception);
        }
    }
}
