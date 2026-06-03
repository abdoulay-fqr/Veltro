import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../data/booking_repository.dart';

final bookingRepoProvider = Provider<BookingRepository>((ref) => BookingRepository());

final coursesProvider = FutureProvider.family<List<dynamic>, String?>((ref, level) async {
  return ref.read(bookingRepoProvider).getCourses(level: level, status: 'SCHEDULED');
});

class CoursesScreen extends ConsumerStatefulWidget {
  const CoursesScreen({super.key});

  @override
  ConsumerState<CoursesScreen> createState() => _CoursesScreenState();
}

class _CoursesScreenState extends ConsumerState<CoursesScreen> {
  String? _selectedLevel;
  final _searchCtrl = TextEditingController();

  @override
  void dispose() { _searchCtrl.dispose(); super.dispose(); }

  void _showFilterSheet() {
    showModalBottomSheet(
      context: context,
      backgroundColor: const Color(0xFF1A1A1A),
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (_) => Padding(
        padding: const EdgeInsets.all(24),
        child: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('Filter by Level', style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w700)),
          const SizedBox(height: 16),
          ...['All', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'].map((level) => ListTile(
            title: Text(level, style: const TextStyle(color: Colors.white70)),
            trailing: _selectedLevel == (level == 'All' ? null : level)
                ? const Icon(Icons.check, color: Colors.white) : null,
            onTap: () {
              setState(() => _selectedLevel = level == 'All' ? null : level);
              Navigator.pop(context);
            },
          )),
          const SizedBox(height: 8),
        ]),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final coursesAsync = ref.watch(coursesProvider(_selectedLevel));

    return Scaffold(
      backgroundColor: const Color(0xFF0F0F0F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F0F0F),
        foregroundColor: Colors.white,
        title: const Text('Classes', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        elevation: 0,
        actions: [
          IconButton(icon: const Icon(Icons.filter_list, size: 20), onPressed: _showFilterSheet),
        ],
      ),
      body: Column(children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
          child: TextField(
            controller: _searchCtrl,
            style: const TextStyle(color: Colors.white, fontSize: 14),
            onChanged: (_) => setState(() {}),
            decoration: InputDecoration(
              hintText: 'Search classes…',
              hintStyle: const TextStyle(color: Colors.white38),
              prefixIcon: const Icon(Icons.search, color: Colors.white38, size: 18),
              filled: true, fillColor: const Color(0xFF1E1E1E),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
            ),
          ),
        ),
        Expanded(
          child: coursesAsync.when(
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (e, _) => Center(child: Text('Error: $e', style: const TextStyle(color: Colors.white54))),
            data: (courses) {
              final query = _searchCtrl.text.toLowerCase();
              final filtered = query.isEmpty ? courses
                  : courses.where((c) => (c['name'] ?? '').toString().toLowerCase().contains(query)).toList();

              if (filtered.isEmpty) {
                return const Center(child: Text('No classes found', style: TextStyle(color: Colors.white38)));
              }

              return ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: filtered.length,
                itemBuilder: (_, i) {
                  final c = filtered[i] as Map<String, dynamic>;
                  return _CourseCard(course: c, onTap: () => context.push('/courses/${c['id']}'));
                },
              );
            },
          ),
        ),
      ]),
    );
  }
}

class _CourseCard extends StatelessWidget {
  final Map<String, dynamic> course;
  final VoidCallback onTap;
  const _CourseCard({required this.course, required this.onTap});

  Color _levelColor(String? level) => switch (level) {
    'BEGINNER'     => const Color(0xFF60A5FA),
    'INTERMEDIATE' => const Color(0xFFFBBF24),
    'ADVANCED'     => const Color(0xFFA78BFA),
    _              => Colors.grey,
  };

  @override
  Widget build(BuildContext context) {
    final available = (course['availableSpots'] as int? ?? 0);
    final capacity = (course['capacity'] as int? ?? 1);
    final fillRate = (course['fillRate'] as double? ?? 0.0);
    final dateTime = course['dateTime'] != null ? DateTime.parse(course['dateTime'].toString()) : DateTime.now();

    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(color: const Color(0xFF1E1E1E), borderRadius: BorderRadius.circular(16)),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [
            Expanded(child: Text(course['name'] ?? '', style: const TextStyle(color: Colors.white, fontSize: 15, fontWeight: FontWeight.w600))),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
              decoration: BoxDecoration(color: _levelColor(course['level']?.toString()).withOpacity(0.15), borderRadius: BorderRadius.circular(20)),
              child: Text(course['level'] ?? '', style: TextStyle(color: _levelColor(course['level']?.toString()), fontSize: 11, fontWeight: FontWeight.w600)),
            ),
          ]),
          const SizedBox(height: 6),
          Text('${dateTime.toString().substring(0, 16).replaceFirst('T', ' ')} · ${course['room'] ?? 'No room'}',
              style: const TextStyle(color: Colors.white54, fontSize: 12)),
          const SizedBox(height: 10),
          Row(children: [
            Expanded(child: ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: LinearProgressIndicator(value: fillRate.clamp(0.0, 1.0), backgroundColor: Colors.white12,
                  valueColor: AlwaysStoppedAnimation<Color>(fillRate > 0.8 ? Colors.red : Colors.green), minHeight: 4),
            )),
            const SizedBox(width: 8),
            Text('$available spots', style: TextStyle(color: available == 0 ? Colors.red : Colors.white54, fontSize: 12)),
          ]),
        ]),
      ),
    );
  }
}
