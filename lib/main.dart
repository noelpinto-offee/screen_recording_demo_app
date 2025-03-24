import 'package:demo_app/controllers/recording_controller.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

void main() {
  Get.put(RecordingController());
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
      ),
      home: Scaffold(
        appBar: AppBar(title: Text("Screen Recording App"),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            spacing: 24.0,
            children: [
                ElevatedButton(
                  child: Text("Start Recording"),
                  onPressed: () => RecordingController.to.startRecording(),
                ),
                ElevatedButton(
                  child: Text("Pause Recording"),
                  onPressed: () => RecordingController.to.pauseRecording(),
                ),
                ElevatedButton(
                  child: Text("Resume Recording"),
                  onPressed: () => RecordingController.to.resumeRecording(),
                ),
                ElevatedButton(
                  child: Text("Stop Recording"),
                  onPressed: () => RecordingController.to.stopRecording(),
                )
            ],
          )
        ),
      ),
    );
  }
}