import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:get/get.dart';
import 'package:permission_handler/permission_handler.dart';

class RecordingController extends GetxController with WidgetsBindingObserver {
  static const platform = MethodChannel('com.example.demo_app/native');
  static RecordingController get to => Get.find();

  static bool isRecording = false;

  @override
  void onInit() {
    super.onInit();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void onClose() {
    WidgetsBinding.instance.removeObserver(this);
    super.onClose();
  }

  Future<void> getNativeMessage() async {
    try {
      final String result = await platform.invokeMethod('getNativeMessage');
      print("Native message: $result");
    } on PlatformException catch (e) {
      print("Failed to get message: '${e.message}'.");
    }
  }

  Future<void> startRecording() async {
    try {
      if(await requestScreenRecordPermissions()) {
        final String result = await platform.invokeMethod('startRecording');
        isRecording = true;
        print("Native message: $result");
        pauseRecording();
      }
    } on PlatformException catch (e) {
      print("Failed to get message: '${e.message}'.");
    }
  }

  Future<void> pauseRecording() async {
    try {
      await platform.invokeMethod('pauseRecording');
      // print("Native message: $result");
    } on PlatformException catch (e) {
      print("Failed to get message: '${e.message}'.");
    }
  }

  Future<void> resumeRecording() async {
    try {
      await platform.invokeMethod('resumeRecording');
      // print("Native message: $result");
    } on PlatformException catch (e) {
      print("Failed to get message: '${e.message}'.");
    }
  }

  Future<void> stopRecording() async {
    try {
      final String result = await platform.invokeMethod('stopRecording');
      isRecording = false;
      print("Native message: $result");
    } on PlatformException catch (e) {
      print("Failed to get message: '${e.message}'.");
    }
  }

  Future<bool> requestScreenRecordPermissions() async {
    var micStatus = await Permission.microphone.request();
    if (!micStatus.isGranted) {
      return false;
    }

    var anyStorage = false;
    var storageStatus = await Permission.storage.request();
    if (storageStatus.isGranted) {
      anyStorage = true;
    }

    if (!(await Permission.manageExternalStorage.isGranted)) {
      var status = await Permission.manageExternalStorage.request();
      if (status.isGranted) {
        anyStorage = true;
      }
    } else {
      anyStorage = true;
    }
  
    if(!anyStorage) {
      return false;
    }

    return true;
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) async {
    super.didChangeAppLifecycleState(state);
    switch (state) {
      case AppLifecycleState.resumed:
        print("AppLifecycleState.resumed");
        // pause
        if(isRecording) {
          pauseRecording();
        }
        break;
      case AppLifecycleState.inactive:
        print("AppLifecycleState.inactive");
        // resume
        if(isRecording) {
          resumeRecording();
        }
        break;
      case AppLifecycleState.paused:
        print("AppLifecycleState.paused");
        break;
      case AppLifecycleState.detached:
        print("AppLifecycleState.detached");
        break;
      case AppLifecycleState.hidden:
        print("AppLifecycleState.hidden");
        break;
    }
  }
}