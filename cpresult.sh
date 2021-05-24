kubectl cp ubuntu1:home/build_user/avito3/avito4/result.zip ./result.zip
rm -rf ./result
unzip ./result.zip -d result
xdg-open result/subprojects/outputs/ui-testing-core-app/instrumentation/ui/test-runner
