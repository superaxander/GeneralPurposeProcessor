package alexanders.applications.gpprocessor.plugin;

import alexanders.api.gpprocessor.annotation.AutoPopulate;
import alexanders.api.gpprocessor.annotation.AutoPopulationType;

import java.lang.annotation.Annotation;

public class AnnotationFactory
{
    public static AutoPopulate buildAutoPopulate(AutoPopulationType type, String pluginID, Class<?> injectedClass)
    {
        return new AutoPopulate(){

            @Override
            public Class<? extends Annotation> annotationType()
            {
                return AutoPopulate.class;
            }

            @Override
            public AutoPopulationType populationType()
            {
                return type;
            }

            @Override
            public String pluginID()
            {
                return pluginID;
            }

            @Override
            public Class<?> injectedClass()
            {
                return injectedClass;
            }
        };
    }
}
