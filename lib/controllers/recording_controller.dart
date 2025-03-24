import 'package:flutter/services.dart';
import 'package:get/get.dart';

class RecordingController extends GetxController {
  static const platform = MethodChannel('com.example.demo_app/native');
  static RecordingController get to => Get.find();

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
      final String result = await platform.invokeMethod('startRecording');
      print("Native message: $result");
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
      print("Native message: $result");
    } on PlatformException catch (e) {
      print("Failed to get message: '${e.message}'.");
    }
  }
}