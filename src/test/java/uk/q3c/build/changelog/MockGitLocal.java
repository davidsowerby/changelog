package uk.q3c.build.changelog;

import com.google.common.collect.ImmutableList;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.merge.MergeStrategy;
import org.jetbrains.annotations.NotNull;
import uk.q3c.build.gitplus.GitSHA;
import uk.q3c.build.gitplus.gitplus.FileDeleteApprover;
import uk.q3c.build.gitplus.local.*;
import uk.q3c.build.gitplus.remote.GitRemote;
import uk.q3c.build.gitplus.remote.GitRemoteConfiguration;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Sowerby on 15 Nov 2016
 */
@SuppressWarnings("HardcodedFileSeparator")
public class MockGitLocal implements GitLocal {
    List<GitCommit> commits1 = new ArrayList<>();
    List<Tag> tags = new ArrayList<>();
    ZonedDateTime commitDate = ZonedDateTime.of(LocalDateTime.of(2010, 11, 11, 12, 2), ZoneId.of("Z"));
    ZonedDateTime releaseDate = ZonedDateTime.of(LocalDateTime.of(2015, 1, 11, 12, 12), ZoneId.of("Z"));
    PersonIdent personIdent = new PersonIdent("me", "me@there.com");
    private GitLocalConfiguration configuration = new DefaultGitLocalConfiguration();

    public MockGitLocal() {
        createCommits();
    }

    private void createCommits() {
        createCommit("Fix #1 widget properly fixed. \nWith a long and detailed explanation of what\nwas done to fix it."); //0
        createCommit("Fix #2 widget really properly fixed\n\nAlso fix #3 and fix #4"); //1
        createCommit("Fix#5 Typo in fix reference, space missing");//2
        createCommit("Fix #99 Issue number does not exist");//3
        createCommit("Fix #9a Issue number invalid format");//4
        createCommit("Fix davidsowerby/other#18 fixes issue in another repo");//5
        createCommit("Fix davidsowerby-other#18 fix in other repo invalid number");//6
        createCommit("Fix #aa invalid reference, hash with non-number");//7
        createCommit("invalid reference, trailing hash fix #");//8
        createCommit("Fix #6 contains a an exclusion tag.\n\nExclusion tag {{javadoc}}");//9
        createCommit("Fix ##6 double hash");//10

    }

    public Tag createVersionTag(String tagName, int commitIndex, String tagMessage) {
        Tag tag = new Tag(tagName, releaseDate, commitDate, personIdent, tagMessage, commits1.get(commitIndex), Tag.TagType.ANNOTATED);
        tags.add(tag);
        return tag;
    }

    public GitCommit createCommit(String fullMessage) {
        final String hash = DigestUtils.sha1Hex(Integer.toString(commits1.size()));
        final PersonIdent personIdent = new PersonIdent("me", "me@there.com");
        final GitCommit commit = new GitCommit(fullMessage, hash, personIdent, personIdent);
        commits1.add(commit);
        return commit;
    }

    @NotNull
    @Override
    public GitLocalConfiguration getLocalConfiguration() {
        return null;
    }



    @NotNull
    @Override
    public GitRemote getRemote() {
        return null;
    }

    @Override
    public void setRemote(GitRemote gitRemote) {

    }

    @NotNull
    @Override
    public Git getGit() {
        return null;
    }

    @Override
    public void setGit(Git git) {

    }

    @Override
    public void init() {

    }

    @Override
    public void cloneRemote() {

    }

    @Override
    public void prepare(GitRemote gitRemote) {

    }

    @Override
    public void pull() {

    }

    @Override
    public void createAndInitialise() {

    }

    @NotNull
    @Override
    public String currentCommitHash() {
        return null;
    }

    @Override
    public void checkoutNewBranch(GitBranch gitBranch) {

    }

    @Override
    public void checkoutBranch(GitBranch gitBranch) {

    }

    @Override
    public void checkoutCommit(GitSHA gitSHA) {

    }

    @Override
    public void createBranch(String s) {

    }

    @NotNull
    @Override
    public DirCache add(File file) {
        return null;
    }

    @Override
    public GitSHA commit(String s) {
        return new GitSHA(DigestUtils.sha1Hex("xx"));
    }

    @NotNull
    @Override
    public List<String> branches() {
        return null;
    }

    @NotNull
    @Override
    public GitBranch currentBranch() {
        return null;
    }

    @NotNull
    @Override
    public Status status() {
        return null;
    }

    @NotNull
    @Override
    public String getOrigin() {
        return null;
    }

    @Override
    public void setOrigin() {

    }

    @NotNull
    @Override
    public PushResponse push(boolean b, boolean b1) {
        return null;
    }

    @NotNull
    @Override
    public List<Tag> tags() {
        return tags;
    }

    @NotNull
    @Override
    public ImmutableList<GitCommit> extractDevelopCommits() {
        return ImmutableList.copyOf(commits1);
    }

    @NotNull
    @Override
    public ImmutableList<GitCommit> extractMasterCommits() {
        return ImmutableList.copyOf(commits1);
    }

    @Override
    public void tagLightweight(String s) {

    }

    @Override
    public void verifyRemoteFromLocal() {

    }

    @NotNull
    @Override
    public GitSHA latestCommitSHA(GitBranch gitBranch) {
        return null;
    }

    @NotNull
    @Override
    public GitSHA latestDevelopCommitSHA() {
        return null;
    }

    @NotNull
    @Override
    public GitBranch developBranch() {
        return null;
    }

    @NotNull
    @Override
    public GitBranch masterBranch() {
        return null;
    }

    @Override
    public void close() throws Exception {

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
    public File getProjectDirParent() {
        return null;
    }

    @Override
    public void setProjectDirParent(File file) {

    }

    @NotNull
    @Override
    public String getProjectName() {
        return configuration.getProjectName();
    }

    @Override
    public void setProjectName(String s) {

    }

    @NotNull
    @Override
    public CloneExistsResponse getCloneExistsResponse() {
        return null;
    }

    @Override
    public void setCloneExistsResponse(CloneExistsResponse cloneExistsResponse) {

    }

    @NotNull
    @Override
    public FileDeleteApprover getFileDeleteApprover() {
        return null;
    }

    @Override
    public void setFileDeleteApprover(FileDeleteApprover fileDeleteApprover) {

    }

    @NotNull
    @Override
    public String getTaggerName() {
        return null;
    }

    @Override
    public void setTaggerName(String s) {

    }

    @NotNull
    @Override
    public String getTaggerEmail() {
        return null;
    }

    @Override
    public void setTaggerEmail(String s) {

    }

    @Override
    public boolean getCreate() {
        return false;
    }

    @Override
    public void setCreate(boolean b) {

    }

    @Override
    public boolean getCloneFromRemote() {
        return false;
    }

    @Override
    public void setCloneFromRemote(boolean b) {

    }

    @NotNull
    @Override
    public ProjectCreator getProjectCreator() {
        return null;
    }

    @Override
    public void setProjectCreator(ProjectCreator projectCreator) {

    }

    @NotNull
    @Override
    public GitLocalConfiguration cloneExistsResponse(CloneExistsResponse cloneExistsResponse) {
        return null;
    }

    @NotNull
    @Override
    public GitLocalConfiguration fileDeleteApprover(FileDeleteApprover fileDeleteApprover) {
        return null;
    }

    @NotNull
    @Override
    public GitLocalConfiguration projectName(String s) {
        return configuration.projectName(s);
    }

    @NotNull
    @Override
    public GitLocalConfiguration projectDirParent(File file) {
        configuration.projectDirParent(file);
        return this;
    }

    @NotNull
    @Override
    public GitLocalConfiguration taggerEmail(String s) {
        return null;
    }

    @NotNull
    @Override
    public GitLocalConfiguration taggerName(String s) {
        return null;
    }

    @NotNull
    @Override
    public GitLocalConfiguration cloneFromRemote(boolean b) {
        return null;
    }

    @NotNull
    @Override
    public GitLocalConfiguration create(boolean b) {
        return null;
    }

    @NotNull
    @Override
    public GitLocalConfiguration active(boolean b) {
        return null;
    }

    @NotNull
    @Override
    public File projectDir() {
        return new File(configuration.getProjectDirParent(), configuration.getProjectName());
    }

    @Override
    public void validate(GitRemoteConfiguration gitRemote) {

    }

    @NotNull
    @Override
    public GitLocalConfiguration projectCreator(ProjectCreator projectCreator) {
        return null;
    }

    @NotNull
    @Override
    public ImmutableList<GitCommit> extractCommitsFor(String s) {
        return ImmutableList.copyOf(commits1);
    }

    @NotNull
    @Override
    public ImmutableList<GitCommit> extractCommitsFor(GitBranch gitBranch) {
        return ImmutableList.copyOf(commits1);
    }

    @Override
    public void checkoutCommit(GitSHA gitSHA, String s) {

    }

    @Override
    public void tag(String s, String s1) {

    }

    @Override
    public boolean isInitDone() {
        return false;
    }

    @NotNull
    @Override
    public PushResponse pushTag(String s) {
        return null;
    }

    @NotNull
    @Override
    public PushResponse pushAllTags() {
        return null;
    }

    @Override
    public void copyFrom(GitLocalConfiguration gitLocalConfiguration) {
        throw new RuntimeException("TODO");
    }

    @NotNull
    @Override
    public MergeResult mergeBranch(GitBranch gitBranch, MergeStrategy mergeStrategy, MergeCommand.FastForwardMode fastForwardMode) {
        return null;
    }
}
