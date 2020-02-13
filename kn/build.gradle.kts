plugins {
    kotlin("multiplatform").version("1.3.61")
}

repositories {
    jcenter()
}

kotlin {
    linuxX64 {
        val main by compilations.getting {
            val redismodule by cinterops.creating {
                includeDirs("src/nativeInterop/cinterop")
            }
        }

        binaries {
            sharedLib("brackets_kn")
        }
    }
}
