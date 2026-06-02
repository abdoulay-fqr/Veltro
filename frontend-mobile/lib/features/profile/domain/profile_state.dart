class ProfileState {
  final bool isLoading;
  final String? error;
  final Map<String, dynamic>? member;
  final Map<String, dynamic>? healthProfile;
  final bool isSaving;

  const ProfileState({
    this.isLoading = false,
    this.error,
    this.member,
    this.healthProfile,
    this.isSaving = false,
  });

  ProfileState copyWith({
    bool? isLoading,
    String? error,
    Map<String, dynamic>? member,
    Map<String, dynamic>? healthProfile,
    bool? isSaving,
  }) {
    return ProfileState(
      isLoading: isLoading ?? this.isLoading,
      error: error,
      member: member ?? this.member,
      healthProfile: healthProfile ?? this.healthProfile,
      isSaving: isSaving ?? this.isSaving,
    );
  }
}
