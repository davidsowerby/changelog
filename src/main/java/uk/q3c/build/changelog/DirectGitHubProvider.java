package uk.q3c.build.changelog;

import com.google.inject.Provider;
import uk.q3c.build.gitplus.remote.*;
import uk.q3c.build.gitplus.remote.github.DefaultGitHubProvider;
import uk.q3c.build.gitplus.remote.github.DefaultGitHubRemote;
import uk.q3c.build.gitplus.remote.github.GitHubProvider;
import uk.q3c.build.gitplus.remote.github.GitHubUrlMapper;
import uk.q3c.build.gitplus.util.FileBuildPropertiesLoader;

/**
 * Created by David Sowerby on 30 Nov 2016
 */
public class DirectGitHubProvider implements Provider<GitRemote> {
    @Override
    public GitRemote get() {

        GitRemoteConfiguration remoteConfiguration = new DefaultGitRemoteConfiguration();
        GitHubProvider gitHubProvider = new DefaultGitHubProvider(new FileBuildPropertiesLoader());
        RemoteRequest remoteRequest = new DefaultRemoteRequest();
        GitHubUrlMapper urlMapper = new GitHubUrlMapper();
        return new DefaultGitHubRemote(remoteConfiguration, gitHubProvider, remoteRequest, urlMapper);
    }
}
