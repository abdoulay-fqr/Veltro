import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

/// Maps FCM notification payloads to in-app routes.
///
/// When a real FCM token is integrated (Phase 9), wire this class into
/// FirebaseMessaging.onMessageOpenedApp and getInitialMessage in main.dart.
///
/// Expected FCM data payload shape:
/// {
///   "type": "MESSAGE" | "SUBSCRIPTION" | "BOOKING" | "SHOP" | "WARNING",
///   "id":   "123"       // entity ID to navigate to
/// }
class NotificationRouter {
  static final _navigatorKey = GlobalKey<NavigatorState>();

  static GlobalKey<NavigatorState> get navigatorKey => _navigatorKey;

  /// Call this when a notification is tapped (app opened from background/killed).
  static void handleNotificationTap(
    BuildContext context,
    Map<String, dynamic> data,
  ) {
    final type = data['type'] as String?;
    final id   = data['id']   as String?;

    switch (type) {
      case 'MESSAGE':
        // Navigate to the specific conversation
        final conversationId = int.tryParse(id ?? '0') ?? 0;
        context.push('/chat/$conversationId');

      case 'BOOKING':
        // Navigate to the member's bookings list
        context.push('/my-bookings/1');

      case 'SUBSCRIPTION':
        // Navigate to the subscription screen
        context.push('/subscription/1');

      case 'SHOP':
        // Navigate to order history
        context.push('/shop');

      case 'WARNING':
        // MemberWarning — navigate to home (shows the warning banner)
        context.go('/home');

      case 'COURSE_REMINDER':
        // Navigate to upcoming bookings
        context.push('/my-bookings/1');

      default:
        // Unknown type — navigate to home
        context.go('/home');
    }
  }

  /// Routing table for reference (also used to document push payloads).
  ///
  /// | FCM type         | In-app route            | Trigger                         |
  /// |-----------------|-------------------------|---------------------------------|
  /// | MESSAGE          | /chat/{conversationId}  | MessageReceivedEvent            |
  /// | SUBSCRIPTION     | /subscription/1         | SubscriptionExpiringEvent       |
  /// | BOOKING          | /my-bookings/1          | WaitlistPromotedEvent           |
  /// | COURSE_REMINDER  | /my-bookings/1          | CourseReminderEvent             |
  /// | WARNING          | /home                   | MemberWarningEvent              |
  /// | SHOP             | /shop                   | OrderPlacedEvent                |
  static const routingTable = <String, String>{
    'MESSAGE':         '/chat/:id',
    'SUBSCRIPTION':    '/subscription/:id',
    'BOOKING':         '/my-bookings/:id',
    'COURSE_REMINDER': '/my-bookings/:id',
    'WARNING':         '/home',
    'SHOP':            '/shop',
  };
}
