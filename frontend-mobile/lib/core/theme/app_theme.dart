import 'package:flutter/material.dart';

class AppTheme {
  // Brand Colors — always fixed
  static const Color purple = Color(0xFF7C3AED);
  static const Color white = Color(0xFFFFFFFF);
  static const Color darkBg = Color(0xFF0F0F0F);
  static const Color greyText = Color(0xFF9CA3AF);
  static const Color lightBg = Color(0xFFF9FAFB);
  static const Color lightSurface = Color(0xFFFFFFFF);
  static const Color lightText = Color(0xFF111827);
  static const Color lightInputBg = Color(0xFFF3F4F6);
  static const Color darkSurface = Color(0xFF1A1A1A);

  // ── Dynamic helpers ──────────────────────────────────────
  static Color bg(BuildContext context) =>
      Theme.of(context).brightness == Brightness.dark ? darkBg : lightBg;

  static Color textPrimary(BuildContext context) =>
      Theme.of(context).brightness == Brightness.dark ? white : lightText;

  static Color inputBg(BuildContext context) =>
      Theme.of(context).brightness == Brightness.dark ? darkSurface : lightInputBg;

  static ThemeData get lightTheme {
    return ThemeData(
      brightness: Brightness.light,
      scaffoldBackgroundColor: lightBg,
      primaryColor: purple,
      colorScheme: const ColorScheme.light(
        primary: purple,
        onPrimary: white,
        surface: lightSurface,
        onSurface: lightText,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: lightBg,
        foregroundColor: lightText,
        elevation: 0,
        centerTitle: true,
        titleTextStyle: TextStyle(
          color: lightText,
          fontSize: 18,
          fontWeight: FontWeight.w600,
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: purple,
          foregroundColor: white,
          minimumSize: const Size(double.infinity, 52),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: lightInputBg,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide.none,
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: purple, width: 2),
        ),
        labelStyle: const TextStyle(color: greyText),
        hintStyle: const TextStyle(color: greyText),
      ),
    );
  }

  static ThemeData get darkTheme {
    return ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: darkBg,
      primaryColor: purple,
      colorScheme: const ColorScheme.dark(
        primary: purple,
        onPrimary: white,
        surface: darkSurface,
        onSurface: white,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: darkBg,
        foregroundColor: white,
        elevation: 0,
        centerTitle: true,
        titleTextStyle: TextStyle(
          color: white,
          fontSize: 18,
          fontWeight: FontWeight.w600,
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: purple,
          foregroundColor: white,
          minimumSize: const Size(double.infinity, 52),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: darkSurface,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide.none,
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: purple, width: 2),
        ),
        labelStyle: const TextStyle(color: greyText),
        hintStyle: const TextStyle(color: greyText),
      ),
    );
  }
}