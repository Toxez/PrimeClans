package ua.vdev.primeclans.addon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class AddonClassLoader extends URLClassLoader {

    private final AddonDescription description;
    private final AddonLoader addonLoader;
    final AbstractAddon addon;

    public AddonClassLoader(
            @Nullable ClassLoader parent,
            AddonDescription description,
            AddonLoader addonLoader,
            java.io.File file
    ) throws IOException, InvalidAddonException {
        super(new URL[]{file.toURI().toURL()}, parent);
        this.description = description;
        this.addonLoader = addonLoader;

        try {
            Class<?> jarClass;
            try {
                jarClass = Class.forName(description.mainClass(), true, this);
            } catch (ClassNotFoundException ex) {
                throw new InvalidAddonException("Не найден main class [" + description.mainClass() + "]", ex
                );
            }

            Class<? extends AbstractAddon> addonClass;
            try {
                addonClass = jarClass.asSubclass(AbstractAddon.class);
            } catch (ClassCastException ex) {
                throw new InvalidAddonException("main class '" + description.mainClass() + "' не наследует AbstractAddon", ex
                );
            }

            addon = addonClass.getDeclaredConstructor().newInstance();

        } catch (InvalidAddonException e) {
            throw e;
        } catch (Exception ex) {
            throw new InvalidAddonException("Ошибка создания экземпляра аддона", ex);
        }
    }

    void initialize(AbstractAddon addon) {
        addon.init(description, this, addonLoader);
    }

    @Override
    public URL getResource(String name) {
        return findResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return findResources(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass0(name, resolve, true);
    }

    Class<?> loadClass0(@NotNull String name, boolean resolve, boolean global)
            throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException ignore) {}

        if (global) {
            Class<?> result = addonLoader.getClassByName(name, resolve);
            if (result != null) return result;
        }

        throw new ClassNotFoundException(name);
    }
}