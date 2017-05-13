package alexanders.applications.gpprocessor.plugin;

import alexanders.applications.gpprocessor.Entry;

import java.security.Permission;

public class PluginSecurityManager extends SecurityManager
{
    @Override
    public void checkPermission(Permission perm)
    {
        Class<?>[] context = getClassContext();
        String className = context.length > 4 ? context[4].getName() : "invalid";

        String permissionName = perm.getName() == null ? "invalid" : perm.getName();
        if (permissionName.startsWith("exitVM"))
        {
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
        Class<?>[] classContext = getClassContext();
        String className = classContext.length > 4 ? classContext[4].getName() : "invalid";
        String currentPluginID = Entry.instance.scheduler.getPluginID(Thread.currentThread());
        if (perm.equals(PluginPermission.canGetPluginLoader))
        {
            if (className.startsWith("alexanders.applications.gpprocessor") || (context instanceof String && context.equals(currentPluginID)))
                return;
            throw new SecurityException("Plugin tried to get another plugin's plugin loader");
        }
        this.checkPermission(perm);
        //super.checkPermission(perm, context);
    }

    public static void checkPermissionS(Permission perm, Object context)
    {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan == null || !(secMan instanceof PluginSecurityManager))
            throw new SecurityException("");
        secMan.checkPermission(perm, context);
    }

    public static void checkPermissionS(Permission perm)
    {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan == null || !(secMan instanceof PluginSecurityManager))
            throw new SecurityException("");
        secMan.checkPermission(perm);
    }
}
