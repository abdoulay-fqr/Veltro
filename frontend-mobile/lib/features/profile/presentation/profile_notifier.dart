import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/profile_repository.dart';
import '../domain/profile_state.dart';

final profileRepositoryProvider = Provider<ProfileRepository>((ref) => ProfileRepository());

final profileNotifierProvider = StateNotifierProvider<ProfileNotifier, ProfileState>(
  (ref) => ProfileNotifier(ref.read(profileRepositoryProvider)),
);

class ProfileNotifier extends StateNotifier<ProfileState> {
  final ProfileRepository _repository;

  ProfileNotifier(this._repository) : super(const ProfileState());

  Future<void> load(int memberProfileId) async {
    state = state.copyWith(isLoading: true);
    try {
      final member = await _repository.getMemberProfile(memberProfileId);
      Map<String, dynamic>? health;
      try {
        health = await _repository.getHealthProfile(memberProfileId);
      } catch (_) {}
      state = state.copyWith(isLoading: false, member: member, healthProfile: health);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<bool> updateProfile(int memberProfileId, Map<String, dynamic> fields) async {
    state = state.copyWith(isSaving: true);
    try {
      final updated = await _repository.updateMemberProfile(memberProfileId, fields);
      state = state.copyWith(isSaving: false, member: updated);
      return true;
    } catch (e) {
      state = state.copyWith(isSaving: false, error: e.toString());
      return false;
    }
  }

  Future<bool> uploadAvatar(int memberProfileId, String filePath) async {
    state = state.copyWith(isSaving: true);
    try {
      final updated = await _repository.uploadAvatar(memberProfileId, filePath);
      state = state.copyWith(isSaving: false, member: updated);
      return true;
    } catch (e) {
      state = state.copyWith(isSaving: false, error: e.toString());
      return false;
    }
  }

  Future<bool> upsertHealthProfile(int memberProfileId, Map<String, dynamic> fields) async {
    state = state.copyWith(isSaving: true);
    try {
      final updated = await _repository.upsertHealthProfile(memberProfileId, fields);
      state = state.copyWith(isSaving: false, healthProfile: updated);
      return true;
    } catch (e) {
      state = state.copyWith(isSaving: false, error: e.toString());
      return false;
    }
  }
}
