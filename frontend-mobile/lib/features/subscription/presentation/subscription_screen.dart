import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'subscription_notifier.dart';

class SubscriptionScreen extends ConsumerStatefulWidget {
  final int memberProfileId;
  const SubscriptionScreen({super.key, required this.memberProfileId});

  @override
  ConsumerState<SubscriptionScreen> createState() => _SubscriptionScreenState();
}

class _SubscriptionScreenState extends ConsumerState<SubscriptionScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(subscriptionNotifierProvider(widget.memberProfileId).notifier).load();
    });
  }

  String _planLabel(String plan) => switch (plan) {
    'TRIAL'   => '7-Day Free Trial',
    'SESSION' => 'Day Pass',
    'MONTHLY' => 'Monthly',
    'ANNUAL'  => 'Annual',
    _         => plan,
  };

  Color _statusColor(String status) => switch (status) {
    'ACTIVE'    => Colors.green,
    'PAUSED'    => Colors.orange,
    'CANCELLED' => Colors.red,
    'EXPIRED'   => Colors.grey,
    _           => Colors.grey,
  };

  void _showUpgradeSheet(BuildContext ctx, List<dynamic> plans) {
    showModalBottomSheet(
      context: ctx,
      backgroundColor: const Color(0xFF1A1A1A),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (_) => _UpgradeSheet(
        plans: plans,
        onSelect: (plan) async {
          Navigator.pop(ctx);
          final ok = await ref
              .read(subscriptionNotifierProvider(widget.memberProfileId).notifier)
              .upgrade(plan, 'CARD');
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(ok ? 'Plan upgraded to $plan!' : 'Upgrade failed')),
            );
          }
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(subscriptionNotifierProvider(widget.memberProfileId));
    final plan = state.activePlan;
    final daysRemaining = plan?['daysRemaining'] as int? ?? 0;
    final totalDays = _planDurationDays(plan?['plan'] as String? ?? 'MONTHLY');

    return Scaffold(
      backgroundColor: const Color(0xFF0F0F0F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F0F0F),
        foregroundColor: Colors.white,
        title: const Text('My Subscription', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        elevation: 0,
        actions: [
          if (state.isSaving)
            const Padding(
              padding: EdgeInsets.all(16),
              child: SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2)),
            ),
        ],
      ),
      body: state.isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: () => ref.read(subscriptionNotifierProvider(widget.memberProfileId).notifier).load(),
              child: ListView(
                padding: const EdgeInsets.all(20),
                children: [
                  // Renewal alert banner
                  if (plan != null && daysRemaining <= 7 && plan['status'] == 'ACTIVE')
                    Container(
                      margin: const EdgeInsets.only(bottom: 16),
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        color: const Color(0xFF7C3AED).withValues(alpha: 0.15),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: const Color(0xFF7C3AED).withValues(alpha: 0.3)),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.warning_amber_rounded, color: Color(0xFF7C3AED), size: 18),
                          const SizedBox(width: 10),
                          Expanded(
                            child: Text(
                              'Your subscription expires in $daysRemaining day(s). Renew now to keep access.',
                              style: const TextStyle(color: Colors.white70, fontSize: 13),
                            ),
                          ),
                        ],
                      ),
                    ),

                  // Active plan card
                  if (plan == null)
                    _NoPlanCard(onUpgrade: () => _showUpgradeSheet(context, state.plans))
                  else ...[
                    _PlanCard(
                      planName: _planLabel(plan['plan'] ?? ''),
                      status: plan['status'] ?? '',
                      statusColor: _statusColor(plan['status'] ?? ''),
                      daysRemaining: daysRemaining,
                      totalDays: totalDays,
                      endDate: plan['endDate'] ?? '',
                      autoRenew: plan['autoRenew'] == true,
                      price: plan['planPrice'] != null ? '\$${plan['planPrice']}' : '',
                    ),
                    const SizedBox(height: 16),

                    // Actions
                    Row(
                      children: [
                        Expanded(
                          child: OutlinedButton(
                            onPressed: () => _showUpgradeSheet(context, state.plans),
                            style: OutlinedButton.styleFrom(
                              side: const BorderSide(color: Colors.white24),
                              foregroundColor: Colors.white,
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                              padding: const EdgeInsets.symmetric(vertical: 14),
                            ),
                            child: const Text('Upgrade Plan'),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: _PauseResumeButton(
                            status: plan['status'] ?? '',
                            subscriptionId: plan['id'] as int? ?? 0,
                            memberId: widget.memberProfileId,
                          ),
                        ),
                      ],
                    ),
                  ],
                  const SizedBox(height: 24),

                  // Invoices
                  const Text('Payment History',
                      style: TextStyle(color: Colors.white, fontSize: 14, fontWeight: FontWeight.w600)),
                  const SizedBox(height: 12),
                  if (state.invoices.isEmpty)
                    const Text('No payments yet.', style: TextStyle(color: Colors.white38, fontSize: 13))
                  else
                    ...state.invoices.map((inv) => _InvoiceTile(invoice: inv as Map<String, dynamic>)),
                  const SizedBox(height: 24),
                ],
              ),
            ),
    );
  }

  int _planDurationDays(String plan) => switch (plan) {
    'TRIAL'   => 7,
    'SESSION' => 1,
    'MONTHLY' => 30,
    'ANNUAL'  => 365,
    _         => 30,
  };
}

class _PlanCard extends StatelessWidget {
  final String planName, status, endDate, price;
  final Color statusColor;
  final int daysRemaining, totalDays;
  final bool autoRenew;

  const _PlanCard({
    required this.planName,
    required this.status,
    required this.statusColor,
    required this.daysRemaining,
    required this.totalDays,
    required this.endDate,
    required this.autoRenew,
    required this.price,
  });

  @override
  Widget build(BuildContext context) {
    final progress = totalDays > 0 ? (1 - daysRemaining / totalDays).clamp(0.0, 1.0) : 0.0;
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: const Color(0xFF1E1E1E),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(children: [
            Text(planName,
                style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w700)),
            const Spacer(),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
              decoration: BoxDecoration(
                color: statusColor.withValues(alpha: 0.15),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(status,
                  style: TextStyle(color: statusColor, fontSize: 12, fontWeight: FontWeight.w600)),
            ),
          ]),
          const SizedBox(height: 4),
          Text(price, style: const TextStyle(color: Colors.white54, fontSize: 14)),
          const SizedBox(height: 16),
          ClipRRect(
            borderRadius: BorderRadius.circular(4),
            child: LinearProgressIndicator(
              value: progress,
              backgroundColor: Colors.white12,
              valueColor: const AlwaysStoppedAnimation<Color>(Color(0xFF7C3AED)),
              minHeight: 6,
            ),
          ),
          const SizedBox(height: 8),
          Row(children: [
            Text('$daysRemaining days left',
                style: const TextStyle(color: Colors.white70, fontSize: 13)),
            const Spacer(),
            Text('Expires $endDate',
                style: const TextStyle(color: Colors.white38, fontSize: 12)),
          ]),
          if (autoRenew) ...[
            const SizedBox(height: 8),
            const Row(children: [
              Icon(Icons.autorenew, color: Color(0xFF4ADE80), size: 14),
              SizedBox(width: 4),
              Text('Auto-renew enabled', style: TextStyle(color: Color(0xFF4ADE80), fontSize: 12)),
            ]),
          ],
        ],
      ),
    );
  }
}

class _NoPlanCard extends StatelessWidget {
  final VoidCallback onUpgrade;
  const _NoPlanCard({required this.onUpgrade});

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
          color: const Color(0xFF1E1E1E),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(children: [
          const Icon(Icons.card_membership, color: Colors.white38, size: 40),
          const SizedBox(height: 12),
          const Text('No active plan', style: TextStyle(color: Colors.white70, fontSize: 16)),
          const SizedBox(height: 8),
          const Text('Subscribe to access all gym features.',
              style: TextStyle(color: Colors.white38, fontSize: 13), textAlign: TextAlign.center),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: onUpgrade,
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.white,
              foregroundColor: Colors.black,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            child: const Text('Choose a Plan'),
          ),
        ]),
      );
}

class _PauseResumeButton extends ConsumerWidget {
  final String status;
  final int subscriptionId, memberId;
  const _PauseResumeButton({required this.status, required this.subscriptionId, required this.memberId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isPaused = status == 'PAUSED';
    return OutlinedButton(
      onPressed: () async {
        final notifier = ref.read(subscriptionNotifierProvider(memberId).notifier);
        final ok = isPaused
            ? await notifier.resume(subscriptionId)
            : await notifier.pause(subscriptionId);
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(ok
                ? (isPaused ? 'Subscription resumed' : 'Subscription paused')
                : 'Action failed')),
          );
        }
      },
      style: OutlinedButton.styleFrom(
        side: BorderSide(color: isPaused ? Colors.green.withValues(alpha: 0.4) : Colors.orange.withValues(alpha: 0.4)),
        foregroundColor: isPaused ? Colors.green : Colors.orange,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        padding: const EdgeInsets.symmetric(vertical: 14),
      ),
      child: Text(isPaused ? 'Resume' : 'Pause'),
    );
  }
}

class _InvoiceTile extends StatelessWidget {
  final Map<String, dynamic> invoice;
  const _InvoiceTile({required this.invoice});

  @override
  Widget build(BuildContext context) => Container(
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          color: const Color(0xFF1E1E1E),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(children: [
          const Icon(Icons.receipt_outlined, color: Colors.white38, size: 18),
          const SizedBox(width: 12),
          Expanded(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('\$${invoice['amount']}',
                  style: const TextStyle(color: Colors.white, fontSize: 14, fontWeight: FontWeight.w600)),
              Text('${invoice['method']} · ${invoice['paidAt']?.toString().substring(0, 10) ?? ''}',
                  style: const TextStyle(color: Colors.white38, fontSize: 12)),
            ]),
          ),
          Text(invoice['invoiceRef'] ?? '',
              style: const TextStyle(color: Colors.white24, fontSize: 11, fontFamily: 'monospace')),
        ]),
      );
}

class _UpgradeSheet extends StatelessWidget {
  final List<dynamic> plans;
  final void Function(String plan) onSelect;
  const _UpgradeSheet({required this.plans, required this.onSelect});

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Choose a Plan',
                style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w700)),
            const SizedBox(height: 4),
            const Text('Cancel anytime. Auto-renew available.',
                style: TextStyle(color: Colors.white38, fontSize: 13)),
            const SizedBox(height: 20),
            ...plans.map((p) {
              final plan = p as Map<String, dynamic>;
              final isFree = (plan['price'] as num?) == 0;
              return GestureDetector(
                onTap: () => onSelect(plan['plan'] as String),
                child: Container(
                  margin: const EdgeInsets.only(bottom: 10),
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: const Color(0xFF2A2A2A),
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(color: Colors.white12),
                  ),
                  child: Row(children: [
                    Expanded(
                      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                        Text(plan['plan'] as String,
                            style: const TextStyle(color: Colors.white, fontSize: 15, fontWeight: FontWeight.w600)),
                        const SizedBox(height: 2),
                        Text(plan['description'] as String? ?? '',
                            style: const TextStyle(color: Colors.white38, fontSize: 12)),
                      ]),
                    ),
                    Text(
                      isFree ? 'Free' : '\$${plan['price']}',
                      style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w700),
                    ),
                  ]),
                ),
              );
            }),
            const SizedBox(height: 8),
          ],
        ),
      );
}
