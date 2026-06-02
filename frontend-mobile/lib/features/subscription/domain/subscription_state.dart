class SubscriptionState {
  final bool isLoading;
  final String? error;
  final Map<String, dynamic>? activePlan;
  final List<dynamic> invoices;
  final List<dynamic> plans;
  final bool isSaving;

  const SubscriptionState({
    this.isLoading = false,
    this.error,
    this.activePlan,
    this.invoices = const [],
    this.plans = const [],
    this.isSaving = false,
  });

  SubscriptionState copyWith({
    bool? isLoading,
    String? error,
    Map<String, dynamic>? activePlan,
    List<dynamic>? invoices,
    List<dynamic>? plans,
    bool? isSaving,
  }) {
    return SubscriptionState(
      isLoading: isLoading ?? this.isLoading,
      error: error,
      activePlan: activePlan ?? this.activePlan,
      invoices: invoices ?? this.invoices,
      plans: plans ?? this.plans,
      isSaving: isSaving ?? this.isSaving,
    );
  }
}
