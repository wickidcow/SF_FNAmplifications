package ne.fnfal113.fnamplifications.utils.compatibility;

import org.bukkit.Material;

/**
 * Materials that previously needed cross-version aliases.
 *
 * <p>This maintained fork targets Minecraft 1.21.11 and newer, where
 * {@link Material#SHORT_GRASS} is always available. Keeping this as a direct
 * constant avoids parsing Bukkit/Paper version strings during class loading.</p>
 */
public final class VersionedMaterial {

    public static final Material SHORT_GRASS = Material.SHORT_GRASS;

    private VersionedMaterial() {}
}
