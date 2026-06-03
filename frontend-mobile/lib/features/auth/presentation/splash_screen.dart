import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../presentation/auth_notifier.dart';
import '../domain/auth_state.dart';
import '../../../core/theme/app_theme.dart';

class SplashScreen extends ConsumerStatefulWidget {
  const SplashScreen({super.key});

  @override
  ConsumerState<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends ConsumerState<SplashScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnim;
  bool _hasNavigated = false;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    );
    _fadeAnim = CurvedAnimation(parent: _controller, curve: Curves.easeIn);
    _controller.forward();

    // Fix: use addPostFrameCallback to check and act on the INITIAL state
    // before registering the listener. This prevents the redirect loop where
    // ref.listen in build() only fires on state *changes*, so if the state
    // is already `authenticated` (e.g. hot-restart), the listener never fires
    // and the user gets stuck on the splash screen indefinitely.
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      final currentState = ref.read(authNotifierProvider);

      if (currentState.status == AuthStatus.authenticated) {
        _navigateOnce('/home');
        return;
      }
      if (currentState.status == AuthStatus.unauthenticated) {
        _navigateOnce('/login');
        return;
      }

      // State is still `unknown` — start the async session check
      _checkSession();
    });
  }

  Future<void> _checkSession() async {
    // Minimum splash display time for branding
    await Future.delayed(const Duration(milliseconds: 1200));
    if (!mounted) return;
    await ref.read(authNotifierProvider.notifier).checkSession();
  }

  void _navigateOnce(String path) {
    if (_hasNavigated || !mounted) return;
    _hasNavigated = true;
    context.go(path);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // Fix: guard with _hasNavigated so a widget rebuild after navigation
    // (e.g. triggered by Riverpod) doesn't fire context.go a second time.
    ref.listen<AuthState>(authNotifierProvider, (_, state) {
      if (_hasNavigated) return;
      if (state.status == AuthStatus.authenticated) {
        _navigateOnce('/home');
      } else if (state.status == AuthStatus.unauthenticated) {
        _navigateOnce('/login');
      }
    });

    return Scaffold(
      backgroundColor: AppTheme.bg(context),
      body: FadeTransition(
        opacity: _fadeAnim,
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                'VELTRO',
                style: TextStyle(
                  color: AppTheme.purple,
                  fontSize: 42,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 6,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Your fitness journey starts here',
                style: TextStyle(
                  color: AppTheme.greyText,
                  fontSize: 13,
                  letterSpacing: 0.5,
                ),
              ),
              const SizedBox(height: 48),
              SizedBox(
                width: 24,
                height: 24,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  color: AppTheme.purple,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
