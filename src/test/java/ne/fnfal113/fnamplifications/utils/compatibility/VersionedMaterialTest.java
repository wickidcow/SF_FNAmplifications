package ne.fnfal113.fnamplifications.utils.compatibility;

import static org.junit.Assert.assertSame;

import org.bukkit.Material;
import org.junit.Test;

public class VersionedMaterialTest {

    @Test
    public void shortGrassDoesNotDependOnServerVersionString() {
        assertSame(Material.SHORT_GRASS, VersionedMaterial.SHORT_GRASS);
    }
}
