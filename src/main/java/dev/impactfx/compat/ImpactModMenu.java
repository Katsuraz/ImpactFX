package dev.impactfx.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.impactfx.gui.ImpactConfigScreen;

/** Loaded only by Mod Menu; the core mod runs without Mod Menu installed. */
public final class ImpactModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ImpactConfigScreen::new;
    }
}
