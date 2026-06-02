import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/subscription_repository.dart';
import '../domain/subscription_state.dart';

final subscriptionRepositoryProvider = Provider<SubscriptionRepository>((ref) => SubscriptionRepository());

final subscriptionNotifierProvider = StateNotifierProvider.family<SubscriptionNotifier, SubscriptionState, int>(
  (ref, memberId) => SubscriptionNotifier(ref.read(subscriptionRepositoryProvider), memberId),
);

class SubscriptionNotifier extends StateNotifier<SubscriptionState> {
  final SubscriptionRepository _repo;
  final int _memberId;

  SubscriptionNotifier(this._repo, this._memberId) : super(const SubscriptionState());

  Future<void> load() async {
    state = state.copyWith(isLoading: true);
    try {
      final results = await Future.wait([
        _repo.getActivePlan(_memberId).catchError((_) => <String, dynamic>{}),
        _repo.getInvoices(_memberId).catchError((_) => <dynamic>[]),
        _repo.getPlans().catchError((_) => <dynamic>[]),
      ]);
      state = state.copyWith(
        isLoading: false,
        activePlan: results[0] as Map<String, dynamic>?,
        invoices: results[1] as List<dynamic>,
        plans: results[2] as List<dynamic>,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<bool> pause(int subscriptionId) async {
    state = state.copyWith(isSaving: true);
    try {
      final updated = await _repo.pause(subscriptionId);
      state = state.copyWith(isSaving: false, activePlan: updated);
      return true;
    } catch (e) {
      state = state.copyWith(isSaving: false, error: e.toString());
      return false;
    }
  }

  Future<bool> resume(int subscriptionId) async {
    state = state.copyWith(isSaving: true);
    try {
      final updated = await _repo.resume(subscriptionId);
      state = state.copyWith(isSaving: false, activePlan: updated);
      return true;
    } catch (e) {
      state = state.copyWith(isSaving: false, error: e.toString());
      return false;
    }
  }

  Future<bool> upgrade(String plan, String paymentMethod) async {
    state = state.copyWith(isSaving: true);
    try {
      await _repo.upgradePlan(_memberId, plan, paymentMethod);
      await load();
      return true;
    } catch (e) {
      state = state.copyWith(isSaving: false, error: e.toString());
      return false;
    }
  }
}
