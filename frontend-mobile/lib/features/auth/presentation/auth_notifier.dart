import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/auth_repository.dart';
import '../domain/auth_state.dart';
import 'package:dio/dio.dart';

final authRepositoryProvider = Provider<AuthRepository>((ref) => AuthRepository());

final authNotifierProvider = StateNotifierProvider<AuthNotifier, AuthState>(
  (ref) => AuthNotifier(ref.read(authRepositoryProvider)),
);

class AuthNotifier extends StateNotifier<AuthState> {
  final AuthRepository _repository;

  AuthNotifier(this._repository) : super(const AuthState());

  Future<void> checkSession() async {
    final session = await _repository.getSavedSession();
    if (session['token'] != null && session['role'] != null) {
      state = state.copyWith(
        status: AuthStatus.authenticated,
        role: session['role'],
        identifier: session['identifier'],
      );
    } else {
      state = state.copyWith(status: AuthStatus.unauthenticated);
    }
  }

  Future<void> login(String identifier, String password) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final data = await _repository.login(identifier, password);
      state = state.copyWith(
        status: AuthStatus.authenticated,
        role: data['role'],
        identifier: identifier,
        isLoading: false,
      );
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: _parseError(e),
      );
    }
  }

  Future<void> register(String identifier, String password, String role) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final data = await _repository.register(identifier, password, role);
      state = state.copyWith(
        status: AuthStatus.authenticated,
        role: data['role'],
        identifier: identifier,
        isLoading: false,
      );
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: _parseError(e),
      );
    }
  }

  Future<void> logout() async {
    await _repository.logout();
    state = const AuthState(status: AuthStatus.unauthenticated);
  }

  void clearError() {
    state = state.copyWith(error: null);
  }

  String _parseError(dynamic e) {
  if (e is DioException) {
    final data = e.response?.data;
    if (data != null && data['message'] != null) {
      final msg = data['message'].toString().toLowerCase();
      if (msg.contains('invalid credentials')) return 'Invalid email or password';
      if (msg.contains('already in use')) return 'This email is already registered';
      return data['message'].toString();
    }
    if (e.type == DioExceptionType.connectionTimeout ||
        e.type == DioExceptionType.connectionError) {
      return 'Cannot connect to server. Check your connection.';
    }
  }
  return 'Something went wrong. Please try again.';
}
}