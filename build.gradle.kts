/*
 * Copyright 2020 Wunderman Thompson Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    val gradleAemPluginVersion = "14.2.8"
    id("com.cognifide.aem.instance.local") version gradleAemPluginVersion
    id("com.cognifide.aem.bundle") version gradleAemPluginVersion
    id("com.cognifide.aem.package") version gradleAemPluginVersion
}

group = "com.mysite"

repositories {
    jcenter()
    mavenCentral()
	maven("https://repo.adobe.com/nexus/content/groups/public")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    test {
        failFast = true
        useJUnitPlatform()
        testLogging.showStandardStreams = true
    }
}

dependencies {	
    compileOnly("org.projectlombok:lombok:1.18.12")
    annotationProcessor("org.projectlombok:lombok:1.18.12")

    implementation("com.adobe.aem:aem-sdk-api:2020.6.3766.20200619T110731Z-200604")

    testCompileOnly("org.projectlombok:lombok:1.18.12")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
    testImplementation("org.mockito:mockito-core:2.25.1")
    testImplementation("org.mockito:mockito-junit-jupiter:2.25.1")
    testImplementation("junit-addons:junit-addons:1.4")
    testImplementation("io.wcm:io.wcm.testing.aem-mock.junit5:2.5.2")
    testImplementation("uk.org.lidalia:slf4j-test:1.0.1")
}
