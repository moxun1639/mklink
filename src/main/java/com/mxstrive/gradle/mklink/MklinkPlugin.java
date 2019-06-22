package com.mxstrive.gradle.mklink;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MklinkPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getTasks().create("mklink", Mklink.class, (task) -> {
			task.setGroup("Build");
		});
	}
}
