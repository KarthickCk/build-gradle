Gradle plugin that post-processes JUnit XML test results to add a                                                                                                             
`filename` attribute (and, on failures, a `line` attribute) to each                                                                                                           
`<testcase>`. CI tools can render clickable links from test reports                                                                                                           
straight to source.
                                                                                                                                                                                
---                                                                                                                                                                           

## What it does

Input (standard JUnit XML):
  ```xml
  <testcase name="matches" classname="com.adyen.GlobMatcherTest" time="0.001"/>                                                                                                 
  ```                                                                                                                                                                           

Output (augmented):
  ```xml                                                                                                                                                                      
  <testcase name="matches" classname="com.adyen.GlobMatcherTest" time="0.001"
            filename="app/src/test/java/com/adyen/GlobMatcherTest.kt"/>                                                                                                         
  ```                                                                                                                                                                           

For failed tests, the line of the failing assertion is also added:
  ```xml                                                                                                                                                                      
  <testcase ... filename="..." line="72">                                                                                                                                       
    <failure>...</failure>                                                                                                                                                      
  </testcase>                                                                                                                                                                   
  ```                                                                                                                                                                           

Originals are untouched; augmented copies go to a separate directory.
                                                                                                                                                                              
---

## Requirements

- Gradle 8.0+ (tested on 9.1)
- JDK 21
- Consumer must apply `java` or `kotlin("jvm")`

  ---

## Setup

In the consumer's root `settings.gradle.kts` (must be the first block):

  ```kotlin                                                                                                                                                                     
  pluginManagement {                                                                                                                                                          
      includeBuild("junit-plugin")
  }
  ```

In the consumer module's `build.gradle.kts`:

  ```kotlin                                                                                                                                                                     
  plugins {                                                                                                                                                                   
      kotlin("jvm") version "2.1.20"
      id("com.adyen.junit-plugin")                                                                                                                                              
  }                                                                                                                                                                             
  ```                                                                                                                                                                           
                                                                                                                                                                                
---                                                                                                                                                                         

## Usage

Just run tests as normal:

  ```
  ./gradlew :app:test
  ```                                                                                                                                                                           

Augmented XMLs land in:

  ```                                                                                                                                                                           
  <consumer>/build/test-results-augmented/                                                                                                                                    
  ```

The plugin attaches itself to every `Test` task via `finalizedBy`, so it                                                                                                      
runs whether tests pass or fail.
                                                                                                                                                                                
---                                                                                                                                                                         

## Configuration

  ```kotlin                                                                                                                                                                     
  junitFilename {                                                                                                                                                             
      outputDir = layout.buildDirectory.dir("ci/test-reports")
  }                                                                                                                                                                             
  ```

| Property    | Default                          | Description                  |                                                                                           
  | ----------- | -------------------------------- | ---------------------------- |
| `outputDir` | `build/test-results-augmented`   | Where augmented XMLs go      |                                                                                             
   
---

## Development

  ```                                                                                                                                                                           
  ./gradlew :junit-plugin:test       # plugin's own tests
  ./gradlew :app:test                # end-to-end against the example consumer                                                                                                  
  ls app/build/test-results-augmented/                                                                                                                                          
  ```                                                                                                                                                                           
                                                                                                                                                                                
---
