package com.mxstrive.gradle.mklink;

import groovy.lang.Closure;
import org.gradle.api.DefaultTask;

public class Mklinks extends DefaultTask {
	private int mklinkTaskCount = 0;

	public void mklink(Closure<Void> config) {
		config.setDelegate(getProject().getTasks().create("mklink" + (++mklinkTaskCount), Mklink.class));
		config.call();
	}
}
