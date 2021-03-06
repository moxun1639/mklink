package com.mxstrive.gradle.mklink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GFileUtils;

public class Mklink extends DefaultTask {
	public static final String MKLINK_TASK_NAME = "mklink";
	private static final String NEXT_ANY_TASK_NAME = "*";
	private String next;
	private String link;
	private String target;
	private Boolean allowNoRootPath = false;

	@Input
	public void setNext(String next) {
		this.next = next;
		Project project = getProject();
		project.getTasks().forEach((task) -> {
			if (IgnoredTask.isIgnoredTask(task)) {
				return;
			}
			if (task.getName().startsWith(Mklink.MKLINK_TASK_NAME)) {
				return;
			}
//			System.out.println(task.getPath() + " => " + task.getProject().getProjectDir() + " - " + task.getGroup()	+ " - " + task.getName());
			if (task != this && (next.equals(NEXT_ANY_TASK_NAME) || task.getName().equals(next))) {
				task.dependsOn(this);
			}
		});
		makeTaskDescription();
	}

	@Input
	public void setLink(String link) {
		this.link = link;
		makeTaskDescription();
	}

	@Input
	public void setTarget(String target) {
		this.target = target;
		makeTaskDescription();
	}

	@Input
	public void setAllowNoRootPath(Boolean allowNoRootPath) {
	  this.allowNoRootPath = allowNoRootPath;
	}

	private void makeTaskDescription() {
		if ( next == null || link == null || target == null) {
			return;
		}
		setDescription(String.format("Create a symbolic link for [%s] before [%s]: [%s] => [%s]", getProject().getPath(), next, link, target));
	}
	
	@TaskAction
	private void makeLink() {
		if (target == null) {
			throw new GradleException("please set target");
		}
		Logger logger = getLogger();
		File linkDir = new File(link);
		File targetDir = new File(target);

		logger.debug("mklink: [" + linkDir + "] ==> [" + targetDir + "]");

		Path rootPath = Paths.get(target).getRoot();
		if (!rootPath.toFile().exists()) {
			if (allowNoRootPath) {
				logger.warn("rootPath: [" + rootPath + "] is not exists, the symbolic link will not be created");
				return;
			} else {
				throw new GradleException("rootPath: [" + rootPath + "] is not exists");
			}
		}
		try {
			GFileUtils.mkdirs(targetDir);
		} catch (Exception e) {
			logger.warn("mkdirs fail, [" + targetDir.getAbsolutePath() + "]");
		}
		if (linkDir.exists()) {
			String wContent = System.currentTimeMillis() + "";
			String testFileName = ".link.tmp";
			File testFile = new File(linkDir.getAbsoluteFile() + "/" + testFileName);
			GFileUtils.writeFile(wContent, testFile);
			if (!testFile.exists()) {
				throw new GradleException("please delete：[" + linkDir.getAbsolutePath() + "]");
			}
			File destTestFile = new File(targetDir.getAbsoluteFile() + "/" + testFileName);
			if (!destTestFile.exists()) {
				GFileUtils.forceDelete(testFile);
				throw new GradleException("please delete：[" + linkDir.getAbsolutePath() + "]");
			}
			String rContent = GFileUtils.readFile(destTestFile);
			GFileUtils.forceDelete(testFile);
			if (wContent.equals(rContent)) {
				return;
			}
		} else if (linkDir.exists()) {
			throw new GradleException("please delete：[" + linkDir.getAbsolutePath() + "]");
		}

		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String cmd = null;
		logger.debug("osName: " + osName);
		logger.debug("osVersion: " + osVersion);
		if (osName.startsWith("Windows")) {
			cmd = String.format("cmd /C mklink /J %s %s", linkDir.getPath(), targetDir.getPath());
		} else if (osName.equals("Linux") || osName.startsWith("Mac OS X")) {
			cmd = String.format("ln -s %s %s", targetDir.getPath(), linkDir.getPath());
		}
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (InterruptedException | IOException e) {
			throw new GradleException(e.toString());
		}
		if (process.exitValue() != 0) {
			throw new GradleException("mklink error");
		}
	}

}
