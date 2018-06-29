package com.tiefan.framework.version.plugin;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.HashMap;
import java.util.List;

/**
 * @author gavinwu
 * @date 2018/5/31 14:37
 */

@Mojo(name = "check")
public class VersionCheckMojo extends AbstractMojo {


    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("version check begin...");
        check();
        getLog().info("version check end...");
    }

    /**
     * 校验版本有效性
     * @throws MojoExecutionException
     */
    public void check() throws MojoExecutionException {
        HashMap<String, String> versionMap = new HashMap<String, String>();
        try {
            DependencyManagement dependencyManagement = project.getDependencyManagement();
            List<Dependency> list = dependencyManagement.getDependencies();
            if (list == null || list.size() <= 0) {
                throw new MojoExecutionException("DependencyManagement.dependencies.size=0");
            }
            for (int i = 0; i < list.size(); i++) {
                Dependency d = list.get(i);
                versionMap.put(d.getGroupId() + ":" + d.getArtifactId(), d.getVersion());
            }
            list = project.getDependencies();
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    Dependency d = list.get(i);
                    String key = d.getGroupId() + ":" + d.getArtifactId();
                    String version = versionMap.get(key);
                    if (version != null) {
                        if (compareVersion(version, d.getVersion()) > 0) {
                            throw new MojoExecutionException("the version of " + d.getGroupId() + ":" + d.getArtifactId() + " is too low , please upgrade");
                        }
                    }
                }
            }
        } finally {
            versionMap.clear();
        }
    }

    public int compareVersion(String parentVersion, String childVersion) {
        ComparableVersion parentCV = new ComparableVersion(parentVersion);
        ComparableVersion childCV = new ComparableVersion(childVersion);
        return parentCV.compareTo(childCV);
    }


}
