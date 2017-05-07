package alexanders.applications.gpprocessor.plugin;

import java.security.Permission;

public class PluginSecurityManager extends SecurityManager
{
    @Override
    public void checkPermission(Permission perm)
    {
        String permissionName = perm.getName() == null ? "invalid" : perm.getName();
        if (permissionName.startsWith("exitVM"))
        {
            Class<?>[] context = getClassContext();
            String className = context.length > 4 ? context[4].getName() : "invalid";
            if (className.startsWith("alexanders.applications.gpprocessor"))
                return;
            throw new SecurityException("Plugin tried to call System.exit() this is not allowed please use PluginManager.requestShutdown()");
        } else if (permissionName.equals("setSecurityManager"))
        {
            throw new SecurityException("Replacing the PluginSecurityManager is not allowed");
        }
        //super.checkPermission(perm);
    }

    @Override
    public void checkPermission(Permission perm, Object context)
    {
        this.checkPermission(perm);
        //super.checkPermission(perm, context);
    }
}
