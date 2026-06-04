import 'dart:convert';
import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../../core/network/dio_client.dart';

class AuthRepository {
  static const _storage = FlutterSecureStorage();

  /// On Flutter web, Dio sometimes delivers response.data as a raw JSON String
  /// instead of a parsed Map. This helper normalises both forms.
  Map<String, dynamic> _asMap(dynamic raw) {
    if (raw is Map<String, dynamic>) return raw;
    if (raw is String) return jsonDecode(raw) as Map<String, dynamic>;
    throw FormatException('Unexpected response type: ${raw.runtimeType}');
  }
  // Use the platform-aware DioClient.baseUrl so physical-device builds
  // reach 172.20.10.9 instead of device-loopback localhost.
  late final Dio _dio = Dio(BaseOptions(
    baseUrl: DioClient.baseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
    headers: {'Content-Type': 'application/json'},
  ));

  Future<Map<String, dynamic>> login(String identifier, String password) async {
    final response = await _dio.post('/auth/login', data: {
      'identifier': identifier,
      'password': password,
    });
    final body = _asMap(response.data);
    final data = body['data'] as Map<String, dynamic>;
    final userId = data['userId'] is int
        ? data['userId'] as int
        : int.tryParse(data['userId']?.toString() ?? '0') ?? 0;
    await _storage.write(key: 'access_token', value: data['accessToken'] as String);
    await _storage.write(key: 'refresh_token', value: data['refreshToken'] as String);
    await _storage.write(key: 'role', value: data['role'] as String);
    await _storage.write(key: 'user_id', value: userId.toString());
    await _storage.write(key: 'identifier', value: identifier);
    return {...data, 'userId': userId};
  }

  Future<Map<String, dynamic>> register(String identifier, String password, String role) async {
    final response = await _dio.post('/auth/register', data: {
      'identifier': identifier,
      'password': password,
      'role': role,
    });
    final body = _asMap(response.data);
    final data = body['data'] as Map<String, dynamic>;
    final userId = data['userId'] is int
        ? data['userId'] as int
        : int.tryParse(data['userId']?.toString() ?? '0') ?? 0;
    await _storage.write(key: 'access_token', value: data['accessToken'] as String);
    await _storage.write(key: 'refresh_token', value: data['refreshToken'] as String);
    await _storage.write(key: 'role', value: data['role'] as String);
    await _storage.write(key: 'user_id', value: userId.toString());
    await _storage.write(key: 'identifier', value: identifier);
    return {...data, 'userId': userId};
  }

  Future<void> logout() async {
    final token = await _storage.read(key: 'access_token');
    if (token != null) {
      try {
        await _dio.post(
          '/auth/logout',
          options: Options(headers: {'Authorization': 'Bearer $token'}),
        );
      } catch (_) {}
    }
    await _storage.deleteAll();
  }

  Future<Map<String, String?>> getSavedSession() async {
    final role = await _storage.read(key: 'role');
    final identifier = await _storage.read(key: 'identifier');
    final token = await _storage.read(key: 'access_token');
    return {'role': role, 'identifier': identifier, 'token': token};
  }
}