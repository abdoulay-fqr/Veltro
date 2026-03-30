import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/theme/app_theme.dart';

void main() {
  runApp(
    const ProviderScope(
      child: VeltroApp(),
    ),
  );
}

class VeltroApp extends StatelessWidget {
  const VeltroApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Veltro',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.darkTheme,
      home: const Scaffold(
        body: Center(
          child: Text(
            'Veltro',
            style: TextStyle(
              color: AppTheme.purple,
              fontSize: 48,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ),
    );
  }
}