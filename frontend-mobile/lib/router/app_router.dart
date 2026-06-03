import 'package:go_router/go_router.dart';
import 'package:flutter/material.dart';
import '../features/auth/presentation/splash_screen.dart';
import '../features/auth/presentation/login_screen.dart';
import '../features/auth/presentation/register_screen.dart';
import '../features/home/presentation/home_screen.dart';
import '../features/profile/presentation/profile_screen.dart';
import '../features/settings/presentation/settings_screen.dart';
import '../features/subscription/presentation/subscription_screen.dart';
import '../features/booking/presentation/courses_screen.dart';
import '../features/booking/presentation/my_bookings_screen.dart';
import '../features/activity/presentation/activity_screen.dart';
import '../features/messaging/presentation/chats_screen.dart';
import '../features/messaging/presentation/chat_screen.dart';
import '../features/shop/presentation/shop_screen.dart';
import '../features/shop/presentation/cart_screen.dart';

final appRouter = GoRouter(
  initialLocation: '/',
  routes: [
    GoRoute(path: '/', builder: (_, __) => const SplashScreen()),
    GoRoute(path: '/login', builder: (_, __) => const LoginScreen()),
    GoRoute(path: '/register', builder: (_, __) => const RegisterScreen()),
    GoRoute(path: '/home', builder: (_, __) => const HomeScreen()),
    GoRoute(
      path: '/profile/:id',
      builder: (context, state) {
        final id = int.tryParse(state.pathParameters['id'] ?? '0') ?? 0;
        return ProfileScreen(memberProfileId: id);
      },
    ),
    GoRoute(path: '/settings', builder: (_, __) => const SettingsScreen()),
    GoRoute(
      path: '/subscription/:id',
      builder: (context, state) {
        final id = int.tryParse(state.pathParameters['id'] ?? '0') ?? 0;
        return SubscriptionScreen(memberProfileId: id);
      },
    ),
    GoRoute(path: '/courses', builder: (_, __) => const CoursesScreen()),
    GoRoute(
      path: '/my-bookings/:memberId',
      builder: (context, state) {
        final id = int.tryParse(state.pathParameters['memberId'] ?? '0') ?? 0;
        return MyBookingsScreen(memberId: id);
      },
    ),
    GoRoute(
      path: '/activity/:id',
      builder: (context, state) {
        final id = int.tryParse(state.pathParameters['id'] ?? '1') ?? 1;
        return ActivityScreen(memberProfileId: id);
      },
    ),
    GoRoute(path: '/chats', builder: (_, __) => const ChatsScreen()),
    GoRoute(
      path: '/chat/:id',
      builder: (context, state) {
        final id = int.tryParse(state.pathParameters['id'] ?? '0') ?? 0;
        return ChatScreen(conversationId: id);
      },
    ),
    GoRoute(path: '/shop', builder: (_, __) => const ShopScreen()),
    GoRoute(path: '/cart', builder: (_, __) => const CartScreen()),
    GoRoute(path: '/dashboard', builder: (_, __) => const _PlaceholderScreen(title: 'Dashboard')),
    GoRoute(path: '/coach', builder: (_, __) => const _PlaceholderScreen(title: 'Coach Portal')),
  ],
);

class _PlaceholderScreen extends StatelessWidget {
  final String title;
  const _PlaceholderScreen({required this.title});

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: Text(title)),
        body: Center(child: Text('$title — coming soon',
            style: const TextStyle(color: Colors.white70))),
      );
}
