import 'package:go_router/go_router.dart';
import 'package:flutter/material.dart';
import '../features/auth/presentation/splash_screen.dart';
import '../features/auth/presentation/login_screen.dart';
import '../features/auth/presentation/register_screen.dart';

final appRouter = GoRouter(
  initialLocation: '/',
  routes: [
    GoRoute(
      path: '/',
      builder: (context, state) => const SplashScreen(),
    ),
    GoRoute(
      path: '/login',
      builder: (context, state) => const LoginScreen(),
    ),
    GoRoute(
      path: '/register',
      builder: (context, state) => const RegisterScreen(),
    ),
    // Placeholder routes — will be replaced in later phases
    GoRoute(
      path: '/dashboard',
      builder: (context, state) => const _PlaceholderScreen(title: 'Dashboard'),
    ),
    GoRoute(
      path: '/coach',
      builder: (context, state) => const _PlaceholderScreen(title: 'Coach Portal'),
    ),
    GoRoute(
      path: '/home',
      builder: (context, state) => const _PlaceholderScreen(title: 'Member Home'),
    ),
  ],
);

class _PlaceholderScreen extends StatelessWidget {
  final String title;
  const _PlaceholderScreen({required this.title});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Center(
        child: Text(
          '$title — coming soon',
          style: const TextStyle(color: Colors.white70),
        ),
      ),
    );
  }
}