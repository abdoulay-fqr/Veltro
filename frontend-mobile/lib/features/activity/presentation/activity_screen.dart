import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/activity_repository.dart';

final activityRepoProvider = Provider<ActivityRepository>((ref) => ActivityRepository());

final memberStatsProvider = FutureProvider.family<Map<String, dynamic>, int>((ref, memberId) async {
  return ref.read(activityRepoProvider).getMemberStats(memberId);
});

final recentSessionsProvider = FutureProvider.family<List<dynamic>, int>((ref, memberId) async {
  return ref.read(activityRepoProvider).getRecentSessions(memberId);
});

class ActivityScreen extends ConsumerWidget {
  final int memberProfileId;
  const ActivityScreen({super.key, required this.memberProfileId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final statsAsync = ref.watch(memberStatsProvider(memberProfileId));
    final sessionsAsync = ref.watch(recentSessionsProvider(memberProfileId));

    return Scaffold(
      backgroundColor: const Color(0xFF0F0F0F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F0F0F),
        foregroundColor: Colors.white,
        title: const Text('My Performance', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, size: 20),
            onPressed: () {
              ref.invalidate(memberStatsProvider(memberProfileId));
              ref.invalidate(recentSessionsProvider(memberProfileId));
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(memberStatsProvider(memberProfileId));
          ref.invalidate(recentSessionsProvider(memberProfileId));
        },
        child: statsAsync.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => Center(child: Text('Error: $e', style: const TextStyle(color: Colors.white54))),
          data: (stats) => ListView(
            padding: const EdgeInsets.all(16),
            children: [
              // Low activity banner
              if ((stats['currentStreak'] as int? ?? 0) == 0)
                _LowActivityBanner(),
              const SizedBox(height: 16),

              // Streak counter
              _StreakCard(streak: stats['currentStreak'] as int? ?? 0),
              const SizedBox(height: 16),

              // Personal records
              _SectionLabel('Personal Records'),
              const SizedBox(height: 10),
              Row(children: [
                Expanded(child: _RecordCard(emoji: '🔥', label: 'Max Calories', value: '${stats['maxCaloriesInSession'] ?? 0} kcal')),
                const SizedBox(width: 10),
                Expanded(child: _RecordCard(emoji: '📏', label: 'Max Distance', value: '${stats['maxDistanceKm'] ?? 0} km')),
                const SizedBox(width: 10),
                Expanded(child: _RecordCard(emoji: '⏱', label: 'Longest Session', value: '${stats['maxDurationMinutes'] ?? 0} min')),
              ]),
              const SizedBox(height: 20),

              // Weekly summary
              _SectionLabel('This Week'),
              const SizedBox(height: 10),
              Row(children: [
                Expanded(child: _StatCard(label: 'Calories', value: '${stats['weeklyCalories'] ?? 0}', unit: 'kcal')),
                const SizedBox(width: 10),
                Expanded(child: _StatCard(label: 'Total Sessions', value: '${stats['totalSessions'] ?? 0}', unit: 'all-time')),
                const SizedBox(width: 10),
                Expanded(child: _StatCard(
                  label: 'Avg Heart Rate',
                  value: stats['avgHeartRate'] != null ? '${(stats['avgHeartRate'] as double).round()}' : '—',
                  unit: 'bpm',
                )),
              ]),
              const SizedBox(height: 20),

              // Weekly calories bar chart
              _SectionLabel('Weekly Calorie Chart'),
              const SizedBox(height: 10),
              sessionsAsync.when(
                loading: () => const SizedBox(height: 80, child: Center(child: CircularProgressIndicator())),
                error: (e, _) => const SizedBox.shrink(),
                data: (sessions) => _WeeklyCaloriesChart(sessions: sessions),
              ),
              const SizedBox(height: 20),

              // Heart rate sparkline (last 10 sessions)
              _SectionLabel('Heart Rate (last 10 sessions)'),
              const SizedBox(height: 10),
              sessionsAsync.when(
                loading: () => const SizedBox.shrink(),
                error: (e, _) => const SizedBox.shrink(),
                data: (sessions) {
                  final hrData = sessions
                      .where((s) => s['avgHeartRate'] != null)
                      .take(10)
                      .map((s) => (s['avgHeartRate'] as int).toDouble())
                      .toList();
                  return _HeartRateSparkline(data: hrData);
                },
              ),
              const SizedBox(height: 32),
            ],
          ),
        ),
      ),
    );
  }
}

class _LowActivityBanner extends StatelessWidget {
  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: const Color(0xFFFBBF24).withValues(alpha: 0.15),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: const Color(0xFFFBBF24).withValues(alpha: 0.3)),
        ),
        child: const Row(children: [
          Text('💪', style: TextStyle(fontSize: 18)),
          SizedBox(width: 10),
          Expanded(
            child: Text("You haven't trained recently — book a class to keep your streak!",
                style: TextStyle(color: Colors.white70, fontSize: 13)),
          ),
        ]),
      );
}

class _StreakCard extends StatelessWidget {
  final int streak;
  const _StreakCard({required this.streak});

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: streak > 0
                ? [const Color(0xFF7C3AED), const Color(0xFF4F46E5)]
                : [const Color(0xFF1E1E1E), const Color(0xFF2A2A2A)],
            begin: Alignment.topLeft, end: Alignment.bottomRight,
          ),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Row(children: [
          Text(streak > 0 ? '🔥' : '💤', style: const TextStyle(fontSize: 40)),
          const SizedBox(width: 16),
          Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('$streak day${streak != 1 ? "s" : ""}',
                style: const TextStyle(color: Colors.white, fontSize: 28, fontWeight: FontWeight.w800)),
            Text(streak > 0 ? 'Active streak — keep it up!' : 'No streak — start today!',
                style: const TextStyle(color: Colors.white70, fontSize: 13)),
          ]),
        ]),
      );
}

class _RecordCard extends StatelessWidget {
  final String emoji, label, value;
  const _RecordCard({required this.emoji, required this.label, required this.value});

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(color: const Color(0xFF1E1E1E), borderRadius: BorderRadius.circular(12)),
        child: Column(children: [
          Text(emoji, style: const TextStyle(fontSize: 20)),
          const SizedBox(height: 4),
          Text(value, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700, fontSize: 13)),
          Text(label, style: const TextStyle(color: Colors.white38, fontSize: 10), textAlign: TextAlign.center),
        ]),
      );
}

class _StatCard extends StatelessWidget {
  final String label, value, unit;
  const _StatCard({required this.label, required this.value, required this.unit});

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(color: const Color(0xFF1E1E1E), borderRadius: BorderRadius.circular(12)),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(label, style: const TextStyle(color: Colors.white38, fontSize: 11)),
          const SizedBox(height: 4),
          Text(value, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700, fontSize: 18)),
          Text(unit, style: const TextStyle(color: Colors.white24, fontSize: 10)),
        ]),
      );
}

class _WeeklyCaloriesChart extends StatelessWidget {
  final List<dynamic> sessions;
  const _WeeklyCaloriesChart({required this.sessions});

  @override
  Widget build(BuildContext context) {
    final days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    final calories = List<int>.filled(7, 0);

    final now = DateTime.now();
    final weekStart = now.subtract(Duration(days: now.weekday - 1));

    for (final s in sessions) {
      if (s['recordedAt'] == null || s['caloriesBurned'] == null) continue;
      final dt = DateTime.parse(s['recordedAt'].toString());
      final dayIdx = dt.difference(weekStart).inDays;
      if (dayIdx >= 0 && dayIdx < 7) {
        calories[dayIdx] += (s['caloriesBurned'] as int? ?? 0);
      }
    }

    final maxCal = calories.reduce((a, b) => a > b ? a : b).toDouble();

    return Container(
      height: 120,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: const Color(0xFF1E1E1E), borderRadius: BorderRadius.circular(12)),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.end,
        children: List.generate(7, (i) {
          final pct = maxCal > 0 ? calories[i] / maxCal : 0.0;
          return Expanded(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 3),
              child: Column(mainAxisAlignment: MainAxisAlignment.end, children: [
                if (calories[i] > 0)
                  Text('${calories[i]}', style: const TextStyle(color: Colors.white54, fontSize: 9)),
                const SizedBox(height: 2),
                AnimatedContainer(
                  duration: const Duration(milliseconds: 500),
                  height: 70 * pct,
                  decoration: BoxDecoration(
                    color: i == now.weekday - 1 ? const Color(0xFF7C3AED) : const Color(0xFF3F3F46),
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
                const SizedBox(height: 4),
                Text(days[i], style: const TextStyle(color: Colors.white38, fontSize: 10)),
              ]),
            ),
          );
        }),
      ),
    );
  }
}

class _HeartRateSparkline extends StatelessWidget {
  final List<double> data;
  const _HeartRateSparkline({required this.data});

  @override
  Widget build(BuildContext context) {
    if (data.isEmpty) {
      return Container(
        height: 60,
        decoration: BoxDecoration(color: const Color(0xFF1E1E1E), borderRadius: BorderRadius.circular(12)),
        child: const Center(child: Text('No heart rate data', style: TextStyle(color: Colors.white38, fontSize: 12))),
      );
    }

    final max = data.reduce((a, b) => a > b ? a : b);
    final min = data.reduce((a, b) => a < b ? a : b);
    final range = max - min;

    return Container(
      height: 80,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: const Color(0xFF1E1E1E), borderRadius: BorderRadius.circular(12)),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.end,
        children: data.map((hr) {
          final pct = range > 0 ? (hr - min) / range : 0.5;
          final color = hr > 160 ? Colors.red : hr > 140 ? Colors.orange : const Color(0xFF4ADE80);
          return Expanded(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 2),
              child: Column(mainAxisAlignment: MainAxisAlignment.end, children: [
                Flexible(
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 400),
                    height: 40 * pct + 5,
                    decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(3)),
                  ),
                ),
              ]),
            ),
          );
        }).toList(),
      ),
    );
  }
}

class _SectionLabel extends StatelessWidget {
  final String title;
  const _SectionLabel(this.title);
  @override
  Widget build(BuildContext context) => Text(title,
      style: const TextStyle(color: Colors.white, fontSize: 14, fontWeight: FontWeight.w600));
}
