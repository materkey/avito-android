git pull
make clean stage_ui_tests verbose=DEBUG kubernetesContext=minikube
#mkdir -p subprojects/outputs/ui-testing-core-app/instrumentation/ui/report-files
#cp -r /tmp/* subprojects/outputs/ui-testing-core-app/instrumentation/ui/report-files/
rm result.zip
zip -r result.zip subprojects/outputs/ui-testing-core-app/instrumentation/ui
