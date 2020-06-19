import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class LoginChannel {
  static const _channel = const MethodChannel('kabbalatv/login');

  void login(String user, String password) {
    _channel.invokeMethod("login", [user, password]);
  }
}
