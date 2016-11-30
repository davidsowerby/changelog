package uk.q3c.build.changelog;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;
import uk.q3c.build.gitplus.gitplus.DefaultGitPlus;
import uk.q3c.build.gitplus.gitplus.GitPlus;
import uk.q3c.build.gitplus.local.*;
import uk.q3c.build.gitplus.remote.DefaultGitRemoteProvider;
import uk.q3c.build.gitplus.remote.GitRemote;
import uk.q3c.build.gitplus.remote.GitRemoteProvider;
import uk.q3c.build.gitplus.remote.ServiceProvider;

import java.util.Map;

/**
 * Created by David Sowerby on 30 Nov 2016
 */
public class ChangeLogFactory {

    public static ChangeLog getInstance() {
        BranchConfigProvider branchConfigProvider = new DefaultBranchConfigProvider();
        BranchConfigProvider branchConfigProvider2 = new DefaultBranchConfigProvider();
        GitProvider gitProvider = new DefaultGitProvider();
        GitProvider gitProvider2 = new DefaultGitProvider();
        GitLocalConfiguration gitLocalConfiguration = new DefaultGitLocalConfiguration();
        GitLocalConfiguration gitLocalConfiguration2 = new DefaultGitLocalConfiguration();
        GitLocal gitLocal = new DefaultGitLocal(branchConfigProvider, gitProvider, gitLocalConfiguration);
        WikiLocal wikiLocal = new DefaultWikiLocal(branchConfigProvider2, gitProvider2, gitLocalConfiguration2);
        Map<ServiceProvider, Provider<GitRemote>> remotes = ImmutableMap.of(ServiceProvider.GITHUB, new DirectGitHubProvider());
        GitRemoteProvider gitRemoteProvider = new DefaultGitRemoteProvider(remotes);
        GitPlus gitPlus = new DefaultGitPlus(gitLocal, wikiLocal, gitRemoteProvider);
        ChangeLogConfiguration changelogConfiguration = new DefaultChangeLogConfiguration();
        VersionHistoryBuilder versionHistoryBuilder = new DefaultVersionHistoryBuilder();
        return new DefaultChangeLog(gitPlus, changelogConfiguration, versionHistoryBuilder);
    }


}
