package uk.q3c.build.changelog;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by David Sowerby on 30 Nov 2016
 */
@SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "ClassWithoutLogger", "UtilityClass", "UtilityClassCanBeEnum", "PublicMethodWithoutLogging"})
public final class ChangeLogFactory {

    private ChangeLogFactory() {
        // static only
    }

    public static ChangeLog getInstance() {
        final Injector injector = Guice.createInjector(new ChangeLogModule());
        return injector.getInstance(ChangeLog.class);
    }


}
