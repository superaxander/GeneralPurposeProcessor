package alexanders.applications.gpprocessor.plugin;

import java.security.Permission;

public class PluginPermission extends Permission
{
    public static final PluginPermission canGetPluginLoader = new PluginPermission("canGetPluginLoader");

    /**
     * Constructs a permission with the specified name.
     *
     * @param name name of the Permission object being created.
     */
    public PluginPermission(String name)
    {
        super(name);
    }

    @Override
    public boolean implies(Permission permission)
    {
        return permission.getName().equals(this.getName());
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof PluginPermission && implies(((PluginPermission) obj));
    }

    @Override
    public int hashCode()
    {
        return getName().hashCode();
    }

    @Override
    public String getActions()
    {
        return null;
    }
}
