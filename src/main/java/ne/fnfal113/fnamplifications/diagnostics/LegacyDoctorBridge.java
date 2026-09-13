package ne.fnfal113.fnamplifications.diagnostics;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.logging.Level;
import ne.fnfal113.fnamplifications.FNAmplifications;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

/** Optional reflective bridge to Slimefun Legacy's Addon Doctor API. */
public final class LegacyDoctorBridge {

    private static final String DOCTOR_API =
        "io.github.thebusybiscuit.slimefun4.api.diagnostics.AddonDoctor";
    private static final String REPORT_API =
        "io.github.thebusybiscuit.slimefun4.api.diagnostics.AddonDoctorReport";

    private LegacyDoctorBridge() {
    }

    public static void register(FNAmplifications plugin) {
        Plugin slimefun = Bukkit.getPluginManager().getPlugin("Slimefun");
        if (slimefun == null) {
            return;
        }

        ClassLoader loader = slimefun.getClass().getClassLoader();
        try {
            Class<?> doctorInterface = Class.forName(DOCTOR_API, false, loader);
            Class<?> reportClass = Class.forName(REPORT_API, false, loader);
            Constructor<?> reportConstructor = reportClass.getConstructor(
                String.class, boolean.class, long.class, long.class, long.class, long.class, List.class);
            InvocationHandler handler =
                (proxy, method, arguments) -> invokeDoctor(proxy, method, arguments, reportConstructor);
            Object provider = Proxy.newProxyInstance(loader, new Class<?>[]{doctorInterface}, handler);
            registerRaw(Bukkit.getServicesManager(), doctorInterface, provider, plugin);
            plugin.getLogger().info("Registered FN Amplifications quiver reconciliation with Slimefun Legacy Doctor.");
        } catch (ClassNotFoundException ignored) {
            // Other Slimefun implementations do not necessarily expose Legacy's optional Doctor API.
        } catch (ReflectiveOperationException | RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING,
                "Could not register FN Amplifications' optional Slimefun Doctor bridge.", exception);
        }
    }

    public static void unregister(FNAmplifications plugin) {
        Bukkit.getServicesManager().unregisterAll(plugin);
    }

    private static Object invokeDoctor(
        Object proxy, Method method, Object[] arguments, Constructor<?> reportConstructor)
        throws ReflectiveOperationException {
        return switch (method.getName()) {
            case "getAddonName" -> "FN Amplifications";
            case "runDoctor" -> {
                boolean repair = arguments != null && arguments.length > 0 && Boolean.TRUE.equals(arguments[0]);
                FNDoctorReport report = FNDoctor.run(repair);
                yield reportConstructor.newInstance(
                    "FN Amplifications",
                    repair,
                    report.scannedEntries(),
                    report.issuesFound(),
                    report.repairedEntries(),
                    report.failures(),
                    report.details());
            }
            case "toString" -> "FNAmplificationsAddonDoctor";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> arguments != null && arguments.length == 1 && arguments[0] == proxy;
            default -> throw new UnsupportedOperationException("Unsupported AddonDoctor method: " + method.getName());
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerRaw(
        ServicesManager services, Class service, Object provider, FNAmplifications plugin) {
        services.register(service, provider, plugin, ServicePriority.Normal);
    }
}
