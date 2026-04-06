enum AuthStatus { unknown, authenticated, unauthenticated }

class AuthState {
  final AuthStatus status;
  final String? role;
  final String? identifier;
  final String? error;
  final bool isLoading;

  const AuthState({
    this.status = AuthStatus.unknown,
    this.role,
    this.identifier,
    this.error,
    this.isLoading = false,
  });

  AuthState copyWith({
    AuthStatus? status,
    String? role,
    String? identifier,
    String? error,
    bool? isLoading,
  }) {
    return AuthState(
      status: status ?? this.status,
      role: role ?? this.role,
      identifier: identifier ?? this.identifier,
      error: error ?? this.error,
      isLoading: isLoading ?? this.isLoading,
    );
  }
}