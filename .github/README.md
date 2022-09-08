### Deploy
- clean ```~/.m2/repository``` dir
- bump version in ```Makefile``` ```publish_to_maven_local``` task
- run ```make publish_to_maven_local```
- pack `````~/.m2/repository/com````` dir in ```~/.m2/repository``` to ```com.zip```
- upload ```com.zip``` to artifactory as Bundle Artifact
