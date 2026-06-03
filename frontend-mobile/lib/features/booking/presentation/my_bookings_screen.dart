import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/booking_repository.dart';

final myBookingsProvider = FutureProvider.family<List<dynamic>, Map<String, dynamic>>((ref, args) async {
  return ref.read(bookingRepoProvider).getMyBookings(args['memberId'] as int, status: args['status'] as String?);
});

class MyBookingsScreen extends ConsumerStatefulWidget {
  final int memberId;
  const MyBookingsScreen({super.key, required this.memberId});

  @override
  ConsumerState<MyBookingsScreen> createState() => _MyBookingsScreenState();
}

class _MyBookingsScreenState extends ConsumerState<MyBookingsScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
  }

  @override
  void dispose() { _tabController.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F0F0F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F0F0F),
        foregroundColor: Colors.white,
        title: const Text('My Bookings', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        elevation: 0,
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: Colors.white,
          labelColor: Colors.white,
          unselectedLabelColor: Colors.white38,
          tabs: const [Tab(text: 'Upcoming'), Tab(text: 'Past'), Tab(text: 'Cancelled')],
        ),
      ),
      body: TabBarView(controller: _tabController, children: [
        _BookingList(memberId: widget.memberId, status: 'BOOKED'),
        _BookingList(memberId: widget.memberId, status: null, showAttendance: true),
        _BookingList(memberId: widget.memberId, status: 'CANCELLED'),
      ]),
    );
  }
}

class _BookingList extends ConsumerWidget {
  final int memberId;
  final String? status;
  final bool showAttendance;

  const _BookingList({required this.memberId, required this.status, this.showAttendance = false});

  Future<void> _cancel(BuildContext context, WidgetRef ref, int bookingId) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        backgroundColor: const Color(0xFF1E1E1E),
        title: const Text('Cancel Booking?', style: TextStyle(color: Colors.white)),
        content: const Text('Cancellations must be made at least 2 hours before the course.',
            style: TextStyle(color: Colors.white70)),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false),
              child: const Text('Keep', style: TextStyle(color: Colors.white54))),
          TextButton(onPressed: () => Navigator.pop(context, true),
              child: const Text('Cancel Booking', style: TextStyle(color: Colors.red))),
        ],
      ),
    );
    if (confirmed != true) return;
    try {
      await BookingRepository().cancelBooking(bookingId);
      ref.invalidate(myBookingsProvider);
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Booking cancelled')));
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
      }
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bookingsAsync = ref.watch(myBookingsProvider({'memberId': memberId, 'status': status}));
    return bookingsAsync.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('Error: $e', style: const TextStyle(color: Colors.white54))),
      data: (bookings) {
        if (bookings.isEmpty) {
          return Center(child: Text(
            status == 'CANCELLED' ? 'No cancelled bookings' : 'No ${status?.toLowerCase() ?? ''} bookings',
            style: const TextStyle(color: Colors.white38),
          ));
        }
        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: bookings.length,
          itemBuilder: (_, i) {
            final b = bookings[i] as Map<String, dynamic>;
            final bookingStatus = b['status']?.toString() ?? '';
            final hoursUntil = b['courseDateTime'] != null
                ? DateTime.parse(b['courseDateTime'].toString()).difference(DateTime.now()).inHours
                : null;
            final canCancel = bookingStatus == 'BOOKED' && (hoursUntil == null || hoursUntil >= 2);

            return Container(
              margin: const EdgeInsets.only(bottom: 10),
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(color: const Color(0xFF1E1E1E), borderRadius: BorderRadius.circular(14)),
              child: Row(children: [
                Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Text('Booking #${b['id']}', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w600)),
                  const SizedBox(height: 2),
                  Text('Course #${b['courseId']}', style: const TextStyle(color: Colors.white54, fontSize: 12)),
                  if (bookingStatus == 'WAITLISTED')
                    Text('Waitlist position #${b['waitlistPosition']}',
                        style: const TextStyle(color: Colors.orange, fontSize: 12)),
                  if (showAttendance && b['present'] != null)
                    Text(b['present'] == true ? '✓ Present' : '✗ Absent',
                        style: TextStyle(color: b['present'] == true ? Colors.green : Colors.red, fontSize: 12)),
                ])),
                if (canCancel)
                  TextButton(
                    onPressed: () => _cancel(context, ref, b['id'] as int),
                    style: TextButton.styleFrom(foregroundColor: Colors.red),
                    child: const Text('Cancel'),
                  ),
              ]),
            );
          },
        );
      },
    );
  }
}
