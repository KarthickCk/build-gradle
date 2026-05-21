CLI that reads `filters.yaml`, matches glob patterns against changed file                                                                                                     
paths, and writes the results as a GitLab-CI-compatible `.env` file.
Modeled after GitHub Actions                                                                                                                                                  
[paths-filter](https://github.com/marketplace/actions/paths-changes-filter).
                                                                                                                                                                                
---                                                                                                                                                                           

## Quick example

`filters.yaml`:
  ```yaml
  filters:
    backend:
      - "app/src/**/*.kt"                                                                                                                                                       
      - "!app/src/test/**"                                                                                                                                                      
    frontend:                                                                                                                                                                   
      - "web/**/*.ts"                                                                                                                                                           
      - "web/**/*.tsx"                                                                                                                                                          
    docs:
      - "docs/**/*.md"                                                                                                                                                          
  ```                                                                                                                                                                         

Run:
  ```
  ./gradlew :app:run --args="--config app/filters.yaml --files 'app/src/main/App.kt web/src/i.tsx'"                                                                             
  ```                                                                                                                                                                           

Output (`.env`):
  ```                                                                                                                                                                         
  backend=true
  frontend=true
  docs=false
  ```

  ---

## Requirements

- JDK 21 (handled by the Gradle toolchain)
- Internet access on first `./gradlew` run

  ---                                                                                                                                                                         

## CLI options

| Option     | Required | Default | Description                                |                                                                                              
  | ---------- | -------- | ------- | ------------------------------------------ |
| `--config` | yes      | —       | Path to `filters.yaml`                     |                                                                                              
| `--files`  | yes      | —       | Space-separated changed file paths         |                                                                                              
| `--output` | no       | `.env`  | Where to write the env file                |
                                                                                                                                                                                
---                                                                                                                                                                         

## Filter semantics

A filter matches if **any** changed file matches at least one inclusion                                                                                                       
pattern AND does not match any exclusion pattern (`!`-prefixed).

Glob syntax follows `paths-filter`:

- `*` — characters within one path segment
- `**` — zero or more path segments
- `!pattern` — exclusion

So `app/**/Main.kt` matches both `app/Main.kt` and `app/src/Main.kt`.
                                                                                                                                                                              
---                                                                                                                                                                           

## GitLab CI integration

  ```yaml                                                                                                                                                                       
  detect-changes:                                                                                                                                                             
    script:
      - ./gradlew :app:run --args="--config filters.yaml --files \"$CHANGED_FILES\""
    artifacts:                                                                                                                                                                  
      reports:
        dotenv: .env                                                                                                                                                            
                                                                                                                                                                              
  backend-job:
    needs: [detect-changes]
    rules:                                                                                                                                                                      
      - if: '$backend == "true"'                                                                                                                                                
    script: ./gradlew :app:assemble                                                                                                                                             
  ```                                                                                                                                                                           
                                                                                                                                                                              
---                                                                                                                                                                         

## Test

  ```                                                                                                                                                                           
  ./gradlew :app:test                                                                                                                                                         
  ```                                                                                                                                                                         

  ---

## See also

[`junit-plugin/README.md`](junit-plugin/README.md) — Part 2: Gradle plugin                                                                                                    
that adds `filename` and `line` attributes to JUnit XML reports.
