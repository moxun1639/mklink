package com.mxstrive.gradle.mklink;

import java.util.function.Predicate;
import java.util.stream.Stream;
import org.gradle.api.Task;

public enum IgnoredTask {

  CLEAN("clean"),
  INIT("init"),
  WRAPPER("wrapper"),
  BUILD_ENVIRONMENT("buildEnvironment"),
  COMPONENTS("components"),
  DEPENDENCIES("dependencies"),
  DEPENDENCY_INSIGHT("dependencyInsight"),
  DEPENDENT_COMPONENTS("dependentComponents"),
  HELP("help"),
  MODEL("model"),
  PROJECTS("projects"),
  PROPERTIES("properties"),
  TASKS("tasks");

  public String name;
  
  private IgnoredTask(String name) {
    this.name = name;
  }

  public static boolean isIgnoredTask(Task task) {
    return Stream.of(values()).anyMatch(new Predicate<IgnoredTask>(){
      @Override
      public boolean test(IgnoredTask t) {
        return task.getName().equals(t.name);
      }
    });
  }
  
}
