# mklink
Create a symbolic link for gradle
config like this

    plugins {
        id "com.mxstrive.gradle.mklink-plugin" version "1.0.4"
    }

    mklink {
        // next task, which depends on mklink
        next = '*'
        // Specifies the new symbolic link name
        link = "${project.projectDir}\\build"
        // Specifies the path (relative or absolute) that the new link refers to.
        target = "R:\\Build\\${project.name}"
        // [optional] Allow build continue when the root  path of target not exist
        allowNoRootPath = true
    }
