import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/theme/app_theme.dart';
import 'router/app_router.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  // ── FCM Deep Link Setup ──────────────────────────────────────────────────
  // When real Firebase Cloud Messaging is integrated (Phase 9), uncomment:
  //
  // await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
  //
  // Handle notification tap when app is in background (terminated → background)
  // FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
  //   final ctx = NotificationRouter.navigatorKey.currentContext;
  //   if (ctx != null && message.data.isNotEmpty) {
  //     NotificationRouter.handleNotificationTap(ctx, message.data);
  //   }
  // });
  //
  // Handle notification tap when app is fully terminated (cold start)
  // final initialMessage = await FirebaseMessaging.instance.getInitialMessage();
  // if (initialMessage != null && initialMessage.data.isNotEmpty) {
  //   // Store the initial route — navigate after app is fully built
  //   _initialNotificationData = initialMessage.data;
  // }
  //
  // ── End FCM Setup ────────────────────────────────────────────────────────

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
    return MaterialApp.router(
      title: 'Veltro',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      routerConfig: appRouter,
      // When FCM is integrated, pass the navigatorKey so NotificationRouter
      // can navigate from outside the widget tree:
      // navigatorKey: NotificationRouter.navigatorKey,
    );
  }
}
