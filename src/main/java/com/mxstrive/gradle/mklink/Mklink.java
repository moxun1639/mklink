package com.mxstrive.gradle.mklink;

import java.io.File;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GFileUtils;

public class Mklink extends DefaultTask {
	private static final String NEXT_ANY_TASK_NAME = "*";
	private static final String CLEAN_TASK_NAME = "clean";
	public String next = NEXT_ANY_TASK_NAME;
	public String link;
	public String target;

	@Input
	public void setNext(String next) {
		this.next = next;
		Project project = getProject();
		project.getTasks().forEach((task) -> {
			if (task.getName().equals(CLEAN_TASK_NAME)) {
				return;
			}
//			System.out.println(task.getPath() + " => " + task.getProject().getProjectDir() + " - " + task.getGroup()	+ " - " + task.getName());
			if (task != this && (next.equals(NEXT_ANY_TASK_NAME) || task.getName().equals(next))) {
				task.dependsOn(this);
			}
		});
	}

	@Input
	public void setLink(String link) {
		this.link = link;
	}

	@Input
	public void setTarget(String target) {
		this.target = target;
	}

	@TaskAction
	void makeLink() {
		if (target == null) {
			throw new GradleException("please set target");
		}
		Logger logger = getLogger();
		File linkDir = new File(link);
		File targetDir = new File(target);

		logger.debug("mklink: [" + linkDir + "] ==> [" + targetDir + "]");

		GFileUtils.mkdirs(targetDir);
		if (linkDir.exists() && targetDir.exists()) {
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

		GFileUtils.mkdirs(targetDir);
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
			throw new GradleException("link error");
		}
	}

}
