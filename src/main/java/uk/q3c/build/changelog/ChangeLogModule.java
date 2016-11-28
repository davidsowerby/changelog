package uk.q3c.build.changelog;

import com.google.inject.AbstractModule;
import uk.q3c.build.gitplus.GitPlusModule;

/**
 * Created by David Sowerby on 12 Nov 2016
 */
@SuppressWarnings("ALL")
public class ChangeLogModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new GitPlusModule());
        bind(ChangeLogConfiguration.class).to(DefaultChangeLogConfiguration.class);
        bind(ChangeLog.class).to(DefaultChangeLog.class);
        bind(VersionHistoryBuilder.class).to(DefaultVersionHistoryBuilder.class);
    }
}
