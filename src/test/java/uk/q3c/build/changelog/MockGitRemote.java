package uk.q3c.build.changelog;

import com.google.common.collect.ImmutableList;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.jetbrains.annotations.NotNull;
import uk.q3c.build.gitplus.GitSHA;
import uk.q3c.build.gitplus.local.GitBranch;
import uk.q3c.build.gitplus.local.GitLocal;
import uk.q3c.build.gitplus.remote.*;
import uk.q3c.build.gitplus.remote.github.DefaultGitHubRemote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by David Sowerby on 15 Nov 2016
 */
public class MockGitRemote implements GitRemote {

    private final Map<Integer, GPIssue> issues = new HashMap<>();
    private final List<String> fixWords = ImmutableList.of("fix", "fixes", "fixed", "resolve", "resolves", "resolved", "close", "closes",
            "closed");

    public MockGitRemote() {
        createIssues();
    }

    private void createIssues() {
        issues.put(1, new GPIssue(1).title("widget is not fixed properly"));
        issues.put(2, new GPIssue(2).title("widget gets called twice"));
        issues.put(3, new GPIssue(3).title("issue 3"));
        issues.put(4, new GPIssue(4).title("issue 4"));
        issues.put(5, new GPIssue(5).title("issue 5"));
        issues.put(18, new GPIssue(18).title("issue 18, pretending to be from another repo"));
    }

    @NotNull
    @Override
    public GitRemoteConfiguration getConfiguration() {
        return null;
    }

    @NotNull
    @Override
    public GitRemoteUrlMapper getUrlMapper() {
        return null;
    }

    @Override
    public boolean isIssueFixWord(String s) {
        return fixWords.contains(s.toLowerCase());
    }


    public List<GPIssue> issueSet(int index) {
        switch (index) {
            case 0:
                return ImmutableList.of();
            case 1:
                return ImmutableList.of(issues.get(1));
            case 2:
                return ImmutableList.of(issues.get(2), issues.get(3), issues.get(4));
            case 3:
                return ImmutableList.of(issues.get(5));
            case 4:
                return ImmutableList.of(issues.get(18));
        }
        throw new UnsupportedOperationException("test not configured for that set index");
    }


    @NotNull
    @Override
    public GPIssue getIssue(int i) {
        if (issues.containsKey(i)) {
            return issues.get(i);
        } else {
            throw new GitRemoteException("Issue not found, issue: " + i);
        }
    }

    /**
     * Returns issue 18 as though it were from another repo
     *
     * @param s
     * @param s1
     * @param i
     * @return
     */
    @NotNull
    @Override
    public GPIssue getIssue(String s, String s1, int i) {
        return issues.get(18);
    }

    @NotNull
    @Override
    public CredentialsProvider getCredentialsProvider() {
        return null;
    }

    @NotNull
    @Override
    public DefaultGitHubRemote.Status apiStatus() {
        return null;
    }

    @NotNull
    @Override
    public GPIssue createIssue(String s, String s1, String... strings) {
        return null;
    }

    @Override
    public void createRepo() {

    }

    @Override
    public void deleteRepo() {

    }

    @NotNull
    @Override
    public Set<String> listRepositoryNames() {
        return null;
    }

    @Override
    public void mergeLabels() {

    }

    @NotNull
    @Override
    public Map<String, String> mergeLabels(Map<String, String> map) {
        return null;
    }

    @NotNull
    @Override
    public Map<String, String> getLabelsAsMap() {
        return null;
    }

    @NotNull
    @Override
    public GitSHA latestDevelopCommitSHA() {
        return null;
    }

    @NotNull
    @Override
    public GitSHA latestCommitSHA(GitBranch gitBranch) {
        return null;
    }

    @Override
    public boolean hasBranch(GitBranch gitBranch) {
        return false;
    }

    @Override
    public void prepare(GitLocal gitLocal) {

    }

    @Override
    public boolean getActive() {
        return false;
    }

    @Override
    public void setActive(boolean b) {

    }

    @NotNull
    @Override
    public String getProjectDescription() {
        return null;
    }

    @Override
    public void setProjectDescription(String s) {

    }

    @NotNull
    @Override
    public String getProjectHomePage() {
        return null;
    }

    @Override
    public void setProjectHomePage(String s) {

    }

    @Override
    public boolean getPublicProject() {
        return false;
    }

    @Override
    public void setPublicProject(boolean b) {

    }

    @NotNull
    @Override
    public String getRepoUser() {
        return null;
    }

    @Override
    public void setRepoUser(String s) {

    }

    @NotNull
    @Override
    public String getRepoName() {
        return null;
    }

    @Override
    public void setRepoName(String s) {

    }

    @NotNull
    @Override
    public Map<String, String> getIssueLabels() {
        return null;
    }

    @Override
    public void setIssueLabels(Map<String, String> map) {

    }

    @Override
    public boolean getMergeIssueLabels() {
        return false;
    }

    @Override
    public void setMergeIssueLabels(boolean b) {

    }

    @NotNull
    @Override
    public String getConfirmDelete() {
        return null;
    }

    @Override
    public void setConfirmDelete(String s) {

    }

    @Override
    public boolean getCreate() {
        return false;
    }

    @Override
    public void setCreate(boolean b) {

    }

    @NotNull
    @Override
    public RemoteRepoDeleteApprover getRepoDeleteApprover() {
        return null;
    }

    @Override
    public void setRepoDeleteApprover(RemoteRepoDeleteApprover remoteRepoDeleteApprover) {

    }

    @NotNull
    @Override
    public String getProviderBaseUrl() {
        return null;
    }

    @Override
    public void setProviderBaseUrl(String s) {

    }

    @Override
    public boolean deleteRepoApproved() {
        return false;
    }

    @NotNull
    @Override
    public String remoteRepoFullName() {
        return null;
    }

    @Override
    public void setupFromOrigin(String s) {

    }

    @NotNull
    @Override
    public GitRemoteConfiguration repoUser(String s) {
        return null;
    }

    @NotNull
    @Override
    public GitRemoteConfiguration repoName(String s) {
        return null;
    }

    @NotNull
    @Override
    public GitRemoteConfiguration mergeIssueLabels(boolean b) {
        return null;
    }

    @NotNull
    @Override
    public GitRemoteConfiguration issueLabels(Map<String, String> map) {
        return null;
    }

    @NotNull
    @Override
    public GitRemoteConfiguration projectDescription(String s) {
        return null;
    }

    @NotNull
    @Override
    public GitRemoteConfiguration projectHomePage(String s) {
        return null;
    }

    @NotNull
    @Override
    public GitRemoteConfiguration publicProject(boolean b) {
        return null;
    }

    @NotNull
    @Override
    public GitRemoteConfiguration confirmDelete(String s) {
        return null;
    }

    @NotNull
    @Override
    public GitRemoteConfiguration repoDeleteApprover(RemoteRepoDeleteApprover remoteRepoDeleteApprover) {
        return null;
    }

    @NotNull
    @Override
    public GitRemoteConfiguration create(boolean b) {
        return null;
    }

    @Override
    public void copy(GitRemoteConfiguration gitRemoteConfiguration) {

    }

    @NotNull
    @Override
    public GitRemoteConfiguration active(boolean b) {
        return null;
    }

    @Override
    public void validate(GitLocal gitLocal) {

    }

    @NotNull
    @Override
    public GitRemote getParent() {
        return null;
    }

    @Override
    public void setParent(GitRemote gitRemote) {

    }

    @NotNull
    @Override
    public String repoBaselUrl() {
        return null;
    }

    @NotNull
    @Override
    public String cloneUrl() {
        return null;
    }

    @NotNull
    @Override
    public String tagUrl() {
        return null;
    }

    @NotNull
    @Override
    public String wikiUrl() {
        return null;
    }

    @NotNull
    @Override
    public String apiUrl() {
        return null;
    }

    @NotNull
    @Override
    public String wikiCloneUrl() {
        return null;
    }

    @NotNull
    @Override
    public String issuesUrl() {
        return null;
    }
}
