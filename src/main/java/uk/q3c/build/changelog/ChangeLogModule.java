package uk.q3c.build.changelog;

import com.google.inject.AbstractModule;

/**
 * Created by David Sowerby on 12 Nov 2016
 */
public class ChangeLogModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ChangeLogConfiguration.class).to(DefaultChangeLogConfiguration.class);
        bind(ChangeLog.class).to(DefaultChangeLog.class);
    }
}
